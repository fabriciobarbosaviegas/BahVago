# BahVago - Plataforma de Hospedagem

Projeto acadêmico para a disciplina de Desenvolvimento de Software do curso de Ciência da Computação da UFPEL.

O BahVago é um comparador de hospedagens no estilo Trivago: o viajante busca hotéis, navega pelos quartos e compara ofertas de diferentes parceiros (obtidas em tempo real de uma API externa de ofertas), podendo salvar favoritos e avaliar hotéis. Hoteleiros têm um painel administrativo próprio para gerenciar hotel, quartos, fotos e avaliações.

## 📖 Documentação

| Documento | Conteúdo |
|-----------|----------|
| [docs/ARQUITETURA.md](docs/ARQUITETURA.md) | Arquitetura, funcionalidades, modelo de dados e integrações externas |
| [docs/DOCUMENTACAO.md](docs/DOCUMENTACAO.md) | Referência completa: rotas, níveis de acesso, configuração e convenções de frontend |
| [GUIA_DESENVOLVIMENTO.md](GUIA_DESENVOLVIMENTO.md) | Guia prático para desenvolver no projeto (padrões, armadilhas conhecidas, verificação) |

## 🧰 Stack

- **Java 17** + **Spring Boot 3.2.0** (Spring MVC)
- **Thymeleaf** — views renderizadas no servidor
- **Spring Data JPA / Hibernate** — persistência
- **Spring Security 6** — autenticação por formulário, papéis `USER`/`HOTELEIRO`, BCrypt, CSRF via cookie
- **MySQL 8** — banco de dados (container Docker)
- **Lombok** — redução de boilerplate nas entidades
- **RestClient (Spring 6.1)** — integrações HTTP (API externa de ofertas e Nominatim/OpenStreetMap)

## 🚀 Como Executar

### Pré-requisitos

- Java 17+
- Maven 3.6+
- Docker + Docker Compose (para o MySQL)
- (Opcional, para ofertas reais) a API externa de ofertas rodando em `http://localhost:8001`

