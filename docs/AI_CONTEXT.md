# AI_CONTEXT.md

> Referência primária para assistentes de IA que trabalham neste repositório.  
> Leia este arquivo por completo antes de gerar ou editar qualquer código.  
> As regras de atualização contínua são definidas em `.github/copilot-instructions.md`.

---

## Histórico de Alterações

<!-- Insira uma linha no início a cada atualização: "- YYYY-MM-DD — descrição" -->
- 2026-05-20 — Splash ajustada: logo reduzida em `ic_splash_logo_static` para melhor proporção visual no lançamento
- 2026-05-19 — Tela `Detalhe` simplificada para leitura: removidas ações de Editar/Excluir do AppBar e limpeza de eventos/UI events relacionados no fluxo da feature
- 2026-05-19 — Botão de 3 pontinhos na Lista atualizado para menu de ações em bottom sheet (Editar e Excluir), evitando popup deslocado sobre/abaixo do card
- 2026-05-19 — Ação de excluir na Lista refinada: ícone de lixeira direto sobre o card substituído por menu discreto (⋮) com opção “Excluir cartão” em dropdown
- 2026-05-19 — Botão + do topo refinado com microdetalhes premium (sombra suave, borda translúcida circular e ícone dimensionado por token de design)
- 2026-05-19 — Estado vazio da Lista refinado com linguagem humana e hierarquia visual (`title` + `subtitle` no `EmptyState`), botão + do topo reforçado com cor primária e fundo em gradiente sutil na tela sem cartões
- 2026-05-19 — ListaScreen alterada para padrão premium sem FAB sobreposto: ação de adicionar movida para o topo (ícone + em `AppTopAppBar.actions`) e texto do estado vazio atualizado
- 2026-05-19 — Adicionado campo `template` ao modelo de domínio `Cartao` e a `CartaoEntity`; migração Room v1→v2; criado `CartaoTemplateCard` e `CartaoTemplateMini` em `ui/components/`; ListaScreen e CadastrarAlterarScreen atualizados com seletor de template visual (Bradesco, Itaú, Nubank, Inter, C6 Bank, Padrão)
- 2026-05-19 — Downgrade do Android Gradle Plugin para 9.0.0 para compatibilidade com Android Studio; seção de build atualizada
- 2026-05-16 — Adicionada feature Login: LoginUiState, LoginEvent, LoginUiEvent, LoginScreen, LoginViewModel, LoginRoute; AppNavHost atualizado com LoginRoute como startDestination
- 2026-05-15 — Revisão completa: corrigidos tokens IconSize (extraSmall/small/extraLarge), adicionado smallMedium ao Spacing, corrigida Matriz de Estratégia (Repository em vez de DAO, tipos Cartao), corrigida ordem de parâmetros do AppScaffold, documentados CadastrarAlterarUiState.salvando e isEdicao
- 2026-05-15 — Tradução completa para pt-BR; todos os arquivos da pasta docs/ atualizados
- 2026-05-15 — Adicionados Guia de Manutenção e Histórico de Alterações; criado .github/copilot-instructions.md para aplicação contínua das regras de atualização
- 2026-05-15 — Geração inicial a partir de análise completa do código-fonte

---

## Informações Rápidas

| Campo | Valor |
|---|---|
| Projeto | `GerenciadorCartoes` — aplicativo Android de CRUD (cartões de crédito/débito) |
| Pacote raiz | `com.app.gerenciadorcartoes` |
| Application ID | `com.app.gerenciadorcartoes` |
| Linguagem | Kotlin 2.3.21 (compilador K2) |
| Kit de UI | Jetpack Compose + Material 3 |
| Arquitetura | MVVM · Fluxo de Dados Unidirecional · Separação em camadas |
| DI | Hilt 2.59.2 (KSP — sem KAPT) |
| Persistência | Room 2.8.4 (reativo via `Flow`) |
| Navegação | Navigation Compose 2.9.0 — **apenas rotas type-safe** |
| Min SDK | 28 (Android 9 Pie) |
| Target/Compile SDK | 36 |
| Build | Módulo único `:app`; Java 17; `buildConfig = true` |

---

## Árvore de Fontes (apenas `:app` — fontes geradas excluídas)

