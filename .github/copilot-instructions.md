# Instruções para GitHub Copilot — GerenciadorCartoes

> **Este arquivo é lido automaticamente pelo GitHub Copilot Agent no início de cada sessão.**  
> **Todas as regras abaixo foram extraídas do código-fonte real. Não inventar padrões — apenas seguir o que já existe.**

---

## 0. Checklist Obrigatório Antes de Gerar Qualquer Código

Antes de escrever ou editar qualquer arquivo Kotlin, execute esta sequência:

1. ✅ **SEMPRE** leia `docs/AI_CONTEXT.md` por completo — ele é a fonte primária de verdade deste projeto.
2. ✅ **SEMPRE** identifique em qual camada o arquivo gerado deve residir antes de criá-lo.
3. ✅ **SEMPRE** siga exatamente os templates das seções §5, §6 e §8 deste arquivo.
4. ❌ **NUNCA** invente um padrão não observado no código-fonte existente.
5. ❌ **NUNCA** sugira "melhorias arquiteturais" — apenas reproduza o que já existe.
6. ❌ **NUNCA** gere código sem verificar as regras de camadas da §4.

---

## 1. Contexto do Projeto

Aplicativo Android de CRUD de cartões de crédito/débito (`Cartao`), com armazenamento local via Room.

| Campo | Valor |
|---|---|
| Pacote raiz | `com.app.gerenciadorcartoes` |
| Linguagem | Kotlin 2.3.21 (compilador K2) |
| UI | Jetpack Compose + Material 3 (BOM 2026.05.00) |
| Arquitetura | MVVM estrito com Fluxo de Dados Unidirecional (UDF) |
| DI | Hilt 2.59.2 via KSP (sem KAPT) |
| Banco | Room 2.8.4 (reativo via `Flow`) |
| Navegação | Navigation Compose 2.9.0 — rotas type-safe com `@Serializable` |
| Min SDK | 28 / Target SDK 36 |

---

## 2. Referência Rápida — SEMPRE / NUNCA

A tabela abaixo é o resumo executivo. As seções seguintes detalham cada regra.

| ✅ SEMPRE FAZER | ❌ NUNCA FAZER |
|---|---|
| `_uiState.update { it.copy(...) }` para mutar estado | `_uiState.value = ...` diretamente |
| `Channel<XUiEvent>(Channel.BUFFERED)` para eventos pontuais | `StateFlow` para eventos de navegação/Snackbar |
| `when` exaustivo em sealed interfaces | `else ->` em `when` sobre sealed interface |
| `hiltViewModel()` apenas em `XScreen` | `hiltViewModel()` dentro de `XContent` |
| `@Preview` chamando `XContent` | `@Preview` chamando `XScreen` |
| `GerenciadorCartoesTheme {}` em todo `@Preview` | `@Preview` sem o tema encapsulador |
| `LocalSpacing.current.medium` para espaçamento | `16.dp` literal em código de feature |
| `CartaoRepository` (interface) nos ViewModels | `CartaoDao` injetado diretamente em ViewModel |
| `Cartao` (domínio) nos ViewModels | `CartaoEntity` no ViewModel |
| `observarPorId()` no `DetalheViewModel` | `buscarPorId()` no `DetalheViewModel` |
| `buscarPorId()` no `CadastrarAlterarViewModel` | `observarPorId()` no `CadastrarAlterarViewModel` |
| Lógica de negócio exclusivamente no ViewModel | Qualquer lógica dentro de Composables |
| `abstract class` + `companion object` quando precisar de `@Binds` | `object` para módulos Hilt que contenham `@Binds` |
| Mappers chamados apenas dentro de `CartaoRepositoryImpl` | Mappers chamados em ViewModel ou UI |
| `runCatching { }.onFailure { }` para todas as chamadas ao repository | `try/catch`, `Result`, ou exceções verificadas |

---

## 3. Estrutura de Pacotes

> ✅ **SEMPRE** coloque novos arquivos no pacote correto conforme a árvore abaixo.  
> ❌ **NUNCA** coloque ViewModels dentro de `ui/`, nem UiState fora de `ui/feature/<nome>/state/`.

