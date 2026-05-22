# ARCHITECTURE.md

> Gerado a partir de análise do código-fonte — `2026-05-15`.  
> Todas as referências a classes e arquivos apontam para arquivos reais em `:app`.

---

## 1. Padrão: MVVM com Fluxo de Dados Unidirecional

O projeto aplica **MVVM (Model-View-ViewModel)** com um fluxo de dados unidirecional estrito em cada camada. A UI nunca muta o estado diretamente — ela despacha eventos para o ViewModel.

```
Interação do usuário
       │
       ▼
    Screen (Composable)            ← renderiza UiState; despacha XEvent
       │  onEvent(XEvent)
       ▼
    ViewModel (@HiltViewModel)     ← mantém o estado, processa eventos
       │  chamada ao repository
       ▼
    Repository (interface)         ← contrato de domínio; sem tipos de framework
       │  chamada ao CartaoDao + mapper
       ▼
    Room DAO                       ← persistência SQLite
       │  Flow<Entity> / suspend
       ▼
    CartaoEntity                   ← representação de armazenamento

Sessão (autenticação persistente)
       │
       ▼
    SessionManager (interface)
       │  DataStore<Preferences> + fallback criptografado
       ▼
    SessionManagerImpl
```

---

## 2. Separação de Camadas

### Camada 1 — Modelo de Domínio (`model/`)

`Cartao.kt` é uma `data class` Kotlin pura com **zero importações de framework**. É o único tipo que atravessa todos os limites de camadas. Nem anotações do Room (`@Entity`, `@PrimaryKey`) nem do Retrofit (`@SerialName`) aparecem nesta camada.

```kotlin
// model/Cartao.kt — imports: none
data class Cartao(
    val id          : Long   = 0L,
    val nomeTitular : String = "",
    val finalNumero : String = "",
    val bandeira    : String = "",
    val validade    : String = "",
    val limite      : Double = 0.0,
)
```

### Camada 2 — Dados / Persistência (`data/local/`)

Concentra todo o código específico do Room. Nada desta camada vaza para `viewmodel/` ou `ui/`.

| Arquivo | Responsabilidade |
|---|---|
| `CartaoEntity.kt` | `@Entity(tableName = "cartoes")` — um campo por coluna do banco; espelha `Cartao` 1-a-1 |
| `CartaoDao.kt` | Interface `@Dao` — duas estratégias de leitura para a mesma linha: `Flow` reativo + `suspend` pontual |
| `AppDatabase.kt` | `@Database` — declara a lista de entidades (v1), `exportSchema = false`, expõe `cartaoDao()` |

**Duas assinaturas de leitura para acesso a uma única linha em `CartaoDao`:**
```kotlin
// Reativo — usado pelo DetalheViewModel; auto-atualiza após edição ou exclusão externa
@Query("SELECT * FROM cartoes WHERE id = :id")
fun observarPorId(id: Long): Flow<CartaoEntity?>

// Pontual — usado pelo CadastrarAlterarViewModel para pré-preencher o formulário de edição
@Query("SELECT * FROM cartoes WHERE id = :id")
suspend fun buscarPorId(id: Long): CartaoEntity?
```

`observarTodos()` inclui ordenação: `SELECT * FROM cartoes ORDER BY nomeTitular ASC`.

As exclusões usam `@Query` (não `@Delete`) para operar pelo `id` sem exigir um objeto `CartaoEntity` completo:
```kotlin
@Query("DELETE FROM cartoes WHERE id = :id")
suspend fun excluirPorId(id: Long)
```

### Camada 3 — Repository (`repository/`)

Faz a ponte entre as camadas de domínio e de dados. A interface `CartaoRepository`, voltada ao domínio, expõe apenas objetos `Cartao`; `CartaoRepositoryImpl` é a única classe que importa `CartaoDao` ou `CartaoEntity`.

