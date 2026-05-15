# CODING_GUIDELINES.md

> Gerado a partir de análise do código-fonte — `2026-05-15`.  
> Todos os padrões descritos aqui são **observados em arquivos reais do projeto**, não são regras aspiracionais.

---

## 1. Nomenclatura de Pacotes

**Pacote raiz:** `com.app.gerenciadorcartoes`

Todos os pacotes estão em **letras minúsculas com notação de ponto**, refletindo a estrutura de diretórios:

| Pacote | Conteúdo |
|---|---|
| `com.app.gerenciadorcartoes` | Classe de aplicação, MainActivity |
| `com.app.gerenciadorcartoes.model` | Modelos de domínio (sem dependências Android) |
| `com.app.gerenciadorcartoes.data.local.entity` | Classes `@Entity` do Room |
| `com.app.gerenciadorcartoes.data.local.dao` | Interfaces `@Dao` do Room |
| `com.app.gerenciadorcartoes.data.local.database` | Subclasse de `RoomDatabase` |
| `com.app.gerenciadorcartoes.data.local.converter` | `@TypeConverters` do Room (diretório existe, atualmente vazio) |
| `com.app.gerenciadorcartoes.repository` | Interface e implementação do Repository |
| `com.app.gerenciadorcartoes.repository.mapper` | Funções de extensão de mapeamento |
| `com.app.gerenciadorcartoes.network.service` | Interfaces de serviço Retrofit |
| `com.app.gerenciadorcartoes.di` | Módulos Hilt |
| `com.app.gerenciadorcartoes.ui.theme` | Tokens do design system |
| `com.app.gerenciadorcartoes.ui.components` | Composables reutilizáveis (agnósticos de feature) |
| `com.app.gerenciadorcartoes.ui.navigation` | Rotas + NavHost |
| `com.app.gerenciadorcartoes.ui.feature.<nome>` | Arquivos de UI específicos de feature |
| `com.app.gerenciadorcartoes.ui.feature.<nome>.state` | Data class de UiState |
| `com.app.gerenciadorcartoes.viewmodel` | Todos os ViewModels (o pacote é **irmão de `ui/`**, não está dentro dela) |

---

## 2. Nomenclatura de Arquivos

**Regra: o nome do arquivo deve corresponder exatamente à única declaração pública que ele contém.**

| Arquivo de exemplo | Declaração pública |
|---|---|
| `Cartao.kt` | `data class Cartao` |
| `CartaoEntity.kt` | `data class CartaoEntity` |
| `CartaoDao.kt` | `interface CartaoDao` |
| `CartaoRepository.kt` | `interface CartaoRepository` |
| `CartaoRepositoryImpl.kt` | `class CartaoRepositoryImpl` |
| `CartaoMapper.kt` | funções de extensão `toDomain()` / `toEntity()` |
| `ListaScreen.kt` | `ListaScreen()` + `ListaContent()` + previews (mesma feature) |
| `ListaEvent.kt` | `sealed interface ListaEvent` |
| `ListaUiEvent.kt` | `sealed interface ListaUiEvent` |
| `ListaUiState.kt` | `data class ListaUiState` |
| `ListaViewModel.kt` | `class ListaViewModel` |

---

## 3. Nomenclatura de Classes

### Domínio e dados

| Tipo de classe | Sufixo | Exemplo |
|---|---|---|
| Modelo de domínio | _(nenhum)_ | `Cartao` |
| Entidade Room | `Entity` | `CartaoEntity` |
| DAO Room | `Dao` | `CartaoDao` |
| Banco de dados Room | `Database` | `AppDatabase` |
| Interface Repository | `Repository` | `CartaoRepository` |
| Implementação do Repository | `RepositoryImpl` | `CartaoRepositoryImpl` |
| Serviço Retrofit | `Service` | `ApiService` |

### UI e ViewModel

| Tipo de classe | Sufixo | Exemplo |
|---|---|---|
| ViewModel | `ViewModel` | `ListaViewModel` |
| Estado da UI | `UiState` | `ListaUiState` |
| Evento do usuário (UI → VM) | `Event` | `ListaEvent` |
| Efeito pontual (VM → UI) | `UiEvent` | `ListaUiEvent` |
| Rota de navegação | `Route` | `DetalheRoute`, `ListaRoute` |

### Funções Composable

- Função **Screen**: `<Feature>Screen` — é dona do ViewModel
- Função **Content**: `<Feature>Content` — pura, recebe estado
- **Composables internos**: `private fun` no mesmo arquivo, nomeados pela responsabilidade (ex.: `CartaoItem`, `DetalheBody`, `FormularioBody`, `DetalheRow`)
- **Previews**: `private fun <Feature><Variante>Preview()` — sempre `private`