```
app/src/main/java/com/app/gerenciadorcartoes/
│
├── GerenciadorCartoesApp.kt          @HiltAndroidApp — ponto de entrada do DI
├── MainActivity.kt                   @AndroidEntryPoint · enableEdgeToEdge()
│
├── model/
│   └── Cartao.kt                     Modelo de domínio puro — zero importações Android/Room
│
├── data/local/
│   ├── converter/                    (vazio — reservado para @TypeConverters)
│   ├── dao/CartaoDao.kt              @Dao — leituras reativas com Flow + escritas suspend
│   ├── database/AppDatabase.kt       @Database v1 — expõe cartaoDao()
│   └── entity/CartaoEntity.kt        @Entity("cartoes")
│
├── repository/
│   ├── CartaoRepository.kt           Interface — apenas tipos de domínio
│   ├── CartaoRepositoryImpl.kt       @Singleton — delega para CartaoDao + mapper
│   └── mapper/CartaoMapper.kt        funções de extensão toDomain() / toEntity()
│
├── network/
│   └── service/ApiService.kt         Placeholder Retrofit — sem endpoints ativos
│
├── di/
│   ├── AppModule.kt                  abstract class — @Binds + @Provides no companion
│   └── NetworkModule.kt              object — apenas @Provides
│
└── ui/
    ├── theme/                        Color · Type · Shape · Spacing · IconSize · Theme
    ├── components/                   AppScaffold · AppTopAppBar · AppLoading · EmptyState
    ├── navigation/                   Routes.kt · AppNavHost.kt
    ├── feature/
    │   ├── login/                    LoginEvent · LoginUiEvent · LoginScreen · state/LoginUiState
    │   ├── lista/                    ListaEvent · ListaUiEvent · ListaScreen · state/ListaUiState
    │   ├── detalhe/                  DetalheEvent · DetalheUiEvent · DetalheScreen · state/DetalheUiState
    │   └── cadastraralterar/         CadastrarAlterarEvent · CadastrarAlterarUiEvent
    │                                 CadastrarAlterarScreen · state/CadastrarAlterarUiState
    └── viewmodel/
        ├── LoginViewModel.kt
        ├── ListaViewModel.kt
        ├── DetalheViewModel.kt
        └── CadastrarAlterarViewModel.kt
```

> **ViewModels ficam em `viewmodel/`** — irmão de `ui/`, não dentro dela.  
> **Arquivos UiState ficam em `ui/feature/<name>/state/`**.

---

## Modelo de Domínio

```kotlin
// model/Cartao.kt — NO framework imports
data class Cartao(
    val id          : Long   = 0L,
    val nomeTitular : String = "",
    val finalNumero : String = "",   // last 4 digits — e.g. "1234"
    val bandeira    : String = "",   // "Visa" | "Mastercard" | "Elo" | etc.
    val validade    : String = "",   // "MM/AA" — e.g. "12/28"
    val limite      : Double = 0.0,  // BRL credit limit
    val template    : String = "default", // card visual: "default"|"bradesco"|"itau"|"nubank"|"inter"|"c6bank"
)
```

Todos os campos têm valores padrão → `Cartao()` é sempre um estado inicial válido.

---

## Regras de Camadas (invariantes — nunca viole estas)

| Regra | Detalhe |
|---|---|
| `model/Cartao.kt` tem zero importações de framework | Sem `@Entity`, sem `@SerialName`, sem classes Android |
| A interface `CartaoRepository` usa apenas `Cartao` / primitivos | Sem `CartaoEntity`, sem tipos Room/Retrofit |
| `CartaoRepositoryImpl` é a **única** classe que importa `CartaoEntity` ou `CartaoDao` | Os mappers são chamados aqui e em nenhum outro lugar |
| ViewModels só importam `CartaoRepository` (interface) | Nunca importar `Dao` ou `Entity` diretamente |
| A camada de UI nunca muta o estado diretamente | Sempre despacha `XEvent` via `onEvent()` |
| `XScreen` é dono do ViewModel; `XContent` é puro | Previews sempre chamam `XContent`, nunca `XScreen` |
| Eventos pontuais de navegação / Snackbar passam pelo `Channel<XUiEvent>` | Nunca usar `StateFlow` para efeitos pontuais |

---

## Convenções de Nomenclatura (tabela de consulta)