```
com.app.gerenciadorcartoes/
├── GerenciadorCartoesApp.kt        @HiltAndroidApp — ponto de entrada do DI
├── MainActivity.kt                 @AndroidEntryPoint — única Activity; chama AppNavHost()
│
├── model/                          ← APENAS modelos de domínio puros (zero framework)
│   └── Cartao.kt
│
├── data/local/                     ← APENAS código Room
│   ├── converter/                  (vazio — reservado para @TypeConverters futuros)
│   ├── dao/CartaoDao.kt
│   ├── database/AppDatabase.kt
│   └── entity/CartaoEntity.kt
│
├── repository/                     ← Interface de domínio + implementação + mapper
│   ├── CartaoRepository.kt         Interface — apenas tipos Cartao / primitivos
│   ├── CartaoRepositoryImpl.kt     ÚNICA classe que importa CartaoDao / CartaoEntity
│   └── mapper/CartaoMapper.kt      ÚNICA localização dos mappers toDomain/toEntity
│
├── network/
│   └── service/ApiService.kt       Placeholder — sem endpoints ativos
│
├── di/                             ← APENAS módulos Hilt
│   ├── AppModule.kt
│   └── NetworkModule.kt
│
└── ui/
    ├── theme/                      ← Design system: tema, spacing, iconSize, typography
    ├── components/                 ← Composables agnósticos de feature
    ├── navigation/                 ← Rotas e NavHost APENAS
    ├── feature/
    │   ├── lista/                  ← Tudo que é exclusivo da feature Lista
    │   │   ├── state/ListaUiState.kt
    │   │   ├── ListaEvent.kt
    │   │   ├── ListaUiEvent.kt
    │   │   └── ListaScreen.kt
    │   ├── detalhe/                ← Tudo que é exclusivo da feature Detalhe
    │   └── cadastraralterar/       ← Tudo que é exclusivo da feature CadastrarAlterar
    └── viewmodel/                  ← TODOS os ViewModels — irmão de ui/, NÃO filho
        ├── ListaViewModel.kt
        ├── DetalheViewModel.kt
        └── CadastrarAlterarViewModel.kt
```

**Regras de posicionamento:**
- ✅ **SEMPRE** coloque ViewModels em `viewmodel/` (fora de `ui/`)
- ✅ **SEMPRE** coloque `XUiState` em `ui/feature/<nome>/state/XUiState.kt`
- ✅ **SEMPRE** coloque `XEvent` e `XUiEvent` em `ui/feature/<nome>/`
- ❌ **NUNCA** crie um ViewModel dentro de `ui/feature/`
- ❌ **NUNCA** coloque código Room (Entity, Dao) fora do pacote `data/local/`

---

## 4. Invariantes de Camadas — Violação Proibida

> ❌ As regras abaixo são **absolutas**. Gerar código que as viole é proibido em qualquer circunstância.

| ❌ NUNCA | ✅ DEVE SER ASSIM |
|---|---|
| `model/Cartao.kt` importar qualquer framework | `Cartao` tem zero importações além de stdlib Kotlin |
| `CartaoRepository` expor `CartaoEntity`, `CartaoDao` ou tipo Retrofit | Interface expõe apenas `Cartao`, `Long`, `Unit`, `Flow`, `Boolean` |
| Qualquer classe além de `CartaoRepositoryImpl` importar `CartaoEntity` | `CartaoRepositoryImpl` é a barreira exclusiva entre domínio e dados |
| ViewModel importar `CartaoDao` diretamente | ViewModel injeta `CartaoRepository` (interface) |
| Composable calcular, validar ou transformar dados | Toda lógica fica no ViewModel; Composable apenas renderiza `UiState` |
| `XContent` receber ou instanciar ViewModel | `hiltViewModel()` é chamado somente em `XScreen` |
| Evento de navegação transportado por `StateFlow` | Navegação via `Channel<XUiEvent>(Channel.BUFFERED)` |
| `@Preview` chamar `XScreen` | `@Preview` sempre chama `XContent` |

---

## 5. Padrão Screen / Content / Preview

