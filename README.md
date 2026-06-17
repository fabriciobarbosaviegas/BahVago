# BahVago - Plataforma de Hospedagem

Projeto acadêmico para a disciplina de Desenvolvimento de Software do curso de Ciência da Computação da UFPEL.

## 🏗️ Arquitetura

O projeto foi adaptado para seguir a **arquitetura MVC (Model-View-Controller) com Spring Boot**, permitindo uma melhor separação de responsabilidades e escalabilidade.

### Estrutura do Projeto

```
src/
├── main/
│   ├── java/com/bahvago/
│   │   ├── BahvagoApplication.java          # Classe principal da aplicação
│   │   ├── controller/                       # Controllers MVC
│   │   │   ├── HomeController.java
│   │   │   ├── UsuarioController.java
│   │   │   ├── HotelController.java
│   │   │   ├── QuartoController.java
│   │   │   ├── ReservaController.java
│   │   │   └── FavoritoController.java
│   │   ├── model/                            # Entidades JPA
│   │   │   ├── Usuario.java
│   │   │   ├── Hotel.java
│   │   │   ├── Quarto.java
│   │   │   ├── Reserva.java
│   │   │   ├── Avaliacao.java
│   │   │   └── Favorito.java
│   │   ├── repository/                       # Repositórios (Data Access Layer)
│   │   │   ├── UsuarioRepository.java
│   │   │   ├── HotelRepository.java
│   │   │   └── ...
│   │   ├── service/                          # Services (Business Logic)
│   │   │   ├── UsuarioService.java
│   │   │   ├── HotelService.java
│   │   │   └── ...
│   │   └── config/                           # Configurações
│   │       └── SecurityConfig.java
│   ├── resources/
│   │   ├── application.properties             # Configuração da aplicação
│   │   ├── templates/                         # Templates Thymeleaf (Views)
│   │   │   ├── index.html
│   │   │   ├── login.html
│   │   │   ├── hotel.html
│   │   │   └── ...
│   │   └── static/                            # Recursos estáticos
│   │       ├── css/style.css
│   │       └── js/script.js
└── test/java/com/bahvago/

pom.xml                                        # Dependências Maven
```

## 📋 Dependências Principais

- **Spring Boot 3.2.0** - Framework web
- **Spring Data JPA** - Acesso a dados
- **Thymeleaf** - Template engine
- **Spring Security** - Autenticação e segurança
- **MySQL** - Banco de dados
- **Lombok** - Redução de boilerplate

## 🚀 Como Executar

### Pré-requisitos

- Java 17+
- Maven 3.6+
- MySQL 8+

### 1. Configurar o Banco de Dados

Inicie o container MySQL:

```bash
cd DB
docker-compose up -d
```

Isso criará um banco de dados MySQL com o nome `bahvagoBD` e usuário `root` com senha `root`.

### 2. Configurar a Aplicação

Edite `src/main/resources/application.properties` se necessário para alterar as credenciais do banco de dados.

### 3. Compilar e Executar

```bash
# Compilar
mvn clean compile

# Executar
mvn spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8080`

### Alternativa: IDE (Eclipse, IntelliJ, VS Code)

1. Importe o projeto como Maven Project
2. Clique em Run

## 📚 Camadas da Arquitetura MVC

### 1. **Model (Camada de Dados)**
- **Entidades JPA**: Define a estrutura dos dados (`Usuario.java`, `Hotel.java`, etc.)
- **Repositories**: Interfaces que herdam de `JpaRepository` para acesso ao banco de dados

### 2. **View (Camada de Apresentação)**
- **Templates Thymeleaf**: Arquivos HTML em `src/main/resources/templates/`
- **Static Files**: CSS, JavaScript e imagens em `src/main/resources/static/`

### 3. **Controller (Camada de Controle)**
- **Controllers**: Classes que tratam requisições HTTP e retornam views
- Exemplo: `HotelController` mapeia requisições para `/hoteis`

## 🔗 Rotas Principais

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/` | Página inicial |
| GET | `/login` | Página de login |
| GET | `/cadastro` | Página de cadastro |
| GET | `/hoteis` | Listar hotéis |
| GET | `/hoteis/search?termo=...` | Buscar hotéis |
| GET | `/hoteis/{id}` | Detalhes do hotel |
| GET | `/quartos/hotel/{idHotel}` | Quartos de um hotel |
| GET | `/favoritos/usuario/{idUsuario}` | Meus favoritos |
| POST | `/reservas/criar` | Criar reserva |
| GET | `/usuarios/perfil/{id}` | Perfil do usuário |

## 🛡️ Segurança

A aplicação inclui configuração básica de segurança com Spring Security:
- Autenticação de usuários
- Rotas públicas e protegidas
- Criptografia de senhas (BCrypt)

## 📝 Exemplo de Fluxo

### Criação de um Hotel

1. **View** (`gerenciar-hotel.html`): Usuário preenche formulário
2. **Controller** (`HotelController.criarHotel()`): Recebe POST e valida dados
3. **Service** (`HotelService.criarHotel()`): Lógica de negócio
4. **Repository** (`HotelRepository.save()`): Persiste no banco
5. **Response**: Redireiona para a página do hotel criado

## 🗄️ Banco de Dados

O banco possui as seguintes tabelas:

- `usuarios` - Usuários e hoteleiros
- `hoteis` - Informações dos hotéis
- `quartos` - Quartos dos hotéis
- `reservas` - Reservas de usuários
- `avaliacoes` - Avaliações dos hotéis
- `favoritos` - Hotéis favoritados

## 📊 Migrations

As migrations SQL estão em `DB/migrations/bahvagoBD.sql` e são executadas automaticamente pelo Hibernate.

## 🧪 Testes

Para executar testes unitários:

```bash
mvn test
```

## 📦 Build para Produção

```bash
mvn clean package -DskipTests
java -jar target/bahvago-1.0.0.jar
```