| Tipo | Padrão | Exemplo |
|---|---|---|
| Modelo de domínio | `<Entidade>` | `Cartao` |
| Entidade Room | `<Entidade>Entity` | `CartaoEntity` |
| DAO Room | `<Entidade>Dao` | `CartaoDao` |
| Banco de dados Room | `AppDatabase` | `AppDatabase` |
| Interface Repository | `<Entidade>Repository` | `CartaoRepository` |
| Implementação do Repository | `<Entidade>RepositoryImpl` | `CartaoRepositoryImpl` |
| Serviço Retrofit | `ApiService` | `ApiService` |
| ViewModel | `<Feature>ViewModel` | `ListaViewModel` |
| Estado da UI | `<Feature>UiState` | `ListaUiState` |
| Evento do usuário (UI → VM) | `<Feature>Event` | `ListaEvent` |
| Efeito pontual (VM → UI) | `<Feature>UiEvent` | `ListaUiEvent` |
| Rota de navegação | `<Feature>Route` | `DetalheRoute` |
| Composable de tela | `<Feature>Screen` | `ListaScreen` |
| Composable de conteúdo | `<Feature>Content` | `ListaContent` |
| Função de Preview | `<Feature><Variante>Preview` (private) | `ListaComItensPreview` |
| Composable interno | `private fun` descritivo | `CartaoItem`, `DetalheRow` |

Nomes de pacotes seguem exatamente a estrutura de diretórios — todos em minúsculas.

---

## Regra de Nomenclatura de Arquivos

Uma declaração pública por arquivo; o nome do arquivo deve corresponder exatamente a essa declaração.  
Exceção: `<Feature>Screen.kt` pode conter `XScreen` + `XContent` + todos os seus Previews.

---

## Template de ViewModel

```kotlin
@HiltViewModel
class XViewModel @Inject constructor(
    savedStateHandle         : SavedStateHandle,        // omit if no route params
    private val cartaoRepository: CartaoRepository,
) : ViewModel() {

    private val route : XRoute = savedStateHandle.toRoute()   // omit if no params
    private val id    : Long   = route.id                     // omit if no params

    private val _uiState = MutableStateFlow(XUiState())
    val uiState: StateFlow<XUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<XUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    init { /* start reactive observation or one-shot load */ }

    fun onEvent(event: XEvent) {
        when (event) {
            // exhaustive — no else branch for sealed interfaces
        }
    }

    private fun privateBusinessFunction() {
        viewModelScope.launch {
            runCatching {
                // DAO / Repository call
            }.onFailure { erro ->
                _uiState.update { it.copy(carregando = false) }
                _uiEvent.send(XUiEvent.MostrarErro(erro.message ?: "Erro desconhecido"))
            }
        }
    }
}
```

**Regras fundamentais:**
- `_uiState` e `_uiEvent` são sempre privados.
- Mutação de estado é sempre via `_uiState.update { it.copy(...) }` — nunca `_uiState.value = ...`.
- Todos os caminhos de erro enviam `MostrarErro` via `Channel`.
- Flag de carregamento: `carregando: Boolean` (não `isLoading`).

---

## Matriz de Estratégia de Leitura

| ViewModel | Método do Repository | Motivo |
|---|---|---|
| `ListaViewModel` | `observarTodos(): Flow<List<Cartao>>` | A lista deve refletir toda escrita em tempo real |
| `DetalheViewModel` | `observarPorId(id): Flow<Cartao?>` | Re-emite após voltar da edição; emissão `null` → `NavigateBack` automático |
| `CadastrarAlterarViewModel` | `suspend buscarPorId(id): Cartao?` | Pré-preenchimento do formulário é pontual — sem necessidade de observação reativa durante a edição |

---

## Template Screen / Content / Preview

