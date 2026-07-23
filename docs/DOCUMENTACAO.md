# Documentação de Referência — BahVago

Referência completa de rotas, níveis de acesso, configuração e convenções. Visão conceitual em [ARQUITETURA.md](ARQUITETURA.md); guia de código em [../GUIA_DESENVOLVIMENTO.md](../GUIA_DESENVOLVIMENTO.md).

## 1. Rotas

Níveis de acesso:
- **público** — sem login
- **autenticado** — qualquer usuário logado
- **hoteleiro** — exige `ROLE_HOTELEIRO` (via `SecurityConfig`) **e**, nos endpoints marcados com 🔒, verificação de propriedade no controller (o hoteleiro só acessa recursos do próprio hotel)

### 1.1 Páginas públicas e autenticação

| Método | Rota | Acesso | Descrição |
|--------|------|--------|-----------|
| GET | `/` | público | Home com formulário de busca (destino, checkin, checkout, hóspedes, quartos) |
| GET | `/login` | público | Login do viajante |
| POST | `/login` | público | Processamento do login (parâmetros `email`, `senha`) |
| GET | `/login-hoteleiro` | público | Login do hoteleiro (sucesso redireciona a `/dashboard`; usuário sem papel volta com `?error`) |
| POST | `/logout` | público | Logout (invalida sessão, apaga `JSESSIONID`) |
| GET | `/cadastro` · `/usuarios/cadastro` | público | Formulário de cadastro |
| POST | `/usuarios/cadastro` | público | Cria usuário (`CadastroUsuarioForm`, senha BCrypt) |

### 1.2 Hotéis e quartos (vitrine pública)

| Método | Rota | Acesso | Descrição |
|--------|------|--------|-----------|
| GET | `/hoteis` | público | Lista todos os hotéis |
| GET | `/hoteis/search` | público | Busca por `termo` (nome/cidade); aceita e propaga `checkin`, `checkout`, `pessoas`, `quartos` |
| GET | `/hoteis/cidade/{cidade}` | público | Hotéis por cidade |
| GET | `/hoteis/{id}` | público | Página do hotel: galeria, avaliações + média, **somente quartos disponíveis**, filtro de hóspedes |
| POST | `/hoteis/{id}/avaliacoes` | usuário logado* | JSON — registra avaliação (nota + comentário) |
| GET | `/quartos/hotel/{codigoHotel}/numero/{numero}` | público | Página do quarto com ofertas externas; aceita `checkin`, `checkout`, `pessoas`, `quartos` (defaults aplicados se ausentes) |
| GET | `/quartos/hotel/{codigoHotel}/numero/{numero}/ofertas` | público | JSON — mesmas ofertas, usado pelo refresh AJAX ao mudar datas. Resposta: `{ofertas, noites, checkin, checkout, pessoas}` |

\* liberado na camada de segurança (`/hoteis/**` é público), identidade do avaliador resolvida no controller.

### 1.3 Conta do usuário

| Método | Rota | Acesso | Descrição |
|--------|------|--------|-----------|
| GET | `/usuarios/perfil` | autenticado | Página de perfil |
| GET | `/usuarios/perfil/{id}` | autenticado | Redirect legado → `/usuarios/perfil` |
| POST | `/usuarios/perfil` | autenticado | Atualiza dados (`AtualizarPerfilForm`) |
| POST | `/usuarios/perfil/senha` | autenticado | Troca senha (`AlterarSenhaForm`) |
| POST | `/usuarios/deletar` | autenticado | Exclui a própria conta |

### 1.4 Favoritos

| Método | Rota | Acesso | Descrição |
|--------|------|--------|-----------|
| GET | `/favoritos` | autenticado | Página com as ofertas salvas (com foto do hotel) |
| GET | `/favoritos/usuario/{cpf}` | autenticado | Redirect legado → `/favoritos` |
| GET | `/favoritos/ids` | autenticado | JSON — códigos de oferta salvos (para pintar os corações nos cards) |
| POST | `/favoritos/toggle/{codigoOferta}` | autenticado | JSON — alterna favorito |
| DELETE | `/favoritos/remover/{codigoOferta}` | autenticado | JSON — remove favorito |

### 1.5 Painel do hoteleiro