```
CartaoRepository (interface — lado do domínio)
    observarTodos()        : Flow<List<Cartao>>
    observarPorId(id)      : Flow<Cartao?>
    buscarPorId(id)        : suspend Cartao?
    salvar(cartao)         : suspend Long
    atualizar(cartao)      : suspend Unit
    excluirPorId(id)       : suspend Unit

CartaoRepositoryImpl (implementação — lado dos dados)
    @Inject constructor(cartaoDao: CartaoDao)
    — chama o DAO, faz o mapeamento via CartaoMapper
```

**Transformação de Flow no repository:**
```kotlin
// observarTodos: CartaoDao emite List<CartaoEntity> → impl mapeia cada um para Cartao
override fun observarTodos(): Flow<List<Cartao>> =
    cartaoDao.observarTodos().map { list -> list.map { it.toDomain() } }

// observarPorId: emite null quando o registro é excluído → DetalheViewModel usa isso para navegar de volta automaticamente
override fun observarPorId(id: Long): Flow<Cartao?> =
    cartaoDao.observarPorId(id).map { it?.toDomain() }
```

**Mapper (`repository/mapper/CartaoMapper.kt`):**
```kotlin
fun CartaoEntity.toDomain(): Cartao   // dados → domínio (usado após leituras)
fun Cartao.toEntity(): CartaoEntity   // domínio → dados (usado antes de escritas)
```

### Camada 4 — ViewModels (`viewmodel/`)

Os três ViewModels seguem um contrato estrutural idêntico:

```kotlin
@HiltViewModel
class XViewModel @Inject constructor(
    savedStateHandle         : SavedStateHandle,   // apenas quando parâmetros de rota são necessários
    private val cartaoRepository: CartaoRepository,
) : ViewModel() {

    // Extração de rota (apenas DetalheViewModel, CadastrarAlterarViewModel)
    private val route : XRoute = savedStateHandle.toRoute()
    private val id    : Long   = route.id

    // Estado — mutável privado, imutável público
    private val _uiState = MutableStateFlow(XUiState(...))
    val uiState: StateFlow<XUiState> = _uiState.asStateFlow()

    // Eventos pontuais — Channel, não StateFlow
    private val _uiEvent = Channel<XUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    init { /* inicia observação reativa ou carregamento pontual */ }

    fun onEvent(event: XEvent) { when (event) { ... } }   // público, exaustivo
    private fun excluir() { ... }                          // funções de negócio privadas
    private fun salvar() { ... }
    private fun validar(): Boolean { ... }
}
```

**Matriz de estratégia — leituras reativas vs. pontuais:**

| ViewModel | Estratégia de Leitura | Motivo |
|---|---|---|
| `ListaViewModel` | `observarTodos().collect {}` → `Flow<List<Cartao>>` | A lista deve refletir toda criação/exclusão em tempo real |
| `DetalheViewModel` | `observarPorId(id).collect {}` → `Flow<Cartao?>` | O detalhe se auto-atualiza após voltar da edição; emissão nula dispara navegar de volta |
| `CadastrarAlterarViewModel` | `buscarPorId(id)` (pontual) → `Cartao?` | Pré-preenchimento do formulário uma única vez; sem necessidade de observar mudanças durante a edição |

**Campos notáveis em `CadastrarAlterarUiState`:**
- `salvando: Boolean = false` — flag separada para o progresso do save; distinto de `carregando` (carregamento inicial dos dados para edição).
- `isEdicao: Boolean` — propriedade computada (`get() = nomeTitular.isNotBlank()`), sem custo de campo.
- Sem campo `erro: String?` global; usa erros por campo (`erroNome`, `erroNumero`, `erroBandeira`, `erroValidade`, `erroLimite`).

### Camada 5 — UI (`ui/`)

A camada de UI **apenas renderiza `UiState`** e **despacha `XEvent`**. Não contém nenhuma lógica de negócio. Suas sub-camadas são:

| Sub-pacote | Conteúdo |
|---|---|
| `ui/theme/` | Tokens do design system — `GerenciadorCartoesTheme`, `Spacing`, `IconSize`, `Typography`, `Shapes` |
| `ui/components/` | Composables agnósticos de feature — `AppScaffold`, `AppTopAppBar`, `AppLoading`, `EmptyState` |
| `ui/navigation/` | Declarações de rotas (`Routes.kt`) e configuração do `NavHost` (`AppNavHost.kt`) |
| `ui/feature/<nome>/` | UI com escopo de feature — `XScreen`, `XContent`, `XEvent`, `XUiEvent`, `state/XUiState` |

---

## 3. Padrão Screen / Content / Preview

Cada tela de feature é dividida em exatamente **três níveis** dentro de um único arquivo `.kt`:

```
XScreen   — dono do ViewModel; coleta uiState; roteia UiEvent via LaunchedEffect
XContent  — composable puro; sem ViewModel; recebe (uiState, snackbarHostState, onEvent)
@Preview  — sempre chama XContent, nunca XScreen
```

**Exemplo de `ListaScreen.kt`:**
```kotlin
// Nível 1 — dono do ViewModel; roteia eventos pontuais; passa estado para baixo
@Composable
fun ListaScreen(
    onNavigateToNovo : () -> Unit,
    onNavigateToItem : (id: Long) -> Unit,
    viewModel        : ListaViewModel = hiltViewModel(),
) {
    val uiState          by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ListaUiEvent.NavegaParaItem  -> onNavigateToItem(event.id)
                ListaUiEvent.NavegaParaNovo     -> onNavigateToNovo()
                is ListaUiEvent.MostrarErro     -> snackbarHostState.showSnackbar(event.mensagem)
                is ListaUiEvent.MostrarMensagem -> snackbarHostState.showSnackbar(event.mensagem)
            }
        }
    }

    ListaContent(uiState = uiState, snackbarHostState = snackbarHostState, onEvent = viewModel::onEvent)
}

// Nível 2 — puro; testável sem ViewModel; recebe estado + callback
@Composable
fun ListaContent(
    uiState           : ListaUiState      = ListaUiState(),
    snackbarHostState : SnackbarHostState = remember { SnackbarHostState() },
    onEvent           : (ListaEvent) -> Unit = {},
) { /* renderiza uiState */ }

// Nível 3 — Previews chamam apenas Content, nunca Screen
@Preview(showBackground = true, name = "Lista – Com cartões")
@Preview(showBackground = true, name = "Lista – Com cartões Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ListaComItensPreview() {
    GerenciadorCartoesTheme {
        ListaContent(uiState = ListaUiState(cartoes = listOf(
            Cartao(1L, "João Silva", "1234", "Visa", "12/28", 5_000.0),
        )))
    }
}
```

---

## 4. Separação Event / UiEvent

Cada feature define duas sealed interfaces distintas:

| Tipo | Direção | Transporte | Finalidade |
|---|---|---|---|
| `XEvent` | UI → ViewModel | chamada direta `onEvent(event)` | Intenção do usuário (clique, alteração de texto, confirmar exclusão) |
| `XUiEvent` | ViewModel → UI | `Channel<T>(BUFFERED)` consumido em `LaunchedEffect` | Efeitos pontuais (navegar, exibir Snackbar) |

**Por que `Channel` para `UiEvent`:**  
`Channel` não reproduz eventos anteriores. Um evento de navegação que sobrevivesse a uma recomposição via `StateFlow` re-acionaria a navegação e produziria entradas duplicadas na pilha de navegação. `Channel.BUFFERED` garante a entrega mesmo que o coletor ainda não esteja ativo no momento da emissão.

**Inventário completo de eventos por feature:**

| Feature | Variantes de `XEvent` | Variantes de `XUiEvent` |
|---|---|---|
| Lista | `NavegaParaNovo`, `NavegaParaItem(id)`, `ExcluirCartao(id)` | `NavegaParaNovo`, `NavegaParaItem(id)`, `MostrarErro(msg)`, `MostrarMensagem(msg)` |
| Detalhe | `Voltar`, `Editar`, `Excluir` | `NavigateBack`, `NavegaParaEditar(id)`, `MostrarErro(msg)` |
| CadastrarAlterar | `Voltar`, `Salvar`, `NomeTitularAlterado(valor)`, `FinalNumeroAlterado(valor)`, `BandeiraAlterada(valor)`, `ValidadeAlterada(valor)`, `LimiteAlterado(valor)` | `NavigateBack`, `MostrarErro(msg)` |