```kotlin
// ── Tier 1: ViewModel owner ──────────────────────────────────────────────────
@Composable
fun XScreen(
    navigateBack    : () -> Unit,               // navigation lambdas from NavHost
    onNavigateTo... : (...) -> Unit,
    viewModel       : XViewModel = hiltViewModel(),
) {
    val uiState          by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                XUiEvent.NavigateBack          -> navigateBack()
                is XUiEvent.MostrarErro        -> snackbarHostState.showSnackbar(event.mensagem)
                // ...
            }
        }
    }

    XContent(
        uiState           = uiState,
        snackbarHostState = snackbarHostState,
        onEvent           = viewModel::onEvent,
    )
}

// ── Tier 2: Pure composable ────────────────────────────────────────────────────
@Composable
fun XContent(
    uiState           : XUiState          = XUiState(),
    snackbarHostState : SnackbarHostState = remember { SnackbarHostState() },
    onEvent           : (XEvent) -> Unit  = {},
) {
    AppScaffold(
        topBar = { AppTopAppBar(title = "Título", onNavigateBack = { onEvent(XEvent.Voltar) }) },
        snackbarHostState = snackbarHostState,
    ) { padding ->
        when {
            uiState.carregando -> AppLoading()
            // ...render content...
        }
    }
}

// ── Tier 3: Previews ───────────────────────────────────────────────────────────
@Preview(showBackground = true, name = "X – Light")
@Preview(showBackground = true, name = "X – Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun XPreview() {
    GerenciadorCartoesTheme { XContent() }
}
```

---

## Template de UiState

```kotlin
// ui/feature/<name>/state/XUiState.kt
data class XUiState(
    val carregando : Boolean  = false,    // loading flag — always "carregando"
    val erro       : String?  = null,     // nullable — null = no error
    // feature-specific fields with defaults:
    val cartao     : Cartao   = Cartao(),
    // form fields use String so they bind directly to OutlinedTextField:
    val nomeTitular : String  = "",
    val erroNome    : String? = null,     // per-field error: "erro<FieldName>"
)
```

**Casos especiais observados em `CadastrarAlterarUiState`:**
- `salvando: Boolean = false` — flag exclusiva para o progresso da operação de salvar (distinto de `carregando`, que representa o carregamento inicial dos dados).
- `isEdicao: Boolean` — propriedade computada (`get() = nomeTitular.isNotBlank()`) que indica se o formulário está em modo edição.
- Não possui campo `erro: String?` global; usa erros por campo (`erroNome`, `erroNumero`, etc.).

---

## Templates de Evento

```kotlin
// XEvent.kt  (user intent — UI → ViewModel)
sealed interface XEvent {
    data object Voltar                       : XEvent   // no data → data object
    data object Salvar                       : XEvent
    data class  NomeTitularAlterado(val valor: String) : XEvent  // has data → data class
    data class  Item(val id: Long)           : XEvent
}

// XUiEvent.kt  (one-shot effects — ViewModel → UI)
sealed interface XUiEvent {
    data object NavigateBack                 : XUiEvent
    data class  MostrarErro(val mensagem: String) : XUiEvent
    data class  MostrarMensagem(val mensagem: String) : XUiEvent
}
```

---

## Template de Rotas de Navegação

```kotlin
// ui/navigation/Routes.kt
@Serializable object NoParamsRoute                            // no params → object
@Serializable data class WithParamRoute(val id: Long)        // has params → data class
@Serializable data class OptionalParamRoute(val id: Long = 0L)  // 0 = create, >0 = edit
```

Extração de rota no ViewModel (sem Bundle, sem chaves string):
```kotlin
private val route: XRoute = savedStateHandle.toRoute()
private val id: Long = route.id
```

Registro no NavHost:
```kotlin
composable<XRoute> { XScreen(navigateBack = { navController.popBackStack() }) }
```

---

## Rotas Existentes

| Objeto/Classe | Significado | Início? |
|---|---|---|
| `ListaRoute` | Lista de todos os cartões | ✅ sim |
| `DetalheRoute(id: Long)` | Detalhe somente leitura | não |
| `CadastrarAlterarRoute(id: Long = 0L)` | Criar (`id=0`) ou editar (`id>0`) | não |

---

## Contrato do Repository

```kotlin
interface CartaoRepository {
    fun observarTodos() : Flow<List<Cartao>>    // reactive — no suspend
    fun observarPorId(id: Long) : Flow<Cartao?> // reactive — null when deleted
    suspend fun buscarPorId(id: Long) : Cartao?
    suspend fun salvar(cartao: Cartao) : Long    // returns generated id
    suspend fun atualizar(cartao: Cartao)
    suspend fun excluirPorId(id: Long)
}
```

---

## Regras de Módulo Hilt

