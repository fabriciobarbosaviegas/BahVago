# Guia Prático de Desenvolvimento - BahVago

## 🎯 Checklist Rápido para Começar

```bash
# 1. Clonar o repositório
git clone <seu-repo>

# 2. Entrar na pasta
cd BahVago

# 3. Iniciar o MySQL
cd DB && docker-compose up -d

# 4. Volta para raiz
cd ..

# 5. Compilar
mvn clean compile

# 6. Executar
mvn spring-boot:run

# 7. Acessar
# Abra o navegador: http://localhost:8080
```

## 🏗️ Como Adicionar um Novo Recurso

Exemplo: **Adicionar avaliação de hotel**

### Passo 1: Criar a Entidade (Model)

`src/main/java/com/bahvago/model/Avaliacao.java`

```java
@Entity
@Table(name = "avaliacoes")
public class Avaliacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "id_hotel", nullable = false)
    private Long idHotel;
    
    @Column(nullable = false)
    private Integer nota;  // 1 a 5
    
    @Column(columnDefinition = "TEXT")
    private String comentario;
}
```

### Passo 2: Criar o Repository

`src/main/java/com/bahvago/repository/AvaliacaoRepository.java`

```java
@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    List<Avaliacao> findByIdHotel(Long idHotel);
    
    @Query("SELECT AVG(a.nota) FROM Avaliacao a WHERE a.idHotel = ?1")
    Double calcularMedia(Long idHotel);
}
```

### Passo 3: Criar o Service

`src/main/java/com/bahvago/service/AvaliacaoService.java`

```java
@Service
public class AvaliacaoService {
    @Autowired
    private AvaliacaoRepository avaliacaoRepository;
    
    public Avaliacao criarAvaliacao(Avaliacao avaliacao) {
        return avaliacaoRepository.save(avaliacao);
    }
    
    public List<Avaliacao> buscarPorHotel(Long idHotel) {
        return avaliacaoRepository.findByIdHotel(idHotel);
    }
    
    public Double calcularMedia(Long idHotel) {
        return avaliacaoRepository.calcularMedia(idHotel);
    }
}
```

### Passo 4: Criar o Controller

`src/main/java/com/bahvago/controller/AvaliacaoController.java`

```java
@Controller
@RequestMapping("/avaliacoes")
public class AvaliacaoController {
    @Autowired
    private AvaliacaoService avaliacaoService;
    
    @PostMapping("/criar")
    public String criarAvaliacao(@ModelAttribute Avaliacao avaliacao, 
                                 @RequestParam Long idHotel,
                                 RedirectAttributes redirectAttributes) {
        avaliacao.setIdHotel(idHotel);
        avaliacaoService.criarAvaliacao(avaliacao);
        redirectAttributes.addFlashAttribute("mensagem", "Avaliação enviada!");
        return "redirect:/hoteis/" + idHotel;
    }
}
```

### Passo 5: Criar a View (Template Thymeleaf)

`src/main/resources/templates/avaliar.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Avaliar Hotel</title>
    <link rel="stylesheet" href="/css/style.css">
</head>
<body>
    <div class="container">
        <h2>Avaliar Hotel</h2>
        <form method="post" th:action="@{/avaliacoes/criar}" class="form-group">
            <input type="hidden" name="idHotel" th:value="${idHotel}">
            
            <div class="form-group">
                <label for="nota">Nota (1-5):</label>
                <select id="nota" name="nota" required>
                    <option value="">Selecione...</option>
                    <option value="1">⭐ Ruim</option>
                    <option value="2">⭐⭐ Fraco</option>
                    <option value="3">⭐⭐⭐ Regular</option>
                    <option value="4">⭐⭐⭐⭐ Bom</option>
                    <option value="5">⭐⭐⭐⭐⭐ Excelente</option>
                </select>
            </div>
            
            <div class="form-group">
                <label for="comentario">Comentário:</label>
                <textarea id="comentario" name="comentario" rows="4"></textarea>
            </div>
            
            <button type="submit" class="btn-submit">Enviar Avaliação</button>
        </form>
    </div>
</body>
</html>
```

## 📝 Exemplos de Consultas Comuns

### Buscar um objeto por ID

```java
// No Repository
Optional<Hotel> hotel = hotelRepository.findById(1L);

// No Service
public Hotel buscarPorId(Long id) {
    return hotelRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Hotel não encontrado"));
}

// No Controller
@GetMapping("/{id}")
public String detalheHotel(@PathVariable Long id, Model model) {
    Hotel hotel = hotelService.buscarPorId(id);
    model.addAttribute("hotel", hotel);
    return "hotel";
}
```

### Listar todos

```java
// Repository (herda de JpaRepository)
List<Hotel> hoteis = hotelRepository.findAll();

// Service
public List<Hotel> listarTodos() {
    return hotelRepository.findAll();
}

// Controller
@GetMapping
public String listar(Model model) {
    model.addAttribute("hoteis", hotelService.listarTodos());
    return "hoteis";
}
```

### Buscar com filtro

```java
// Repository - JPQL
@Query("SELECT h FROM Hotel h WHERE h.cidade = ?1")
List<Hotel> buscarPorCidade(String cidade);

// Repository - Derivado
List<Hotel> findByCidade(String cidade);

// Service
public List<Hotel> buscarPorCidade(String cidade) {
    return hotelRepository.findByCidade(cidade);
}

// Controller
@GetMapping("/cidade/{cidade}")
public String buscarPorCidade(@PathVariable String cidade, Model model) {
    model.addAttribute("hoteis", hotelService.buscarPorCidade(cidade));
    return "resultados";
}
```

