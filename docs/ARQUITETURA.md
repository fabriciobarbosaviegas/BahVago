# Arquitetura e Funcionalidades — BahVago

Este documento descreve **como o sistema é organizado** e **o que ele faz**. Para a referência endpoint a endpoint e detalhes de configuração, veja [DOCUMENTACAO.md](DOCUMENTACAO.md); para convenções de código, [../GUIA_DESENVOLVIMENTO.md](../GUIA_DESENVOLVIMENTO.md).

## 1. Visão Geral

O BahVago é uma aplicação web monolítica **Spring Boot MVC com renderização no servidor** (Thymeleaf). Não há SPA nem API REST separada: os controllers devolvem páginas HTML, e um conjunto pequeno de endpoints JSON atende interações dinâmicas (favoritos, avaliações, atualização ao vivo de ofertas).

```
┌─────────────────────────────────────────────────────────────┐
│  Navegador                                                  │
│  templates Thymeleaf + static/js/script.js + css/style.css  │
└──────────────┬──────────────────────────────────────────────┘
               │ HTTP (páginas + alguns endpoints JSON)
┌──────────────▼──────────────────────────────────────────────┐
│  Spring Boot (porta 8080)                                   │
│  SecurityConfig ─► Controllers ─► Services ─► Repositories  │
│                        │              │                     │
│                        │              ├─► MySQL 8 (Docker,  │
│                        │              │   schema fixo pela  │
│                        │              │   migration)        │
│                        │              ├─► API de ofertas    │
│                        │              │   (localhost:8001)  │
│                        │              └─► Nominatim/OSM     │
│                        └─► uploads/ (imagens no disco)      │
└─────────────────────────────────────────────────────────────┘
```

Decisão estruturante do projeto: **o schema do banco é imutável** (`DB/migrations/bahvagoBD.sql`, executado uma única vez na criação do container MySQL). Toda funcionalidade posterior foi construída sem tocar no schema — reutilizando colunas, calculando em tempo de requisição ou mantendo estado auxiliar em memória.

## 2. Camadas

| Camada | Pacote | Papel |
|--------|--------|-------|
| Configuração | `com.bahvago.config` | `SecurityConfig` (autenticação, papéis, CSRF), `WebConfig` (serve `uploads/` como recurso estático) |
| Controller | `com.bahvago.controller` | Recebe HTTP, valida acesso/propriedade, orquestra services, devolve view ou JSON. DTOs de formulário em `controller/dto` |
| Service | `com.bahvago.service` | Regras de negócio, integrações externas e estado em memória |
| Repository | `com.bahvago.repository` | Interfaces Spring Data JPA |
| Model | `com.bahvago.model` | Entidades JPA espelhando exatamente as tabelas da migration |
| DTO externo | `com.bahvago.dto` | Contratos da API externa de ofertas (`OfertaExterna`, `OfertaExternaResponse`, `OfertasQuartoResponse`) |
| Util | `com.bahvago.util` | `OfertaMatcher` — casamento textual quarto ↔ oferta |

### Services principais

- **`UsuarioService` / `CustomUserDetailsService`** — cadastro com BCrypt e ponte com o Spring Security (login por email; papel derivado do campo `Tipo` do usuário).
- **`HotelService`, `QuartoService`, `AvaliacaoService`, `OfertaService`, `LocalizacaoService`** — CRUD e consultas de domínio. Destaques: `QuartoService.buscarDisponiveisPorHotel` (listagem pública nunca mostra quarto indisponível) e `LocalizacaoService.buscarOuCriar` (localização tem chave natural lat+lon).
- **`FavoritoService`** — monta as views de ofertas salvas do usuário (`OfertaSalvaView`), incluindo foto do hotel.
- **`FileStorageService`** — persiste uploads em `uploads/<subpasta>/` e devolve URLs públicas `/uploads/...`.
- **`OfertaExternaService`** — integração com a API de ofertas (ver §5.1).
- **`GeocodingService`** — integração com Nominatim (ver §5.2).
- **`ManutencaoQuartoService`** — estado em memória "quarto em manutenção" (ver §6).

## 3. Modelo de Dados

Schema fixo definido em `DB/migrations/bahvagoBD.sql` (MySQL 8, utf8mb4):