| Cenário | Tipo de módulo | Motivo |
|---|---|---|
| Precisa de `@Binds` e `@Provides` | `abstract class` + `companion object` | Dagger exige abstract class para `@Binds` |
| Precisa apenas de `@Provides` | `object` | Sem `@Binds` — não é necessário abstract |

Exemplo de módulo misto (`AppModule`):
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds @Singleton
    abstract fun bindCartaoRepository(impl: CartaoRepositoryImpl): CartaoRepository

    companion object {
        @Provides @Singleton
        fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
            Room.databaseBuilder(ctx, AppDatabase::class.java, "gerenciador-cartoes-db").build()

        @Provides @Singleton
        fun provideCartaoDao(db: AppDatabase): CartaoDao = db.cartaoDao()
    }
}
```

---

## Tokens do Design System

```kotlin
// Spacing — use instead of hardcoded dp in feature code
val spacing = LocalSpacing.current         // or MaterialTheme.spacing
spacing.extraSmall   // 4dp
spacing.small        // 8dp
spacing.smallMedium  // 12dp
spacing.medium       // 16dp  ← most common
spacing.large        // 24dp
spacing.extraLarge   // 32dp

// Also accessible as MaterialTheme.spacing.medium

// Icon sizes
val iconSize = LocalIconSize.current       // or MaterialTheme.iconSize
iconSize.extraSmall  // 16dp
iconSize.small       // 20dp
iconSize.medium      // 24dp  ← standard icon slot
iconSize.large       // 40dp
iconSize.extraLarge  // 48dp
```

Nunca use valores de `dp` fixos (`16.dp`, `24.dp` etc.) em telas de funcionalidade ou componentes compartilhados.  
Exceção: `EmptyState` usa `64.dp` para o ícone ilustrativo (fora da escala dos tokens).

---

## API dos Componentes Compartilhados

```kotlin
// AppScaffold — wraps Material3 Scaffold + SnackbarHost
AppScaffold(
    snackbarHostState    : SnackbarHostState = ...,       // 1º parâmetro
    topBar               : @Composable () -> Unit = {},
    floatingActionButton : @Composable () -> Unit = {},
    content              : @Composable (PaddingValues) -> Unit,
)

// AppTopAppBar — wraps Material3 TopAppBar
// @file:OptIn(ExperimentalMaterial3Api::class) required at top of AppTopAppBar.kt only
AppTopAppBar(
    title          : String,
    onNavigateBack : (() -> Unit)? = null,    // null → no back arrow (root screen)
    actions        : @Composable RowScope.() -> Unit = {},
)

// AppLoading — full-screen CircularProgressIndicator
AppLoading()   // no params