---

## 4. Estilo Kotlin

### Alinhamento de propriedades

Propriedades de tipos similares são alinhadas verticalmente para melhorar a legibilidade:

```kotlin
// Observado em CartaoEntity.kt, Cartao.kt, Spacing.kt, arquivos UiState
data class Cartao(
    val id          : Long   = 0L,
    val nomeTitular : String = "",
    val finalNumero : String = "",
    val bandeira    : String = "",
    val validade    : String = "",
    val limite      : Double = 0.0,
)
```

### Expressão `when` — sealed interfaces exaustivas

Todo `when` sobre uma sealed interface é exaustivo (sem branch `else`):

```kotlin
// ListaViewModel.kt
when (event) {
    is ListaEvent.NavegaParaItem ->
        viewModelScope.launch { _uiEvent.send(ListaUiEvent.NavegaParaItem(event.id)) }
    ListaEvent.NavegaParaNovo ->
        viewModelScope.launch { _uiEvent.send(ListaUiEvent.NavegaParaNovo) }
    is ListaEvent.ExcluirCartao -> excluir(event.id)
}
```

### `runCatching` para tratamento de erros em coroutines

Todas as chamadas ao repository dentro de `viewModelScope.launch` usam `runCatching { }.onFailure { }`:

```kotlin
// Padrão utilizado nos três ViewModels
viewModelScope.launch {
    runCatching {
        // caminho feliz
    }.onFailure { erro ->
        _uiEvent.send(XUiEvent.MostrarErro(erro.message ?: "Erro desconhecido"))
    }
}
```

### `_uiState.update { it.copy(...) }` — nunca reatribuir

O estado é sempre mutado via `update`, nunca substituído com `_uiState.value = ...`:

```kotlin
_uiState.update { it.copy(cartoes = cartoes, carregando = false) }
```

---

## 5. Convenções de Sealed Interface

Todos os tipos de evento são `sealed interface` (não `sealed class`). As variantes usam `data object` para singletons e `data class` para portadores de dados:

```kotlin
sealed interface ListaEvent {
    data object NavegaParaNovo               : ListaEvent   // singleton — sem dados
    data class  NavegaParaItem(val id: Long) : ListaEvent   // portador — tem dados
    data class  ExcluirCartao(val id: Long)  : ListaEvent
}
```

**Regra:** se uma variante carrega dados, é uma `data class`. Se não tem payload, é um `data object`.

---

## 6. Design de UiState

Todas as classes `UiState` são **`data class` imutáveis** com valores padrão em todos os campos:

```kotlin
data class ListaUiState(
    val cartoes    : List<Cartao> = emptyList(),
    val carregando : Boolean      = false,
    val erro       : String?      = null,
)
```

**Regras observadas:**
- todo campo tem um padrão — `UiState()` é sempre um estado inicial válido
- `carregando: Boolean` é a flag de carregamento (não `isLoading`)
- `erro: String?` é nullable — `null` significa sem erro
- UiStates de formulário armazenam campos numéricos como `String` para vincular diretamente ao `OutlinedTextField.value`
- erros de validação por campo são campos `String?` nomeados `erro<NomeCampo>` (ex.: `erroNome`, `erroBandeira`)
- formulários com operação de salvar assíncrona usam um campo separado `salvando: Boolean = false` (ver `CadastrarAlterarUiState`) — distinto de `carregando`, que rastreia o carregamento inicial dos dados
- propriedades computadas (ex.: `val isEdicao: Boolean get() = ...`) podem ser declaradas no corpo da `data class` para derivar estado sem custo de campo adicional

---

## 7. Estrutura de ViewModel

Cada ViewModel segue exatamente este layout (observado nos três):

```kotlin
@HiltViewModel
class XViewModel @Inject constructor(
    savedStateHandle         : SavedStateHandle,   // omitir se sem parâmetros de rota
    private val cartaoRepository: CartaoRepository,
) : ViewModel() {

    // 1. Extração de rota (quando parâmetros de rota são necessários)
    private val route : XRoute = savedStateHandle.toRoute()
    private val id    : Long   = route.id

    // 2. Estado
    private val _uiState = MutableStateFlow(XUiState(...))
    val uiState: StateFlow<XUiState> = _uiState.asStateFlow()

    // 3. Channel para eventos pontuais
    private val _uiEvent = Channel<XUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    // 4. Inicialização
    init { /* inicia coleta ou carregamento */ }

    // 5. Manipulador público de eventos
    fun onEvent(event: XEvent) { when (event) { ... } }

    // 6. Funções de negócio privadas
    private fun excluir() { ... }
    private fun salvar()  { ... }
    private fun validar(): Boolean { ... }
}
```