> ✅ **SEMPRE** divida cada tela em exatamente três níveis dentro de um único arquivo `.kt`.  
> ❌ **NUNCA** coloque lógica de negócio em `XContent`.  
> ❌ **NUNCA** chame `hiltViewModel()` fora de `XScreen`.

```kotlin
// ── Nível 1: Screen — dona do ViewModel ─────────────────────────────────────
// ✅ SEMPRE: collectAsStateWithLifecycle() — nunca collectAsState()
// ✅ SEMPRE: LaunchedEffect(viewModel) para consumir uiEvent
// ✅ SEMPRE: criar snackbarHostState aqui e passar para XContent
@Composable
fun XScreen(
    navigateBack : () -> Unit,
    viewModel    : XViewModel = hiltViewModel(),
) {
    val uiState          by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {                                    // ← when EXAUSTIVO
                XUiEvent.NavigateBack   -> navigateBack()
                is XUiEvent.MostrarErro -> snackbarHostState.showSnackbar(event.mensagem)
            }
        }
    }

    XContent(
        uiState           = uiState,
        snackbarHostState = snackbarHostState,
        onEvent           = viewModel::onEvent,             // ← referência de método
    )
}

// ── Nível 2: Content — composable puro ──────────────────────────────────────
// ✅ SEMPRE: todos os parâmetros com valores padrão (testável sem argumentos)
// ✅ SEMPRE: usar AppScaffold, AppTopAppBar, AppLoading dos shared components
// ❌ NUNCA: chamar hiltViewModel() aqui
// ❌ NUNCA: conter lógica de negócio, validação ou transformação de dados
@Composable
fun XContent(
    uiState           : XUiState          = XUiState(),
    snackbarHostState : SnackbarHostState = remember { SnackbarHostState() },
    onEvent           : (XEvent) -> Unit  = {},
) {
    AppScaffold(
        snackbarHostState = snackbarHostState,
        topBar = { AppTopAppBar(title = "Título", onNavigateBack = { onEvent(XEvent.Voltar) }) },
    ) { paddingValues ->
        when {
            uiState.carregando -> AppLoading()
            else               -> { /* renderiza conteúdo com paddingValues */ }
        }
    }
}

// ── Nível 3: Previews ────────────────────────────────────────────────────────
// ✅ SEMPRE: private, GerenciadorCartoesTheme, XContent, par Light+Dark
// ✅ SEMPRE: cobrir carregando, vazio/erro e preenchido
// ❌ NUNCA: chamar XScreen no @Preview
@Preview(showBackground = true, name = "X – Carregando")
@Preview(showBackground = true, name = "X – Carregando Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun XCarregandoPreview() {
    GerenciadorCartoesTheme { XContent(uiState = XUiState(carregando = true)) }
}

@Preview(showBackground = true, name = "X – Com dados")
@Composable
private fun XComDadosPreview() {
    GerenciadorCartoesTheme { XContent() }
}
```

---

## 6. Template de ViewModel

> ✅ **SEMPRE** use `@HiltViewModel` e `@Inject constructor`.  
> ✅ **SEMPRE** exponha `StateFlow` imutável via `asStateFlow()`.  
> ✅ **SEMPRE** exponha `uiEvent` via `receiveAsFlow()`.  
> ✅ **SEMPRE** use `viewModelScope.launch` + `runCatching` para todas as chamadas ao repository.  
> ❌ **NUNCA** use `else ->` no `when` sobre `XEvent` (sealed interface).  
> ❌ **NUNCA** mute `_uiState` com `_uiState.value = ...`.  
> ❌ **NUNCA** injete `CartaoDao` ou `CartaoEntity` no ViewModel.