### API de ofertas (externa)
[https://github.com/fabriciobarbosaviegas/Trivago-Scrapper](https://github.com/fabriciobarbosaviegas/Trivago-Scrapper)

### 1. Banco de dados

```bash
cd DB
docker-compose up -d
cd ..
```

Isso constrói e sobe o container `bahvago-mysql` (MySQL 8, porta `3306`, banco `bahvagoBD`, usuário `root`, senha `root`). **Na primeira inicialização do volume**, o script `DB/migrations/bahvagoBD.sql` é executado automaticamente (via `docker-entrypoint-initdb.d`), criando o schema completo e dados de exemplo.

### 2. Aplicação

```bash
mvn clean compile
mvn spring-boot:run
```

Acesse `http://localhost:8080`. As credenciais do banco e demais configurações estão em `src/main/resources/application.properties`.

### 3. (Opcional) API externa de ofertas

A página do quarto compara preços de parceiros consultando uma API externa (`POST /api/v1/hoteis/ofertas`), configurada por padrão em `http://localhost:8001` (propriedade `api.hoteis.ofertas.url`). Se ela estiver fora do ar, a aplicação **não quebra** — a página exibe o estado vazio "Nenhuma oferta encontrada".

### 4. (Opcional) Povoar quartos a partir da API de ofertas

O script `povoadorDeQuartos.py` consulta a API de ofertas para cada hotel do banco e cria quartos derivados dos nomes de quartos das ofertas reais:

```bash
pip install requests mysql-connector-python
python3 povoadorDeQuartos.py
```

## 🗂️ Estrutura do Projeto

```
BahVago/
├── DB/
│   ├── docker-compose.yml       # Container MySQL
│   ├── Dockerfile               # Imagem MySQL + migration embutida
│   └── migrations/bahvagoBD.sql # Schema + seeds (NUNCA ALTERAR)
├── docs/
│   ├── ARQUITETURA.md           # Arquitetura e funcionalidades
│   └── DOCUMENTACAO.md          # Referência de rotas e configuração
├── povoadorDeQuartos.py         # Seeder de quartos via API de ofertas
├── uploads/                     # Imagens enviadas pelos usuários (servidas em /uploads/**)
├── pom.xml
└── src/main/
    ├── java/com/bahvago/
    │   ├── config/              # SecurityConfig, WebConfig
    │   ├── controller/          # Controllers MVC (+ controller/dto: forms e views)
    │   ├── dto/                 # DTOs da integração com a API externa de ofertas
    │   ├── model/               # Entidades JPA (+ classes de chave composta)
    │   ├── repository/          # Spring Data JPA
    │   ├── service/             # Regras de negócio e integrações
    │   └── util/                # OfertaMatcher (casamento quarto ↔ oferta)
    └── resources/
        ├── application.properties
        ├── templates/           # Views Thymeleaf
        └── static/              # css/style.css, js/script.js
```

## ✨ Funcionalidades

**Viajante (público / autenticado):**
- Busca de hotéis por nome/cidade com datas, hóspedes e nº de quartos propagados por todo o fluxo
- Página do hotel com quartos disponíveis, filtro por nº de hóspedes, avaliações e média
- Página do quarto com comparação de ofertas reais de parceiros (API externa), atualização ao vivo ao mudar as datas, cache de resultados e destaque de melhor preço
- Cadastro/login, perfil (edição de dados e senha), favoritos com fotos reais, escrever avaliações

**Hoteleiro (painel administrativo):**
- Dashboard e login próprio (`/login-hoteleiro`)
- Gerenciar hotel: dados, endereço com **geocodificação automática via OpenStreetMap/Nominatim**, galeria de fotos (upload múltiplo com preview, remoção por foto, toasts de feedback)
- Gerenciar quartos: criar/editar/excluir, galeria de fotos no mesmo padrão, flag "Em manutenção", controle de disponibilidade
- Responder avaliações
- Segurança: todas as rotas administrativas exigem papel `HOTELEIRO` **e** verificação de propriedade (um hoteleiro não edita recursos de outro)

Detalhes de cada fluxo em [docs/ARQUITETURA.md](docs/ARQUITETURA.md).

## 🔗 Principais Rotas

| Método | Rota | Acesso | Descrição |
|--------|------|--------|-----------|
| GET | `/` | público | Home com formulário de busca |
| GET | `/hoteis/search?termo=...` | público | Busca de hotéis |
| GET | `/hoteis/{id}` | público | Página do hotel |
| GET | `/quartos/hotel/{codigoHotel}/numero/{numero}` | público | Página do quarto com ofertas |
| GET | `/favoritos` | autenticado | Ofertas salvas do usuário |
| GET | `/usuarios/perfil` | autenticado | Perfil do usuário |
| GET | `/dashboard` | hoteleiro | Painel administrativo |
| GET | `/hoteis/gerenciar-hotel` | hoteleiro | Edição do hotel |
| GET | `/gerenciar-quartos` | hoteleiro | Listagem/gestão de quartos |

A tabela completa de rotas, com todos os endpoints JSON e administrativos, está em [docs/DOCUMENTACAO.md](docs/DOCUMENTACAO.md).

## 🗄️ Banco de Dados

Tabelas criadas por `DB/migrations/bahvagoBD.sql`: `Localizacao`, `CriterioBusca`, `Usuario`, `HotelEstatisticas`, `ImagemHotel`, `Quarto`, `ImagemQuarto`, `Oferta`, `Avaliacao`, `Salva`, `Reserva`, `HotelFavorito`.

- **Latitude/Longitude são `INT`** em micrograus (`grau X 1.000.000`) e formam a chave primária de `Localizacao`.
- **`Quarto` tem chave composta** (`Numero`, `CodigoHotel`) — não existe id único de quarto.
- A entidade `Hotel` mapeia a tabela **`HotelEstatisticas`**.
- Imagens de hotel/quarto ficam em tabelas próprias (`ImagemHotel`/`ImagemQuarto`), mapeadas como `@ElementCollection`.

## 📦 Build para Produção

```bash
mvn clean package -DskipTests
java -jar target/bahvago-1.0.0.jar
```