**Propriedades expostas:**
- `uiState` é `StateFlow<XUiState>` (visão imutável via `asStateFlow()`)
- `uiEvent` é `Flow<XUiEvent>` (via `receiveAsFlow()`)
- `_uiState` e `_uiEvent` mutáveis são privados

---

## 8. Contrato da Interface Repository

A interface declara **apenas tipos de domínio** — sem tipos do Room ou Retrofit:

```kotlin
interface CartaoRepository {
    fun observarTodos(): Flow<List<Cartao>>        // reativo — sem suspend
    fun observarPorId(id: Long): Flow<Cartao?>     // reativo — sem suspend
    suspend fun buscarPorId(id: Long): Cartao?     // leitura pontual
    suspend fun salvar(cartao: Cartao): Long        // retorna o id gerado
    suspend fun atualizar(cartao: Cartao)
    suspend fun excluirPorId(id: Long)
}
```

**Regras:**
- leituras reativas retornam `Flow<T>` — sem `suspend`
- todas as escritas são `suspend`
- os parâmetros e tipos de retorno da interface referenciam apenas classes de domínio (`Cartao`, `Long`, `Boolean`)
- a implementação (`CartaoRepositoryImpl`) é a **única classe que importa `CartaoEntity` ou `CartaoDao`**

---

## 9. Funções de Mapper

Os mappers são **funções de extensão** no tipo de origem, localizados em `repository/mapper/CartaoMapper.kt`:

```kotlin
// Entity → Domain (chamado após leituras)
fun CartaoEntity.toDomain(): Cartao = Cartao(
    id          = id,
    nomeTitular = nomeTitular,
    // todos os campos
)

// Domain → Entity (chamado antes de escritas)
fun Cartao.toEntity(): CartaoEntity = CartaoEntity(
    id          = id,
    nomeTitular = nomeTitular,
    // todos os campos
)
```

**Regras:**
- sem lógica, sem defaults, sem formatação — mapeamento campo a campo apenas
- um arquivo de mapper por par de entidades
- chamado no limite do repository, nunca dentro do DAO ou do ViewModel

---

## 10. Estrutura de Módulo Hilt

Quando um `@Module` Hilt precisa de `@Binds` e `@Provides`:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {                      // deve ser abstract para @Binds

    @Binds @Singleton
    abstract fun bindCartaoRepository(        // @Binds — função abstract
        impl: CartaoRepositoryImpl
    ): CartaoRepository

    companion object {                          // @Provides dentro do companion object
        @Provides @Singleton
        fun provideDatabase(...): AppDatabase = ...

        @Provides @Singleton
        fun provideCartaoDao(db: AppDatabase): CartaoDao = db.cartaoDao()
    }
}
```

Quando um módulo tem apenas `@Provides` (sem `@Binds`), usa `object`:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun provideRetrofit(...): Retrofit = ...
}
```

---

## 11. Rotas de Navegação

As rotas ficam exclusivamente em `ui/navigation/Routes.kt`:

```kotlin
@Serializable object ListaRoute                          // sem parâmetros → object
@Serializable data class DetalheRoute(val id: Long)     // com parâmetros → data class
@Serializable data class CadastrarAlterarRoute(val id: Long = 0L)  // padrão = novo
```

**Convenções:**
- sufixo `Route` em toda declaração
- `@Serializable` é obrigatório (exigido pela API type-safe do Navigation Compose 2)
- `object` para destinos sem parâmetros; `data class` para destinos com parâmetros
- valor sentinela `id = 0L` significa "criar novo"; `id > 0` significa "editar existente"

---

## 12. Estilo de Parâmetros de Composable

Parâmetros de composables públicos são nomeados, tipados e alinhados:

```kotlin
// Observado em AppScaffold, AppTopAppBar, todas as funções XContent
@Composable
fun AppTopAppBar(
    title          : String,
    onNavigateBack : (() -> Unit)? = null,
    actions        : @Composable RowScope.() -> Unit = {},
)
```

**Regras:**
- parâmetros de slot usam lambdas `@Composable` com nomes descritivos (`topBar`, `floatingActionButton`, `content`, `actions`)
- callbacks são nomeados `on<Ação>` (ex.: `onNavigateBack`, `onNavigateToItem`, `onEvent`)
- parâmetros opcionais sempre têm valores padrão