```kotlin
@HiltViewModel
class XViewModel @Inject constructor(
    savedStateHandle         : SavedStateHandle,         // omitir se sem parâmetros de rota
    private val cartaoRepository: CartaoRepository,      // SEMPRE a interface, nunca o Impl
) : ViewModel() {

    // ✅ Extração de rota — sem Bundle, sem chaves string
    private val route : XRoute = savedStateHandle.toRoute()
    private val id    : Long   = route.id

    // ✅ Estado — privado e mutável; público e imutável
    private val _uiState = MutableStateFlow(XUiState())
    val uiState: StateFlow<XUiState> = _uiState.asStateFlow()

    // ✅ Eventos pontuais — Channel, nunca StateFlow
    private val _uiEvent = Channel<XUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        // ✅ Iniciar observação reativa (Flow.collect) ou carregamento pontual
    }

    // ✅ when EXAUSTIVO — listar todas as variantes de XEvent
    fun onEvent(event: XEvent) {
        when (event) {
            XEvent.Voltar  -> viewModelScope.launch { _uiEvent.send(XUiEvent.NavigateBack) }
            XEvent.Salvar  -> salvar()
            // ... demais variantes
        }
    }

    // ✅ Funções privadas usam runCatching sem exceção
    private fun salvar() {
        viewModelScope.launch {
            runCatching {
                cartaoRepository.salvar(/* ... */)
                _uiEvent.send(XUiEvent.NavigateBack)
            }.onFailure { erro ->
                _uiState.update { it.copy(carregando = false) }
                _uiEvent.send(XUiEvent.MostrarErro(erro.message ?: "Erro desconhecido"))
            }
        }
    }
}
```

---

## 7. Estratégia de Leitura por ViewModel

> ✅ **SEMPRE** use `observarPorId()` (reativo) no `DetalheViewModel`.  
> ✅ **SEMPRE** use `buscarPorId()` (pontual) no `CadastrarAlterarViewModel`.  
> ❌ **NUNCA** inverta essas estratégias — cada uma existe por uma razão específica.

| ViewModel | SEMPRE usar | NUNCA usar | Motivo |
|---|---|---|---|
| `ListaViewModel` | `observarTodos().collect {}` → `Flow<List<Cartao>>` | `buscarPorId()` | Lista deve refletir toda escrita em tempo real |
| `DetalheViewModel` | `observarPorId(id).collect {}` → `Flow<Cartao?>` | `buscarPorId()` | Re-emite após voltar da edição; `null` → `NavigateBack` automático |
| `CadastrarAlterarViewModel` | `buscarPorId(id)` → `suspend Cartao?` | `observarPorId()` | Pré-preenchimento pontual — sem reatividade durante edição |

---

## 8. Templates de UiState e Eventos

> ✅ **SEMPRE** coloque `XUiState` em `ui/feature/<nome>/state/XUiState.kt`.  
> ✅ **SEMPRE** use `data class` imutável com defaults em todos os campos.  
> ✅ **SEMPRE** use `carregando` (não `isLoading`), `erro` (não `errorMessage`).  
> ✅ **SEMPRE** use `sealed interface` (não `sealed class`) para eventos.  
> ✅ **SEMPRE** `data object` para variantes sem dados, `data class` para variantes com dados.  
> ❌ **NUNCA** armazene dados derivados como campo — use propriedade computada (`val x get() = ...`).  
> ❌ **NUNCA** armazene `CartaoEntity` dentro de `UiState`.

### UiState

```kotlin
// ✅ Tela simples (somente leitura)
data class XUiState(
    val carregando : Boolean  = false,   // flag de carregamento inicial
    val erro       : String?  = null,    // null = sem erro
    val cartao     : Cartao   = Cartao(), // SEMPRE tipo de domínio, nunca CartaoEntity
)

// ✅ Formulário (cadastro/edição)
data class XFormUiState(
    // ✅ Campos de formulário como String — vinculam direto ao OutlinedTextField
    val nomeTitular  : String  = "",
    val carregando   : Boolean = false,  // carregamento inicial dos dados para edição
    val salvando     : Boolean = false,  // progresso exclusivo da operação de save
    // ✅ Erros nomeados "erro<NomeCampo>" — null = sem erro
    val erroNome     : String? = null,
    val erroBandeira : String? = null,
) {
    // ✅ Propriedade computada — não duplicar estado
    val isEdicao: Boolean get() = nomeTitular.isNotBlank()
}
```

### Eventos