| Tabela | Entidade JPA | Observações |
|--------|--------------|-------------|
| `Localizacao` | `Localizacao` (+`LocalizacaoId`) | **PK composta (Latitude, Longitude) em micrograus `INT`** (`grau × 1.000.000`) |
| `CriterioBusca` | — | Critério de busca salvo do usuário (FK opcional em `Usuario`) |
| `Usuario` | `Usuario` | PK `CPF` (char 11); `Email` único (login); `Tipo` (bit) distingue viajante × hoteleiro |
| `HotelEstatisticas` | `Hotel` | O nome da tabela vem do modelo original; a entidade chama-se `Hotel`. FK para dono (`CPF`) e `Localizacao` |
| `ImagemHotel` | `Hotel.imagens` | `@ElementCollection` de URLs |
| `Quarto` | `Quarto` (+`QuartoId`) | **PK composta (Numero, CodigoHotel)**; `Disponivel` controla visibilidade pública |
| `ImagemQuarto` | `Quarto.imagens` | `@ElementCollection` de URLs |
| `Oferta` | `Oferta` | Ofertas persistidas vinculadas a quartos (usadas nos favoritos) — distintas das ofertas externas ao vivo |
| `Avaliacao` | `Avaliacao` | Nota + comentário + campo `Resposta` (respondida pelo hoteleiro) |
| `Salva` | `Salva` | Oferta favoritada por usuário (CPF + CodigoOferta) |
| `Reserva` | `Reserva` | Tabela existe; o fluxo web de reservas ainda não foi implementado (`ReservaController`/`ReservaService` vazios) |
| `HotelFavorito` | — | Hotel inteiro favoritado (distinto de `Salva`) |

## 4. Segurança

- **Autenticação**: formulário (`/login`), parâmetros `email`/`senha`, senha BCrypt. Login de hoteleiro em página própria (`/login-hoteleiro`); um `AuthenticationSuccessHandler` verifica o papel e redireciona hoteleiros ao `/dashboard` (e rejeita não-hoteleiros que tentem entrar por ali).
- **CSRF**: `CookieCsrfTokenRepository` com cookie `XSRF-TOKEN` legível por JS; os `fetch` do frontend reenviam o token no header `X-XSRF-TOKEN`.
- **Autorização em duas camadas**:
  1. **Papel** (`SecurityConfig`): rotas administrativas exigem `ROLE_HOTELEIRO`. As regras são avaliadas **em ordem de declaração** — o bloco do hoteleiro vem propositalmente *antes* dos `permitAll()` amplos de `/hoteis/**` e `/quartos/**`.
  2. **Propriedade** (controllers): todo endpoint que lê formulário administrativo ou muta hotel/quarto chama `verificarPropriedade*`, comparando o CPF do hoteleiro autenticado com o CPF dono do hotel. Papel certo ≠ dono do recurso.
- Rotas de conta/favoritos exigem apenas autenticação; busca, página de hotel e página de quarto são públicas.

## 5. Integrações Externas

### 5.1 API de ofertas de parceiros

A comparação de preços da página do quarto vem de uma API externa estilo Trivago (`POST /api/v1/hoteis/ofertas`, padrão `http://localhost:8001`, configurável). Fluxo em `OfertaExternaService`:

1. **Cache primeiro** — `ConcurrentHashMap` chaveado por `(hotel, checkin, checkout, quartos, pessoas)` com TTL (padrão 300s). Buscas repetidas no intervalo não tocam a rede.
2. **Chamada HTTP** — `RestClient` construído com `SimpleClientHttpRequestFactory` (a factory padrão do Boot 3.2.0 descarta corpos POST pequenos). Corpo: `{hotel, dataCheckin, dataCheckout, quartos, pessoas}`.
3. **Filtro por quarto** — `OfertaMatcher` casa o nome livre do quarto da oferta (ex.: "STANDARD DUAS CAMAS") com `Quarto.tipo`/`descricao`, normalizando acentos/caixa e usando sinônimos por tipo. Sem match → devolve todas as ofertas do hotel (nunca tela vazia por excesso de rigor).
4. **Ordenação** — por preço/noite (parse do formato `"R$ 356,00"`); o índice 0 é o "melhor preço".
5. **Resiliência** — qualquer falha (API fora, timeout) vira warning no log + lista vazia; a página renderiza o estado "Nenhuma oferta encontrada".

Os DTOs usam `@JsonAlias` para aceitar o snake_case da API sem quebrar a serialização camelCase do endpoint JSON interno (`.../ofertas`), consumido pelo JS na atualização ao vivo de datas.

### 5.2 Geocodificação (Nominatim/OpenStreetMap)