| Método | Rota | Acesso | Descrição |
|--------|------|--------|-----------|
| GET | `/dashboard` | hoteleiro | Painel inicial |
| GET | `/hoteis/gerenciar-hotel` | hoteleiro | Formulário de edição do hotel do hoteleiro logado |
| POST | `/hoteis/criar` | hoteleiro | Cria hotel (CPF do dono forçado do usuário autenticado) |
| POST | `/hoteis/atualizar/{id}` | hoteleiro 🔒 | Atualiza dados/endereço (geocodifica via Nominatim) e **anexa** novas imagens (multipart `imagemArquivos` e/ou textarea `imagensUrls`) |
| POST | `/hoteis/atualizar/{id}/imagem/remover` | hoteleiro 🔒 | Remove uma imagem específica (parâmetro `url`) |
| GET | `/hoteis/deletar/{id}` | hoteleiro 🔒 | Exclui o hotel |
| GET | `/gerenciar-quartos` | hoteleiro | Lista os quartos do hotel do hoteleiro |
| GET | `/quartos/hotel/{codigoHotel}` | hoteleiro 🔒 | Formulário de novo quarto |
| GET | `/quartos/hotel/{codigoHotel}/numero/{numero}/editar` | hoteleiro 🔒 | Formulário de edição (mesmo template, modo edição) |
| POST | `/quartos/criar` | hoteleiro 🔒 | Cria quarto; aceita `manutencao=true` (força `disponivel=false`) |
| POST | `/quartos/atualizar/{codigoHotel}/{numero}` | hoteleiro 🔒 | Atualiza quarto; imagens **sempre aditivas**; sincroniza flag de manutenção |
| POST | `/quartos/atualizar/{codigoHotel}/{numero}/imagem/remover` | hoteleiro 🔒 | Remove uma imagem específica do quarto |
| GET | `/quartos/deletar/{codigoHotel}/{numero}` | hoteleiro 🔒 | Exclui o quarto |
| GET | `/gerenciar-avaliacoes` | hoteleiro | Avaliações do hotel do hoteleiro |
| POST | `/avaliacoes/{id}/responder` | hoteleiro* | JSON — grava a resposta do hoteleiro (`{"resposta": "..."}`) |

\* usado pela página `/gerenciar-avaliacoes`; a rota em si não está no bloco `hasRole` do `SecurityConfig`.

### 1.6 Recursos estáticos

| Rota | Descrição |
|------|-----------|
| `/css/**`, `/js/**`, `/img/**` | `src/main/resources/static/` |
| `/uploads/**` | Diretório `uploads/` no disco (mapeado pelo `WebConfig`) — fotos enviadas por hoteleiros |

### 1.7 Observações

- `estatisticas.html` existe em `templates/` (e `/estatisticas` consta no `SecurityConfig`), mas **nenhum controller mapeia essa rota atualmente**.
- A tabela `Reserva` existe no banco, mas `ReservaController`/`ReservaService` estão vazios — o fluxo de reservas não foi implementado na camada web.

## 2. Configuração (`application.properties`)

| Propriedade | Padrão | Descrição |
|-------------|--------|-----------|
| `server.port` | `8080` | Porta da aplicação |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/bahvagoBD?...` | Conexão MySQL (container `bahvago-mysql`, root/root) |
| `spring.jpa.hibernate.ddl-auto` | `update` | Presente, mas o schema é definido pela migration — não conte com o Hibernate para DDL |
| `spring.servlet.multipart.max-file-size` | `10MB` | Limite por arquivo de upload (padrão Spring de 1MB é insuficiente para fotos) |
| `spring.servlet.multipart.max-request-size` | `40MB` | Limite total do request multipart |
| `api.hoteis.ofertas.url` | `http://localhost:8001/api/v1/hoteis/ofertas` | API externa de ofertas de parceiros |
| `api.hoteis.ofertas.cache-ttl-segundos` | `300` | TTL do cache em memória de ofertas |
| `geocoding.nominatim.url` | `https://nominatim.openstreetmap.org/search` | Endpoint de geocodificação |
| `geocoding.nominatim.user-agent` | `BahVagoGeoreferenciado/1.0 (...)` | `User-Agent` exigido pelo Nominatim |
| `logging.level.com.bahvago` | `DEBUG` | Inclui log do request/response da API de ofertas |

## 3. Contrato com a API externa de ofertas

**Request** — `POST {api.hoteis.ofertas.url}`:

```json
{
  "hotel": "Nome do Hotel",
  "dataCheckin": "2026-08-21",
  "dataCheckout": "2026-08-23",
  "quartos": 1,
  "pessoas": 2
}
```

**Response** (campos relevantes; a API usa snake_case, mapeado com `@JsonAlias`):