```kotlin
// ✅ XEvent.kt — intenção do usuário (UI → ViewModel)
//    SEMPRE sealed interface, NUNCA sealed class
sealed interface XEvent {
    data object Voltar                           : XEvent  // sem dados → data object
    data object Salvar                           : XEvent
    data class  CampoAlterado(val valor: String) : XEvent  // com dados → data class
}

// ✅ XUiEvent.kt — efeito pontual (ViewModel → UI)
//    SEMPRE transportado por Channel — NUNCA por StateFlow
sealed interface XUiEvent {
    data object NavigateBack                           : XUiEvent
    data class  MostrarErro(val mensagem: String)      : XUiEvent
    data class  MostrarMensagem(val mensagem: String)  : XUiEvent
}
```

---

## 9. Rotas de Navegação

> ✅ **SEMPRE** coloque rotas exclusivamente em `ui/navigation/Routes.kt`.  
> ✅ **SEMPRE** use `@Serializable` em todas as rotas.  
> ✅ **SEMPRE** use `savedStateHandle.toRoute()` para extrair parâmetros — sem Bundle, sem chaves string.  
> ✅ **SEMPRE** use `object` para rotas sem parâmetros; `data class` para rotas com parâmetros.  
> ❌ **NUNCA** passe parâmetros de rota como string em `navController.navigate("rota/$id")`.

```kotlin
// ✅ ui/navigation/Routes.kt
@Serializable object XRoute                                // sem parâmetros → object
@Serializable data class XComParamRoute(val id: Long)     // com parâmetros → data class
@Serializable data class XOpcionalRoute(val id: Long = 0L) // 0 = criar, >0 = editar
```

```kotlin
// ✅ Extração no ViewModel — type-safe, sem string
private val route: XRoute = savedStateHandle.toRoute()
private val id   : Long   = route.id
```

**Rotas existentes — não duplicar nem renomear:**

| Rota | Parâmetros | Destino inicial |
|---|---|---|
| `ListaRoute` | — | ✅ sim |
| `DetalheRoute(id: Long)` | `id` obrigatório | não |
| `CadastrarAlterarRoute(id: Long = 0L)` | `id=0` novo · `id>0` editar | não |

---

## 10. Contrato do Repository

> ✅ **SEMPRE** use a interface `CartaoRepository` nos ViewModels — nunca o `CartaoRepositoryImpl`.  
> ✅ **SEMPRE** mantenha os tipos de retorno como `Cartao` / primitivos — nunca `CartaoEntity`.  
> ❌ **NUNCA** adicione `suspend` às funções reativas (`observarTodos`, `observarPorId`).

```kotlin
interface CartaoRepository {
    // ✅ Reativas — sem suspend, retornam Flow
    fun observarTodos()         : Flow<List<Cartao>>
    fun observarPorId(id: Long) : Flow<Cartao?>       // null quando registro excluído

    // ✅ Pontuais — com suspend
    suspend fun buscarPorId(id: Long)     : Cartao?
    suspend fun salvar(cartao: Cartao)    : Long       // retorna id gerado
    suspend fun atualizar(cartao: Cartao)
    suspend fun excluirPorId(id: Long)
}
```

---

## 11. Convenções de Nomenclatura

> ✅ **SEMPRE** siga exatamente os sufixos abaixo — não criar variações.  
> ❌ **NUNCA** use sufixos como `Manager`, `Helper`, `Handler`, `Interactor` ou `UseCase`.

### Classes e arquivos

| Tipo | Sufixo obrigatório | Exemplo |
|---|---|---|
| Modelo de domínio | _(nenhum)_ | `Cartao` |
| Entidade Room | `Entity` | `CartaoEntity` |
| DAO Room | `Dao` | `CartaoDao` |
| Banco de dados | `Database` | `AppDatabase` |
| Interface Repository | `Repository` | `CartaoRepository` |
| Implementação Repository | `RepositoryImpl` | `CartaoRepositoryImpl` |
| Serviço Retrofit | `Service` | `ApiService` |
| ViewModel | `ViewModel` | `ListaViewModel` |
| Estado da UI | `UiState` | `ListaUiState` |
| Evento do usuário (UI→VM) | `Event` | `ListaEvent` |
| Efeito pontual (VM→UI) | `UiEvent` | `ListaUiEvent` |
| Rota | `Route` | `DetalheRoute` |
| Composable de tela | `Screen` | `ListaScreen` |
| Composable puro | `Content` | `ListaContent` |
| Composable interno | `private fun` descritivo | `CartaoItem`, `DetalheRow` |
| Preview | `private fun <Feature><Variante>Preview` | `ListaComItensPreview` |

