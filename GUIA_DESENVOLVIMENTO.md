# Guia Prático de Desenvolvimento - BahVago

Este guia é para quem vai **escrever código** no projeto. Para entender a arquitetura e as funcionalidades, leia antes [docs/ARQUITETURA.md](docs/ARQUITETURA.md); para a referência de rotas e configuração, [docs/DOCUMENTACAO.md](docs/DOCUMENTACAO.md).

## 🎯 Checklist Rápido para Começar

```bash
# 1. Clonar o repositório
git clone https://github.com/fabriciobarbosaviegas/BahVago
cd BahVago

# 2. Iniciar o MySQL (primeira subida executa DB/migrations/bahvagoBD.sql)
cd DB && docker-compose up -d && cd ..

# 3. Compilar e executar
mvn clean compile
mvn spring-boot:run

# 4. Acessar: http://localhost:8080
# API externa de ofertas em http://localhost:8001 para a página do quarto
https://github.com/fabriciobarbosaviegas/Trivago-Scrapper
```

## 🥇 Regra de Ouro: nunca toque nas migrations

`DB/migrations/bahvagoBD.sql` **não pode ser alterado** — nem para adicionar coluna, nem para "só ajustar um tipo". O schema é fixo. Quando uma funcionalidade nova parecer pedir uma coluna:

1. **Reaproveite colunas existentes** — ex.: a listagem pública esconde quartos via a coluna `Disponivel` que já existia.
2. **Estado auxiliar em memória** — ex.: `ManutencaoQuartoService` guarda "quarto em manutenção" num `Set` em memória (distinto de "indisponível", que é persistido); `OfertaExternaService` mantém cache de ofertas com TTL num `ConcurrentHashMap`. Trade-off aceito: esse estado se perde no restart.
3. **Calcule em tempo de requisição** — ex.: ofertas de parceiros nunca são persistidas; são buscadas ao vivo da API externa.

`spring.jpa.hibernate.ddl-auto=update` está ligado, mas **não conte com o Hibernate para criar/alterar schema**: as entidades devem espelhar exatamente as tabelas da migration.

## 🏗️ Fluxo para Adicionar um Recurso

Ordem de trabalho: **model → repository → service → controller → template → SecurityConfig** (se a rota for administrativa, veja a seção de segurança abaixo).

As entidades usam Lombok (`@Data @Builder @AllArgsConstructor @NoArgsConstructor`) e mapeiam os nomes reais das colunas da migration (PascalCase, ex.: `@Column(name = "CodigoHotel")`). Chaves compostas usam `@IdClass` (`QuartoId`, `LocalizacaoId`).

### Exemplo real: entidade com chave composta e coleção de imagens

```java
@Entity
@Table(name = "Quarto")
@IdClass(QuartoId.class)
public class Quarto {
    @Id @Column(name = "Numero") private Integer numero;
    @Id @Column(name = "CodigoHotel") private Long codigoHotel;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ImagemQuarto", joinColumns = {
        @JoinColumn(name = "Numero", referencedColumnName = "Numero"),
        @JoinColumn(name = "CodigoHotel", referencedColumnName = "CodigoHotel")
    })
    @Column(name = "Url")
    @Builder.Default
    private List<String> imagens = new ArrayList<>();
    // ...
}
```

### Padrão de controller administrativo (hoteleiro)

Todo endpoint que **muta** um recurso do hoteleiro segue este esqueleto — repare na verificação de propriedade, que é obrigatória (a regra de papel no `SecurityConfig` não basta, pois qualquer hoteleiro autenticado passa por ela):

```java
@PostMapping("/atualizar/{codigoHotel}/{numero}")
public String atualizarQuarto(@PathVariable Long codigoHotel, ...,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
    verificarPropriedadeHotel(codigoHotel, usuarioAutenticado(authentication));
    // ... lógica ...
    redirectAttributes.addFlashAttribute("mensagem", "Quarto atualizado com sucesso!");
    return "redirect:/gerenciar-quartos";
}
```

`mensagem`/`erro` como flash attributes viram **toasts** automaticamente em qualquer página que tenha `<div class="toast-container" id="toastContainer">` (o JS genérico em `script.js` cuida do resto).

## 🛡️ Segurança: armadilhas conhecidas

### Ordem das regras importa (bug já ocorreu 2×)

`authorizeHttpRequests` usa **a primeira regra que casar**, não a mais específica. As rotas de hoteleiro (`/hoteis/gerenciar-hotel`, `/quartos/atualizar/**`, etc.) estão declaradas **antes** dos `permitAll()` amplos de `/hoteis/**` e `/quartos/**` em `SecurityConfig`. Ao criar uma rota administrativa nova sob esses prefixos, **adicione-a ao bloco `hasRole("HOTELEIRO")` que vem primeiro** — senão ela nasce pública silenciosamente.

### Propriedade do recurso

Papel ≠ dono. Sempre chame `verificarPropriedade*` (compara o CPF do hoteleiro autenticado com o CPF dono do hotel) em todo GET de formulário administrativo e todo POST/GET mutador. Nunca confie em `cpf` vindo do formulário — force-o do usuário autenticado no servidor.

### CSRF em AJAX

O token CSRF fica no cookie `XSRF-TOKEN` (legível por JS). POSTs via `fetch` devem enviá-lo no header `X-XSRF-TOKEN` — veja os exemplos existentes em `script.js` (favoritos, avaliações).

## 📝 Armadilhas de HTML/Thymeleaf (todas já causaram bugs reais)

### 1. Checkbox desmarcado não é enviado