---

## 5. Navegação — Navigation Compose 2 (Type-Safe)

**Rotas (`ui/navigation/Routes.kt`):**
```kotlin
@Serializable object ListaRoute                          // sem parâmetros → object singleton
@Serializable data class DetalheRoute(val id: Long)     // requer id → data class
@Serializable data class CadastrarAlterarRoute(val id: Long = 0L)  // 0 = novo, >0 = editar
```

**Registro no NavHost (`AppNavHost.kt`):**
```kotlin
NavHost(navController = navController, startDestination = ListaRoute) {
    composable<ListaRoute>            { ListaScreen(onNavigateToNovo = {...}, onNavigateToItem = {...}) }
    composable<DetalheRoute>          { DetalheScreen(navigateBack = {...}, onNavigateToEditar = {...}) }
    composable<CadastrarAlterarRoute> { CadastrarAlterarScreen(navigateBack = {...}) }
}
```

**Extração de parâmetros com type-safety nos ViewModels:**
```kotlin
// Sem acesso a Bundle, sem chaves string, sem NavArgs — totalmente seguro em tempo de compilação
private val route: CadastrarAlterarRoute = savedStateHandle.toRoute()
private val id: Long = route.id
```

`CadastrarAlterarRoute` usa `id = 0L` como sentinela: `id == 0L` → modo inserção, `id > 0L` → modo edição. O ViewModel verifica `if (id != 0L) carregarCartao()` no `init` para pré-preencher o formulário condicionalmente.

Gate de sessão no lançamento:
- `SplashRoute` é o destino inicial.
- `SplashViewModel` aguarda o tempo da splash e consulta `SessionManager.isLoggedIn()`.
- Se `true`, emite `NavigateToLista`; se `false`, emite `NavigateToLogin`.

---

## 6. Injeção de Dependência (Hilt)

```
GerenciadorCartoesApp  (@HiltAndroidApp)
│
├── AppModule  (@InstallIn SingletonComponent)  — abstract class
│   ├── companion object @Provides
│   │   ├── AppDatabase  → Room.databaseBuilder(context, "gerenciador-cartoes-db")
│   │   └── CartaoDao    → AppDatabase.cartaoDao()
│   │   ├── DataStore<Preferences> → session.preferences_pb
│   │   └── EncryptedSharedPreferences("session_secure_prefs")
│   └── @Binds @Singleton
│       └── CartaoRepository ← CartaoRepositoryImpl
│       └── SessionManager   ← SessionManagerImpl
│
└── NetworkModule  (@InstallIn SingletonComponent)  — object
    ├── @Provides Json           (ignoreUnknownKeys, coerceInputValues)
    ├── @Provides OkHttpClient   (LoggingInterceptor BODY, timeouts de 30s)
    ├── @Provides Retrofit       (URL base, conversor kotlinx-serialization)
    └── @Provides ApiService     (retrofit.create)
```

`AppModule` é uma `abstract class` porque o Dagger exige classes abstratas para métodos `@Binds`. Os métodos `@Provides` para objetos concretos ficam em um `companion object` — o padrão canônico do Dagger/Hilt ao combinar os dois tipos de anotação.

`NetworkModule` é um `object` simples pois contém apenas `@Provides` — sem necessidade de `@Binds`.

---

## 7. Fluxo de Dados Reativo (Room + Coroutines)

A cadeia reativa completa do banco de dados até a UI é:

```
Room SQLite
  │  emite um novo snapshot a cada INSERT / UPDATE / DELETE
  ▼
CartaoDao.observarTodos()  : Flow<List<CartaoEntity>>
CartaoDao.observarPorId()  : Flow<CartaoEntity?>
  │
  ▼  (mapeamento via CartaoMapper — toDomain())
CartaoRepositoryImpl
  │  Flow<List<Cartao>>  /  Flow<Cartao?>
  ▼
ViewModel  viewModelScope.launch { flow.collect { } }
  │  _uiState.update { it.copy(cartoes = newList, carregando = false) }
  ▼
StateFlow<XUiState>
  │  collectAsStateWithLifecycle()  — pausa em segundo plano (eficiente em bateria)
  ▼
XContent (Composable) recompõe automaticamente
```

Isso significa que a UI **recompõe automaticamente** após qualquer escrita (salvar, atualizar, excluir) sem nenhuma chamada explícita de refresh. A `DetalheScreen` trata o caso limite de exclusão: quando `observarPorId` emite `null` (registro excluído em outro lugar), o ViewModel envia `NavigateBack` via `Channel`.

---

## 8. Padrão de Tratamento de Erros

Todas as chamadas ao repository dentro de `viewModelScope.launch` usam `runCatching { }.onFailure { }` de forma consistente nos três ViewModels:

```kotlin
viewModelScope.launch {
    runCatching {
        // caminho feliz — chamada ao DAO ou collect
    }.onFailure { erro ->
        _uiState.update { it.copy(carregando = false) }
        _uiEvent.send(XUiEvent.MostrarErro(erro.message ?: "Erro desconhecido"))
    }
}
```

Não são usadas exceções verificadas nem wrappers `Result`. Os erros sempre chegam à UI como `MostrarErro` via `UiEvent`, que a `Screen` exibe via `SnackbarHostState.showSnackbar()`.

---

## 9. Componentes Compartilhados (`ui/components/`)

Quatro composables são agnósticos de feature e compartilhados por todas as telas:

| Composable | Responsabilidade |
|---|---|
| `AppScaffold` | Encapsula `Material3.Scaffold`; conecta o `SnackbarHost`; expõe slots `topBar`, `floatingActionButton`, `content` |
| `AppTopAppBar` | Encapsula `Material3.TopAppBar`; `onNavigateBack` opcional (omitir em telas raiz); slot `actions` opcional |
| `AppLoading` | `CircularProgressIndicator` centralizado em tela cheia; exibido quando `uiState.carregando = true` |
| `EmptyState` | Ícone + mensagem centralizados em tela cheia; exibido quando a lista está vazia em `ListaContent` |

Nenhum desses componentes chama `hiltViewModel()` nem referencia nenhum `UiState` ou `Event` específico de feature.

---

## 10. Design System (`ui/theme/`)

| Arquivo | Símbolo exportado | Padrão de acesso |
|---|---|---|
| `Color.kt` | `Purple40`, `Purple80`, `Pink40`, etc. | Usado internamente em `Theme.kt` |
| `Type.kt` | `Typography` | `MaterialTheme.typography.*` |
| `Shape.kt` | `Shapes` | `MaterialTheme.shapes.*` |
| `Spacing.kt` | `Spacing`, `LocalSpacing` | `LocalSpacing.current.medium` ou `MaterialTheme.spacing.medium` |
| `IconSize.kt` | `IconSize`, `LocalIconSize` | `LocalIconSize.current.medium` ou `MaterialTheme.iconSize.medium` |
| `Theme.kt` | `GerenciadorCartoesTheme` | Encapsula todos os composables; fornece `LocalSpacing` e `LocalIconSize` via `CompositionLocalProvider` |

`Spacing` e `IconSize` são fornecidos tanto via `CompositionLocal` (primário) quanto via propriedades de extensão de `MaterialTheme` (conveniência). As propriedades de extensão delegam para o `CompositionLocal`:

```kotlin
val MaterialTheme.spacing: Spacing
    @Composable @ReadOnlyComposable get() = LocalSpacing.current

val MaterialTheme.iconSize: IconSize
    @Composable @ReadOnlyComposable get() = LocalIconSize.current
```