### Campos de UiState — nomes obrigatórios

| Campo | Tipo | ✅ Usar | ❌ Nunca usar |
|---|---|---|---|
| Flag de carregamento inicial | `Boolean = false` | `carregando` | `isLoading`, `loading`, `isBusy` |
| Flag de save em progresso | `Boolean = false` | `salvando` | `isSaving`, `saving` |
| Erro global | `String? = null` | `erro` | `errorMessage`, `error` |
| Erro por campo | `String? = null` | `erro<NomeCampo>` (ex: `erroNome`) | `nomeError`, `fieldError` |
| Estado de edição | `val get()` | `isEdicao` (computed) | campo `Boolean` redundante |

### Pacotes — regra absoluta

| Conteúdo | Pacote obrigatório |
|---|---|
| ViewModels | `viewmodel` (irmão de `ui/`) |
| UiState de uma feature | `ui.feature.<nome>.state` |
| Eventos e UiEvents de uma feature | `ui.feature.<nome>` |
| Composables reutilizáveis | `ui.components` |
| Rotas e NavHost | `ui.navigation` |

---

## 12. API dos Componentes Compartilhados

> ✅ **SEMPRE** use esses componentes em vez de criar `Scaffold`, `TopAppBar` ou `CircularProgressIndicator` diretamente.  
> ✅ **SEMPRE** passe `snackbarHostState` como **primeiro parâmetro** de `AppScaffold`.  
> ❌ **NUNCA** crie um `Scaffold` nativo dentro de uma feature screen.  
> ❌ **NUNCA** passe `null` explicitamente para `onNavigateBack` — simplesmente omita o parâmetro em telas raiz.

```kotlin
// ✅ AppScaffold — snackbarHostState é SEMPRE o 1º parâmetro
AppScaffold(
    snackbarHostState    : SnackbarHostState = remember { SnackbarHostState() },  // 1º
    topBar               : @Composable () -> Unit = {},
    floatingActionButton : @Composable () -> Unit = {},
    content              : @Composable (PaddingValues) -> Unit,
)

// ✅ AppTopAppBar
// — onNavigateBack = null → tela raiz, sem ícone de voltar
// — onNavigateBack = { onEvent(XEvent.Voltar) } → tela interna
AppTopAppBar(
    title          : String,
    onNavigateBack : (() -> Unit)? = null,
    actions        : @Composable RowScope.() -> Unit = {},
)

// ✅ AppLoading — use quando uiState.carregando == true
AppLoading()

// ✅ EmptyState — use quando lista estiver vazia
EmptyState(message: String, modifier: Modifier = Modifier)
```

---

## 13. Tokens do Design System

> ✅ **SEMPRE** use os tokens de `LocalSpacing` e `LocalIconSize` em vez de valores `dp` literais.  
> ❌ **NUNCA** escreva `16.dp`, `8.dp`, `24.dp` etc. diretamente em código de feature.  
> ❌ **NUNCA** use `MaterialTheme.spacing` ou `MaterialTheme.iconSize` sem que `GerenciadorCartoesTheme` esteja no escopo.

```kotlin
// ✅ SEMPRE: obtendo os tokens
val spacing = LocalSpacing.current    // ou MaterialTheme.spacing

spacing.extraSmall   // 4dp
spacing.small        // 8dp
spacing.smallMedium  // 12dp
spacing.medium       // 16dp  ← mais comum — use para padding padrão de tela
spacing.large        // 24dp
spacing.extraLarge   // 32dp

val iconSize = LocalIconSize.current  // ou MaterialTheme.iconSize

iconSize.extraSmall  // 16dp
iconSize.small       // 20dp
iconSize.medium      // 24dp  ← ícone padrão (NavigationBar, botões, campos)
iconSize.large       // 40dp
iconSize.extraLarge  // 48dp
```