Um checkbox HTML desmarcado **some do POST**, e campos `Boolean` `NOT NULL` estouram `DataIntegrityViolationException`. Padrão do projeto — checkbox seguido de hidden com o mesmo `name` (a ordem importa: `getParameter()` retorna o **primeiro** valor):

```html
<input type="checkbox" name="disponivel" value="true" th:checked="${...}">
<input type="hidden" name="disponivel" value="false">
```

### 2. Nunca aninhe `<form>` dentro de `<form>`

HTML5 proíbe e o navegador corrompe silenciosamente: o `<form>` interno é descartado, mas seu `</form>` **fecha o form externo mais cedo**, deixando os campos/botões seguintes órfãos. Sintoma real: "salvar não funciona e o botão de apagar imagem submete o form errado". Padrão correto (ver `gerenciar-hotel.html` e `novo-quarto.html`): a galeria "Fotos atuais" com seus mini-forms de exclusão é um `<div>` **irmão, antes** do form principal.

### 3. Edição de imagens é sempre aditiva

Nos POSTs de atualização de hotel/quarto, as imagens existentes são **preservadas e as novas anexadas** (nunca `clear()` + substituir). Remoção de foto é exclusivamente pelos endpoints dedicados `.../imagem/remover`. Mantenha esse contrato ao mexer nesses fluxos.

### 4. IDs compartilhados do JS de upload

O preview de upload com remoção pré-envio em `script.js` é genérico e se ativa por IDs: `#imagemArquivos` (input file), `#fileNames` (resumo) e `#uploadPreviewStrip` (galeria). Para ganhar esse comportamento numa página nova, basta usar esses mesmos IDs.

## 🌐 Integrações HTTP (RestClient)

### API externa de ofertas (`OfertaExternaService`)

- **Construa o `RestClient` com `SimpleClientHttpRequestFactory`.** A factory padrão (JDK HttpClient, no Boot 3.2.0) descarta silenciosamente corpos POST pequenos → a API responde `422 field required, loc: body`. A explícita bufferiza o corpo e envia `Content-Length`.
- Toda chamada externa é envolta em try/catch: falha → log de warning + lista vazia, a página nunca quebra.
- Cache em memória por chave `(hotel, checkin, checkout, quartos, pessoas)` com TTL configurável (`api.hoteis.ofertas.cache-ttl-segundos`, padrão 300s).

### Jackson: `@JsonAlias` vs `@JsonProperty`

A API externa fala snake_case (`preco_noite`); nosso endpoint JSON interno devolve camelCase para o JS. Use **`@JsonAlias("preco_noite")`** (só leitura) nos DTOs. `@JsonProperty` renomeia **nos dois sentidos** e quebra a serialização do nosso próprio endpoint (bug real: "undefined/noite" no frontend).

### Geocodificação (`GeocodingService`)

Nominatim/OpenStreetMap exige header `User-Agent` descritivo (configurado em `geocoding.nominatim.user-agent`). Coordenadas são convertidas para **micrograus inteiros** (`grau × 1.000.000`), o formato das colunas `Latitude`/`Longitude`. O serviço nunca lança exceção — retorna `Optional.empty()` e o chamador decide o fallback.

## 📤 Upload de arquivos

- Limites configurados em `application.properties` (`max-file-size=10MB`, `max-request-size=40MB`) — o padrão do Spring (1MB) rejeita fotos reais. Estouro cai no `@ExceptionHandler(MaxUploadSizeExceededException.class)` do `HotelController`, que vira toast de erro.
- `FileStorageService.salvarArquivos(files, "hoteis"|"quartos")` grava em `uploads/<subpasta>/` com prefixo de timestamp e retorna as URLs `/uploads/...` (servidas pelo `WebConfig`).

## 🐛 Debugging

```properties
# application.properties — já ligado:
logging.level.com.bahvago=DEBUG      # inclui request/response da API de ofertas
# Para ver SQL:
spring.jpa.show-sql=true
```

## 🧪 Verificando templates sem subir a aplicação

Padrão usado no projeto para pegar regressões de template offline: um `main` standalone com `SpringTemplateEngine` + `FileTemplateResolver` apontado para `src/main/resources/templates/`, renderizando o template com dados de mock e fazendo asserts no HTML gerado. Dois detalhes de plumbing:

- Sobrescreva `StandardLinkBuilder.computeContextPath` para retornar `""` — senão links `@{/...}` lançam `TemplateProcessingException` fora de um contexto web.
- O Thymeleaf **não valida aninhamento HTML5** — ele renderiza `<form>` aninhado sem reclamar. Inclua uma checagem estrutural própria (varredura balanceando `<form>`/`</form>`) nos asserts.

## 🚨 Erros Comuns

| Erro | Solução |
|------|---------|
| "Table not found" | MySQL fora do ar → `cd DB && docker-compose up -d` |
| Schema desatualizado/estranho | O SQL só roda na 1ª criação do volume: `docker-compose down -v && docker-compose up -d` recria do zero (apaga os dados!) |
| `422 field required, loc: body` na API de ofertas | RestClient sem `SimpleClientHttpRequestFactory` (ver seção acima) |
| `DataIntegrityViolationException` em coluna boolean | Checkbox desmarcado não enviado → padrão checkbox + hidden |
| Campo JSON `undefined` no frontend | `@JsonProperty` onde deveria ser `@JsonAlias` |
| Upload "não funciona" sem erro claro | Arquivo > limite de multipart → conferir `spring.servlet.multipart.*` |
| Rota administrativa acessível deslogado | Regra criada depois do `permitAll()` amplo → mover para o bloco `hasRole("HOTELEIRO")` |
| Form salva errado / botão submete outra coisa | `<form>` aninhado → mover mini-forms para fora do form principal |