Ao salvar o endereço do hotel, `GeocodingService` consulta `https://nominatim.openstreetmap.org/search` (com `User-Agent` identificado, exigência do serviço), converte lat/lon para micrograus inteiros e o `HotelController` usa `LocalizacaoService.buscarOuCriar` para apontar o hotel à localização correta. Falha de geocodificação não bloqueia o salvamento: os campos textuais do endereço são atualizados e o hoteleiro recebe um toast de aviso.

## 6. Estado em Memória (consequência da regra de ouro)

Dois estados vivem apenas na JVM, por decisão consciente de não alterar o schema:

| Estado | Onde | Comportamento no restart |
|--------|------|--------------------------|
| Cache de ofertas externas | `OfertaExternaService` | Se perde; próxima busca refaz a chamada HTTP |
| Flag "Em manutenção" por quarto | `ManutencaoQuartoService` | Se perde a *distinção do motivo*; o quarto continua indisponível (coluna `Disponivel=false` é persistida normalmente) |

"Em manutenção" e "Indisponível" são conceitos distintos: marcar manutenção força `Disponivel=false` no salvamento, mas um quarto pode estar indisponível sem estar em manutenção.

## 7. Funcionalidades por Fluxo

### 7.1 Busca → Hotel → Quarto (viajante)

1. **Home (`/`)**: busca por destino com checkin, checkout, hóspedes e nº de quartos. Esses 4 parâmetros são **propagados por toda a cadeia** (resultados → página do hotel → página do quarto) via querystring.
2. **Resultados (`/hoteis/search`)**: cards com foto, média de avaliações e melhor oferta.
3. **Página do hotel (`/hoteis/{id}`)**: galeria, comodidades (indicador PET derivado dos quartos), **apenas quartos disponíveis**, filtro client-side por nº de hóspedes (pré-preenchido da busca), avaliações com média e formulário de nova avaliação (logado).
4. **Página do quarto (`/quartos/hotel/{ch}/numero/{n}`)**: comparação de ofertas reais de parceiros com badges, melhor preço destacado na lateral e cálculo `preço × noites`. Alterar checkin/checkout dispara **busca AJAX sem reload** (endpoint JSON `.../ofertas`), com animação de carregamento e campos bloqueados durante a consulta. Defaults sensatos quando o visitante chega sem parâmetros (checkin = hoje+30d, 2 noites, pessoas = capacidade do quarto).

### 7.2 Conta do viajante

- Cadastro com validação, login/logout, edição de perfil e troca de senha, exclusão de conta.
- **Favoritos**: toggle por AJAX nos cards de oferta; página `/favoritos` lista as ofertas salvas com a foto real do hotel.
- **Avaliações**: nota + comentário na página do hotel; média recalculada.

### 7.3 Painel do hoteleiro

- Login dedicado, dashboard, e três áreas de gestão:
- **Meu hotel** (`/hoteis/gerenciar-hotel`): edição de dados e endereço (com geocodificação automática), galeria de fotos com upload múltiplo (limite 10MB/arquivo), preview pré-envio com remoção individual, exclusão por foto das já salvas, overlay de carregamento no submit e toasts de sucesso/erro.
- **Quartos** (`/gerenciar-quartos`): listagem real dos quartos do hotel; formulário único em dois modos (criar/editar) com o mesmo padrão de galeria de fotos do hotel; checkboxes `Aceita PET`/`Disponível` e o check **"Em manutenção"** (força indisponibilidade; o JS trava o checkbox `Disponível` enquanto marcado).
- **Avaliações** (`/gerenciar-avaliacoes`): visualização e resposta às avaliações do hotel (endpoint JSON).
- A edição de imagens é **sempre aditiva** (novas fotos anexam às existentes); remoção só pelo botão "×" por foto.

## 8. Frontend

- Um único `static/js/script.js` com blocos **genéricos ativados por convenção de IDs** (reutilizados entre páginas): preview de upload (`#imagemArquivos`/`#fileNames`/`#uploadPreviewStrip`), toasts (`#toastContainer` lendo flash attributes `mensagem`/`erro`), overlay de carregamento (`#pageLoadingOverlay`), filtro de hóspedes (`#filtroHospedes`), interação manutenção↔disponível, e o refresh AJAX de ofertas.
- Templates Thymeleaf com binding manual (`name` + `th:value`/`th:checked`), sem `th:field`. Dois padrões estruturais obrigatórios documentados no [guia](../GUIA_DESENVOLVIMENTO.md): checkbox+hidden para booleans `NOT NULL` e proibição de `<form>` aninhado.