> **Única exceção documentada:** `EmptyState` usa `64.dp` fixo para o ícone ilustrativo — este valor não existe na escala de tokens.

---

## 14. Regras de Módulos Hilt

> ✅ **SEMPRE** use `abstract class` quando o módulo misturar `@Binds` e `@Provides`.  
> ✅ **SEMPRE** coloque os métodos `@Provides` no `companion object` do `abstract class`.  
> ✅ **SEMPRE** use `object` simples quando o módulo tiver apenas `@Provides`.  
> ❌ **NUNCA** use `object` para um módulo que contenha `@Binds` — isso não compila.

```kotlin
// ✅ SEMPRE assim quando mistura @Binds e @Provides (padrão de AppModule)
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds @Singleton
    abstract fun bindXRepository(impl: XRepositoryImpl): XRepository

    companion object {
        @Provides @Singleton
        fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
            Room.databaseBuilder(ctx, AppDatabase::class.java, "gerenciador-cartoes-db").build()
    }
}

// ✅ SEMPRE assim quando só tem @Provides (padrão de NetworkModule)
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun provideRetrofit(...): Retrofit = ...
}
```

---

## 15. Tratamento de Erros

> ✅ **SEMPRE** envolva chamadas ao repository com `viewModelScope.launch { runCatching { } }`.  
> ✅ **SEMPRE** envie o erro para a UI via `_uiEvent.send(XUiEvent.MostrarErro(...))`.  
> ✅ **SEMPRE** redefina `carregando = false` no `.onFailure`.  
> ❌ **NUNCA** use `try/catch` diretamente no ViewModel.  
> ❌ **NUNCA** use `Result<T>` como tipo de retorno no repository.  
> ❌ **NUNCA** deixe uma exceção propagada ao caller sem tratamento.

```kotlin
// ✅ SEMPRE assim
viewModelScope.launch {
    runCatching {
        cartaoRepository.salvar(cartao)            // caminho feliz
        _uiEvent.send(XUiEvent.NavigateBack)
    }.onFailure { erro ->
        _uiState.update { it.copy(carregando = false) }
        _uiEvent.send(XUiEvent.MostrarErro(erro.message ?: "Erro desconhecido"))
    }
}

// ❌ NUNCA assim
try {
    cartaoRepository.salvar(cartao)
} catch (e: Exception) { ... }
```

---

## 16. Validação de Formulário

> ✅ **SEMPRE** valide todos os campos de uma vez (coleção completa de erros).  
> ✅ **SEMPRE** limpe o erro do campo quando o usuário editar esse campo.  
> ✅ **SEMPRE** chame `validar()` antes de `salvar()` e aborte se retornar `false`.  
> ❌ **NUNCA** use short-circuit (retorno antecipado no primeiro erro) na validação.  
> ❌ **NUNCA** valide campos dentro de Composables.

```kotlin
// ✅ SEMPRE assim — validação coletiva no ViewModel
private fun validar(): Boolean {
    var valid = true
    val s = _uiState.value

    if (s.nomeTitular.isBlank()) {
        _uiState.update { it.copy(erroNome = "Nome é obrigatório") }; valid = false
    }
    if (s.finalNumero.length != 4 || !s.finalNumero.all { it.isDigit() }) {
        _uiState.update { it.copy(erroNumero = "Informe os 4 últimos dígitos") }; valid = false
    }
    // ... demais campos — NUNCA retornar antes de verificar todos
    return valid
}

// ✅ SEMPRE limpar o erro ao editar
is XEvent.NomeTitularAlterado ->
    _uiState.update { it.copy(nomeTitular = event.valor, erroNome = null) }

// ✅ SEMPRE limitar finalNumero a 4 chars no handler
is XEvent.FinalNumeroAlterado ->
    _uiState.update { it.copy(finalNumero = event.valor.take(4), erroNumero = null) }
```