// EmptyState — full-screen icon + message
EmptyState(message: String)
```

---

## Padrão Mapper

```kotlin
// repository/mapper/CartaoMapper.kt
fun CartaoEntity.toDomain(): Cartao = Cartao(id = id, nomeTitular = nomeTitular, ...)
fun Cartao.toEntity(): CartaoEntity = CartaoEntity(id = id, nomeTitular = nomeTitular, ...)
```

**Regras:** mapeamento campo a campo; sem lógica; sem defaults; chamado apenas dentro de `CartaoRepositoryImpl`.

---

## OptIn no Nível de Arquivo

O único arquivo que requer `@file:OptIn` é `AppTopAppBar.kt`, por causa da API experimental de `TopAppBar`:

```kotlin
// First line — before package declaration
@file:OptIn(ExperimentalMaterial3Api::class)
package com.app.gerenciadorcartoes.ui.components
```

Este é o único arquivo do projeto que requer essa anotação.

---

## Convenções de Validação de Formulário

- A validação ocorre no ViewModel (`private fun validar(): Boolean`).
- **Coletiva** — não interrompe no primeiro erro; todos os erros de campo são definidos simultaneamente.
- Erro por campo armazenado como `erro<NomeCampo>: String?` no `UiState`.
- O erro é limpo para `null` quando o usuário edita o campo correspondente.
- Campos numéricos do formulário são armazenados como `String` no `UiState` (vinculam diretamente ao `OutlinedTextField`).
- `finalNumero` é limitado a 4 caracteres no nível do manipulador de eventos: `event.valor.take(4)`.

---

## Referência de Build e Dependências

Gerenciado via `gradle/libs.versions.toml`. Entradas principais:

| Alias | Artefato | Versão |
|---|---|---|
| `libs.hilt.android` | `com.google.dagger:hilt-android` | 2.59.2 |
| `libs.hilt.compiler` | `com.google.dagger:hilt-android-compiler` (ksp) | 2.59.2 |
| `libs.androidx.room.runtime` | `androidx.room:room-runtime` | 2.8.4 |
| `libs.androidx.room.ktx` | `androidx.room:room-ktx` | 2.8.4 |
| `libs.androidx.room.compiler` | `androidx.room:room-compiler` (ksp) | 2.8.4 |
| `libs.androidx.navigation.compose` | `androidx.navigation:navigation-compose` | 2.9.0 |
| `libs.androidx.hilt.navigation.compose` | `androidx.hilt:hilt-navigation-compose` | 1.2.0 |
| `libs.retrofit` | `com.squareup.retrofit2:retrofit` | 3.0.0 |
| `libs.okhttp.logging.interceptor` | `com.squareup.okhttp3:logging-interceptor` | 5.3.2 |
| `libs.kotlinx.serialization.json` | `org.jetbrains.kotlinx:kotlinx-serialization-json` | 1.11.0 |

Plugins ativos em `:app`:
- `alias(libs.plugins.hilt.android)`
- `alias(libs.plugins.ksp)`
- `alias(libs.plugins.kotlin.serialization)`
- `alias(libs.plugins.kotlin.compose)`

Plugin Android:
- `com.android.application` (`libs.plugins.android.application`) = `9.0.0`

---

## Anti-Padrões — Nunca Faça Isso

| ❌ Anti-padrão | ✅ Correto |
|---|---|
| `_uiState.value = newState` | `_uiState.update { it.copy(...) }` |
| `StateFlow` para eventos de navegação | `Channel<XUiEvent>(BUFFERED)` |
| `else -> {}` em `when` sobre sealed interface | `when` exaustivo — listar todas as variantes |
| Chamar `hiltViewModel()` em `XContent` | `hiltViewModel()` apenas em `XScreen` |
| `@Preview` chamando `XScreen` | `@Preview` sempre chama `XContent` |
| `@Preview` sem o wrapper `GerenciadorCartoesTheme {}` | Sempre encapsular |
| `CartaoEntity` referenciado no ViewModel | Usar apenas `Cartao` (modelo de domínio) |
| `CartaoDao` injetado diretamente no ViewModel | Injetar `CartaoRepository` (interface) |
| Valor fixo `16.dp` de espaçamento no código de feature | `LocalSpacing.current.medium` |
| `buscarPorId()` no DetalheViewModel | `observarPorId()` (reativo — auto-atualiza após edição) |
| `observarPorId()` no CadastrarAlterarViewModel | `buscarPorId()` (pontual — pré-preenchimento do formulário) |

---

## Como Adicionar uma Nova Funcionalidade (checklist)

1. **Modelo** — adicionar campos em `Cartao.kt` se necessário; atualizar `CartaoEntity.kt`; atualizar o mapper.
2. **DAO** — adicionar métodos `@Query` em `CartaoDao.kt`.
3. **Repository** — adicionar assinatura do método em `CartaoRepository.kt`; implementar em `CartaoRepositoryImpl.kt`.
4. **Rota** — adicionar `@Serializable` object/data class em `Routes.kt`.
5. **Eventos** — criar `<Feature>Event.kt` e `<Feature>UiEvent.kt`.
6. **UiState** — criar `ui/feature/<nome>/state/<Feature>UiState.kt`.
7. **ViewModel** — criar `viewmodel/<Feature>ViewModel.kt` seguindo o template acima.
8. **Screen** — criar `ui/feature/<nome>/<Feature>Screen.kt` com `XScreen` + `XContent` + `@Preview`.
9. **NavHost** — adicionar `composable<XRoute> { ... }` em `AppNavHost.kt`.
10. **Navegação** — conectar as lambdas de navegação na entrada do NavHost da tela chamadora.

---

## Documentação Relacionada

| Arquivo | Conteúdo |
|---|---|
| `docs/PROJECT_CONTEXT.md` | Identificação completa do projeto, modelo de domínio, lista de funcionalidades, especificação de persistência, camada de rede, ferramentas de build, design system |
| `docs/ARCHITECTURE.md` | Separação camada a camada, diagrama de fluxo de dados, padrão Screen/Content/Preview, justificativa Event/UiEvent, Navigation Compose 2, grafo DI Hilt, cadeia reativa |
| `docs/CODING_GUIDELINES.md` | 16 seções: regras de nomenclatura de pacotes/arquivos/classes, estilo Kotlin, sealed interfaces, design de UiState, estrutura de ViewModel, contrato do repository, regras de mapper, módulos Hilt, rotas de navegação, parâmetros de composable, convenções de Preview, tokens de design, `@file:OptIn`, validação de formulário |
| `README.md` | Instruções de configuração, diagrama de arquitetura, guia de camadas para desenvolvedores, descrição das bibliotecas |
| `.github/copilot-instructions.md` | Regras persistentes de IA: mandato de atualização, regras de geração de código, invariantes de camadas, anti-padrões |

---

## Guia de Manutenção

> Para assistentes de IA: consulte esta seção ao terminar uma tarefa para decidir o que atualizar.

### Qual seção atualizar para cada tipo de alteração

| Alteração | Seção em `AI_CONTEXT.md` | Doc companion |
|---|---|---|
| Nova tela de funcionalidade | `Árvore de Fontes` · `Rotas Existentes` · `Como Adicionar uma Nova Funcionalidade` | `PROJECT_CONTEXT.md §4` · `ARCHITECTURE.md §3` |
| Nova rota de navegação | `Árvore de Fontes` · `Rotas Existentes` | `PROJECT_CONTEXT.md §4` · `ARCHITECTURE.md §5` |
| Novo campo em `Cartao` | `Modelo de Domínio` | `PROJECT_CONTEXT.md §3` · `ARCHITECTURE.md §1` |
| Novo método em `CartaoDao` | `Contrato do Repository` · `Matriz de Estratégia de Leitura` | `PROJECT_CONTEXT.md §5` · `ARCHITECTURE.md §2` |
| Novo método em `CartaoRepository` | `Contrato do Repository` | `ARCHITECTURE.md §3` · `CODING_GUIDELINES.md §8` |
| Novo módulo / binding Hilt | `Regras de Módulo Hilt` · `Árvore de Fontes` | `PROJECT_CONTEXT.md §7` · `ARCHITECTURE.md §6` |
| Novo componente compartilhado | `API dos Componentes Compartilhados` · `Árvore de Fontes` | `ARCHITECTURE.md §9` |
| Novo token de design | `Tokens do Design System` | `ARCHITECTURE.md §10` · `CODING_GUIDELINES.md §14` |
| Dependência adicionada / atualizada | `Referência de Build e Dependências` · `Informações Rápidas` | `PROJECT_CONTEXT.md §8` · `README.md` |
| Novo padrão / convenção Kotlin | Seção de template correspondente · `Anti-Padrões` | `CODING_GUIDELINES.md` (adicionar nova seção) |
| Mudança arquitetural (nova camada / pacote) | `Árvore de Fontes` · `Regras de Camadas (invariantes)` · `Como Adicionar uma Nova Funcionalidade` | `ARCHITECTURE.md` · `PROJECT_CONTEXT.md §10` |
| Mudança de Min/Target SDK | `Informações Rápidas` | `PROJECT_CONTEXT.md §1` |
| Mudança no schema do banco (nova entidade / migration) | `Árvore de Fontes` · `Modelo de Domínio` | `PROJECT_CONTEXT.md §5` · `ARCHITECTURE.md §2` |

### Procedimento de atualização (executar após cada tarefa)

1. Identificar o(s) tipo(s) de alteração na tabela acima.
2. Editar cada seção listada — manter o conteúdo factual e fundamentado no código real.
3. Inserir uma linha no início de `## Histórico de Alterações` com a data de hoje.
4. Atualizar os docs companion listados na terceira coluna.
5. **Não** adicionar conteúdo aspiracional ou futuro — apenas o que existe no código-fonte.

### Como deve ser uma entrada no `## Histórico de Alterações`

```
- 2026-05-15 — Adicionado observarPorId() reativo ao CartaoDao; Matriz de Estratégia de Leitura atualizada
- 2026-05-15 — Nova funcionalidade: tela CadastrarAlterar; Árvore de Fontes e Rotas Existentes atualizadas
```