---

## 13. Convenções de Preview

```kotlin
// 1. Sempre private
// 2. Variantes Light + Dark como anotações @Preview separadas na mesma função
// 3. Sempre encapsular em GerenciadorCartoesTheme
// 4. Sempre chamar XContent, nunca XScreen
// 5. Nomeado <Feature><Variante>Preview

@Preview(showBackground = true, name = "Lista – Com cartões")
@Preview(showBackground = true, name = "Lista – Com cartões Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ListaComItensPreview() {
    GerenciadorCartoesTheme {
        ListaContent(
            uiState = ListaUiState(cartoes = listOf(
                Cartao(1L, "João Silva", "1234", "Visa", "12/28", 5_000.0),
            ))
        )
    }
}
```

Cada arquivo fornece previews para pelo menos: estado de carregamento, estado vazio/erro e estado preenchido, onde aplicável.

---

## 14. Uso de Tokens de Design

Espaçamentos e tamanhos de ícone nunca são codificados com valores brutos de `dp` no código de feature. Os tokens do design system são usados em seu lugar:

```kotlin
// ✅ utilizado em todas as telas de feature e componentes
val spacing = LocalSpacing.current
Modifier.padding(spacing.medium)           // 16dp
Modifier.padding(horizontal = spacing.medium, vertical = spacing.small)

// Também disponível via extensão de MaterialTheme
MaterialTheme.spacing.large                // 24dp
MaterialTheme.iconSize.medium              // 24dp
```

**Escala completa de `Spacing`:** `extraSmall` (4dp) · `small` (8dp) · `smallMedium` (12dp) · `medium` (16dp) · `large` (24dp) · `extraLarge` (32dp)

**Escala completa de `IconSize`:** `extraSmall` (16dp) · `small` (20dp) · `medium` (24dp) · `large` (40dp) · `extraLarge` (48dp)

A exceção é `EmptyState.kt`, que usa `64.dp` fixo para o tamanho do ícone — este caso específico não é coberto pelos tokens de `IconSize` (cuja escala vai até `48dp`).

---

## 15. OptIn no Nível de Arquivo

`TopAppBar` do Material 3 exige `@ExperimentalMaterial3Api` com a versão atual do compilador Kotlin. O opt-in é aplicado no **nível do arquivo**, não anotação por anotação:

```kotlin
// AppTopAppBar.kt — primeira linha, antes da declaração package
@file:OptIn(ExperimentalMaterial3Api::class)
package com.app.gerenciadorcartoes.ui.components
```

Este é o único arquivo do projeto que requer esta anotação.

---

## 16. Padrão de Validação de Formulário

A validação inline no campo em `CadastrarAlterarViewModel.validar()` segue um padrão **coletivo** — não interrompe no primeiro erro, de modo que todos os erros de campo são exibidos simultaneamente:

```kotlin
private fun validar(): Boolean {
    val s = _uiState.value
    var valid = true

    if (s.nomeTitular.isBlank()) {
        _uiState.update { it.copy(erroNome = "Nome é obrigatório") }; valid = false
    }
    if (s.finalNumero.length != 4 || !s.finalNumero.all { it.isDigit() }) {
        _uiState.update { it.copy(erroNumero = "Informe os 4 últimos dígitos") }; valid = false
    }
    if (s.bandeira.isBlank()) {
        _uiState.update { it.copy(erroBandeira = "Bandeira é obrigatória") }; valid = false
    }
    if (!Regex("""^\d{2}/\d{2}$""").matches(s.validade)) {
        _uiState.update { it.copy(erroValidade = "Formato MM/AA") }; valid = false
    }
    if (s.limite.isBlank() || s.limite.toDoubleOrNull() == null) {
        _uiState.update { it.copy(erroLimite = "Limite inválido") }; valid = false
    }
    return valid
}
```

Os erros de campo são limpos individualmente quando o usuário modifica o campo correspondente:

```kotlin
is CadastrarAlterarEvent.NomeTitularAlterado ->
    _uiState.update { it.copy(nomeTitular = event.valor, erroNome = null) }
```

O evento `FinalNumeroAlterado` também aplica um limite rígido de 4 caracteres no nível do manipulador de evento (antes da validação):

```kotlin
is CadastrarAlterarEvent.FinalNumeroAlterado ->
    _uiState.update { it.copy(finalNumero = event.valor.take(4), erroNumero = null) }
```