---

## 17. Mutação de Estado

> ✅ **SEMPRE** use `_uiState.update { it.copy(...) }`.  
> ❌ **NUNCA** use `_uiState.value = ...` — é proibido sem exceção.

```kotlin
// ✅ CORRETO — sempre
_uiState.update { it.copy(carregando = false, cartoes = lista) }

// ❌ PROIBIDO — nunca, em hipótese alguma
_uiState.value = _uiState.value.copy(carregando = false)
```

---

## 18. Convenções de Preview

> ✅ **SEMPRE** declare Previews como `private`.  
> ✅ **SEMPRE** encapsule em `GerenciadorCartoesTheme { }`.  
> ✅ **SEMPRE** chame `XContent`, nunca `XScreen`.  
> ✅ **SEMPRE** forneça par Light + Dark para Previews com variação visual.  
> ✅ **SEMPRE** cubra pelo menos três variantes: carregando, vazio/erro, preenchido.  
> ❌ **NUNCA** declare um Preview como `public` ou `internal`.

```kotlin
// ✅ SEMPRE assim — par Light+Dark
@Preview(showBackground = true, name = "Lista – Carregando")
@Preview(showBackground = true, name = "Lista – Carregando Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ListaCarregandoPreview() {
    GerenciadorCartoesTheme { ListaContent(uiState = ListaUiState(carregando = true)) }
}

// ❌ NUNCA assim
@Preview
@Composable
fun ListaPreview() {          // public e sem tema
    ListaScreen(...)          // chamando Screen em vez de Content
}
```

---

## 19. Regra de `@file:OptIn`

> ✅ **SEMPRE** coloque `@file:OptIn(ExperimentalMaterial3Api::class)` como primeira linha de `AppTopAppBar.kt`, antes de `package`.  
> ❌ **NUNCA** adicione `@file:OptIn(ExperimentalMaterial3Api::class)` em qualquer outro arquivo — é necessário somente neste.  
> ❌ **NUNCA** use `@OptIn(ExperimentalMaterial3Api::class)` por anotação em `AppTopAppBar.kt` — use apenas o `@file:`.

```kotlin
// ✅ Primeira linha de AppTopAppBar.kt — antes de package
@file:OptIn(ExperimentalMaterial3Api::class)
package com.app.gerenciadorcartoes.ui.components
```

---

## 20. Regras de Documentação

> ✅ **SEMPRE** atualize `docs/AI_CONTEXT.md` após qualquer alteração de código.  
> ✅ **SEMPRE** use a tabela `## Guia de Manutenção` de `AI_CONTEXT.md` para identificar qual seção atualizar.  
> ✅ **SEMPRE** adicione uma linha no `## Histórico de Alterações` com data e descrição objetiva.  
> ❌ **NUNCA** termine uma tarefa de código sem atualizar a documentação correspondente.

**Mapeamento rápido alteração → doc:**

| Alteração no código | Docs a atualizar |
|---|---|
| Nova tela (Screen/Content) | `AI_CONTEXT.md` + `ARCHITECTURE.md` + `PROJECT_CONTEXT.md` |
| Nova rota | `AI_CONTEXT.md` (§Rotas Existentes) + `PROJECT_CONTEXT.md §4` |
| Novo campo em `Cartao` | `AI_CONTEXT.md` (§Modelo de Domínio) + `PROJECT_CONTEXT.md §3` |
| Novo método no DAO / Repository | `AI_CONTEXT.md` (§Contrato do Repository) + `PROJECT_CONTEXT.md §5` |
| Nova dependência no `libs.versions.toml` | `AI_CONTEXT.md` (§Referência de Build) + `PROJECT_CONTEXT.md §8` + `README.md` |
| Novo componente compartilhado | `AI_CONTEXT.md` (§API dos Componentes) + `ARCHITECTURE.md §9` |
| Novo token de design | `AI_CONTEXT.md` (§Tokens do Design System) + `CODING_GUIDELINES.md §14` |

**Formato da entrada no `## Histórico de Alterações`:**
```
- 2026-05-15 — descrição objetiva e técnica da mudança
```