```json
{
  "quantidade_ofertas": 3,
  "ofertas": [
    {
      "parceiro": "Booking.com",
      "botao": "Ver oferta",
      "url": "https://...",
      "quarto": "STANDARD DUAS CAMAS",
      "preco_noite": "R$ 356,00",
      "preco_total": "R$ 712,00",
      "atributos": ["CHAMPION_DEAL"]
    }
  ]
}
```

Os preços são **strings já formatadas** — a aplicação só os parseia para ordenar por preço/noite. O endpoint JSON interno (`.../ofertas`) repassa os mesmos objetos em camelCase (`precoNoite`, `precoTotal`).

## 4. Templates e comportamentos JS

| Template | Página | Comportamentos JS relevantes |
|----------|--------|------------------------------|
| `index.html` | Home/busca | Form GET para `/hoteis/search` com os 4 parâmetros de busca |
| `resultados.html` | Resultados | Preserva parâmetros de busca; corações de favorito via AJAX |
| `hotel.html` | Hotel | Filtro client-side por hóspedes (`#filtroHospedes` + `data-capacidade`); overlay de carregamento ao navegar para o quarto |
| `quarto.html` | Quarto | Refresh AJAX de ofertas ao mudar checkin/checkout (loading + campos bloqueados durante a busca); redirecionamento às URLs dos parceiros |
| `login.html` / `login-hoteleiro.html` / `cadastro.html` | Autenticação | — |
| `perfil.html` | Perfil | — |
| `favoritos.html` | Favoritos | Remoção via AJAX |
| `dashboard.html` | Painel hoteleiro | — |
| `gerenciar-hotel.html` | Meu hotel | Galeria "Fotos atuais" com mini-forms de exclusão (irmãos do form principal); preview de upload com remoção pré-envio; overlay no submit; toasts |
| `gerenciar-quartos.html` | Quartos | Listagem real com editar/excluir; toasts |
| `novo-quarto.html` | Criar/editar quarto | Mesmo padrão de galeria/preview do hotel; checkbox "Em manutenção" trava "Disponível" |
| `gerenciar-avaliacoes.html` | Avaliações | Resposta via `fetch` para `/avaliacoes/{id}/responder` |
| `politicas.html` / `termos.html` | Institucionais | — |
| `estatisticas.html` | — | Sem rota mapeada atualmente |

Blocos genéricos de `script.js` ativados por convenção de IDs (basta usar os IDs para ganhar o comportamento em qualquer página):

| IDs | Comportamento |
|-----|---------------|
| `#imagemArquivos`, `#fileNames`, `#uploadPreviewStrip` | Preview de upload múltiplo com remoção individual antes do envio (reconstrói `input.files` via `DataTransfer`) |
| `#toastContainer` | Toasts auto-dispensáveis alimentados pelos flash attributes `mensagem`/`erro` |
| `#pageLoadingOverlay` | Overlay de carregamento de página |
| `#manutencaoCheckbox`, `#disponivelCheckbox` | Manutenção marcada → desmarca e trava Disponível |
| `#filtroHospedes`, `#roomsListContainer` | Filtro ao vivo de quartos por capacidade |

## 5. Autenticação e CSRF (para chamadas AJAX)

- Sessão via cookie `JSESSIONID`; login por formulário (`POST /login`, campos `email`/`senha`).
- Token CSRF no cookie `XSRF-TOKEN` (não-HttpOnly). Todo POST/DELETE via `fetch` deve incluir o header `X-XSRF-TOKEN` com o valor do cookie — padrão já usado em favoritos e avaliações.

```bash
# Exemplo: fluxo autenticado via cURL
curl -c jar.txt http://localhost:8080/login                      # pega XSRF-TOKEN
TOKEN=$(grep XSRF jar.txt | awk '{print $7}')
curl -b jar.txt -c jar.txt -X POST http://localhost:8080/login \
  -H "X-XSRF-TOKEN: $TOKEN" -d "email=...&senha=..."
```

## 6. Banco de Dados

- Subida: `cd DB && docker-compose up -d` (container `bahvago-mysql`, volume `mysql_data`).
- O schema (`DB/migrations/bahvagoBD.sql`) roda **apenas na primeira criação do volume**. Para recriar do zero: `docker-compose down -v && docker-compose up -d` (⚠️ apaga todos os dados).
- **Regra de ouro: o arquivo de migration nunca é alterado.** Tabelas e particularidades do schema estão detalhadas em [ARQUITETURA.md §3](ARQUITETURA.md#3-modelo-de-dados).
- Seeder opcional de quartos: `python3 povoadorDeQuartos.py` (requer a API de ofertas em `localhost:8001` e acesso ao MySQL).