### Criar/Salvar

```java
// Service
public Hotel criarHotel(Hotel hotel) {
    return hotelRepository.save(hotel);
}

// Controller
@PostMapping("/criar")
public String criarHotel(@ModelAttribute Hotel hotel, RedirectAttributes attr) {
    Hotel novoHotel = hotelService.criarHotel(hotel);
    attr.addFlashAttribute("mensagem", "Hotel criado!");
    return "redirect:/hoteis/" + novoHotel.getId();
}
```

### Atualizar

```java
// Service
public Hotel atualizarHotel(Hotel hotel) {
    return hotelRepository.save(hotel);  // findById + atualizar
}

// Controller
@PostMapping("/atualizar/{id}")
public String atualizarHotel(@PathVariable Long id, 
                            @ModelAttribute Hotel hotel,
                            RedirectAttributes attr) {
    hotel.setId(id);
    hotelService.atualizarHotel(hotel);
    attr.addFlashAttribute("mensagem", "Atualizado!");
    return "redirect:/hoteis/" + id;
}
```

### Deletar

```java
// Service
public void deletarHotel(Long id) {
    hotelRepository.deleteById(id);
}

// Controller
@GetMapping("/deletar/{id}")
public String deletarHotel(@PathVariable Long id, RedirectAttributes attr) {
    hotelService.deletarHotel(id);
    attr.addFlashAttribute("mensagem", "Deletado!");
    return "redirect:/hoteis";
}
```

## 🎨 Exemplos de Templates Thymeleaf

### Loop com Each

```html
<div th:each="hotel : ${hoteis}" class="hotel-card">
    <h3 th:text="${hotel.nome}">Nome</h3>
    <p th:text="${hotel.descricao}">Descrição</p>
</div>
```

### Condicional

```html
<div th:if="${hoteis.isEmpty()}">
    <p>Nenhum hotel encontrado.</p>
</div>
<div th:unless="${hoteis.isEmpty()}">
    <!-- Exibe se não estiver vazio -->
</div>
```

### Link Dinâmico

```html
<a th:href="@{/hoteis/{id}(id=${hotel.id})}" class="btn">Ver Detalhes</a>
```

### Formulário

```html
<form method="post" th:action="@{/hoteis/criar}">
    <input type="text" name="nome" required>
    <input type="text" name="cidade" required>
    <button type="submit">Criar</button>
</form>
```

### Valor em Objeto

```html
<h3 th:text="${hotel.nome}">Padrão</h3>
<p th:text="|Cidade: ${hotel.cidade}|">Padrão</p>
```

## 🐛 Debugging

### Ver Logs

Edite `src/main/resources/application.properties`:

```properties
logging.level.com.bahvago=DEBUG
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

### Query SQL

Adicione a `application.properties`:

```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

## 📊 Padrão de Resposta do Controller

```java
@Controller
@RequestMapping("/hoteis")
public class HotelController {
    
    // Listar
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("hoteis", service.listarTodos());
        return "hotel";  // renderiza hotel.html
    }
    
    // Detalhe
    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("hotel", service.buscarPorId(id));
        return "detalhe";
    }
    
    // Criar
    @PostMapping("/criar")
    public String criar(@ModelAttribute Hotel hotel, RedirectAttributes attr) {
        Hotel novo = service.criarHotel(hotel);
        attr.addFlashAttribute("mensagem", "Criado com sucesso!");
        return "redirect:/hoteis/" + novo.getId();
    }
    
    // Atualizar
    @PostMapping("/atualizar/{id}")
    public String atualizar(@PathVariable Long id, 
                           @ModelAttribute Hotel hotel,
                           RedirectAttributes attr) {
        hotel.setId(id);
        service.atualizarHotel(hotel);
        attr.addFlashAttribute("mensagem", "Atualizado!");
        return "redirect:/hoteis/" + id;
    }
    
    // Deletar
    @GetMapping("/deletar/{id}")
    public String deletar(@PathVariable Long id, RedirectAttributes attr) {
        service.deletarHotel(id);
        attr.addFlashAttribute("mensagem", "Deletado!");
        return "redirect:/hoteis";
    }
}
```

## 🧪 Testando a API com cURL

```bash
# Listar hotéis
curl http://localhost:8080/hoteis

# Buscar por ID
curl http://localhost:8080/hoteis/1

# Criar hotel (POST)
curl -X POST http://localhost:8080/hoteis/criar \
  -d "nome=Hotel ABC&cidade=Salvador" \
  -H "Content-Type: application/x-www-form-urlencoded"
```

## 📚 Estrutura de Pastas Recomendada para Novo Desenvolvedor

1. **Criar entidade** em `model/`
2. **Criar repository** em `repository/`
3. **Criar service** em `service/`
4. **Criar controller** em `controller/`
5. **Criar template** em `templates/`
6. **Adicionar rota** no controller

Sempre nessa ordem!

## 🚨 Erros Comuns

| Erro | Solução |
|------|---------|
| "Table not found" | Certifique-se que o MySQL está rodando (`docker-compose up -d`) |
| "No mapping found" | Verifique `@RequestMapping` e `@GetMapping/@PostMapping` |
| "Null Pointer Exception" | Use `.orElseThrow()` ou verifique se objeto existe |
| "Template not found" | Verifique se o arquivo HTML está em `templates/` com nome correto |
| "Resource not found (CSS/JS)" | Verifique se os arquivos estão em `static/` com caminho correto |

---
