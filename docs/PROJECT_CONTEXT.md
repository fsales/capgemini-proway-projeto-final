# PROJECT_CONTEXT.md

> Gerado a partir de análise do código-fonte — `2026-05-15`.  
> Todas as informações são derivadas de arquivos reais do projeto. Nenhuma suposição foi feita.

---

## 1. Identificação

| Campo | Valor |
|---|---|
| Nome do projeto | `GerenciadorCartoes` |
| Pacote raiz | `com.app.gerenciadorcartoes` |
| Application ID | `com.app.gerenciadorcartoes` |
| Classe de aplicação | `GerenciadorCartoesApp` |
| Activity de entrada | `MainActivity` |
| Estrutura de módulos | Módulo único — `:app` |
| Min SDK | 28 (Android 9 — Pie) |
| Target / Compile SDK | 36 (minor API level 1) |
| Versão | 1.0 (versionCode 1) |
| `enableEdgeToEdge` | `true` — configurado em `MainActivity.onCreate` |

---

## 2. Objetivo

Aplicativo Android que demonstra um **CRUD completo** (Create, Read, Update, Delete) de cartões de crédito/débito (`Cartao`) armazenados localmente com Room. O projeto serve como implementação de referência de MVVM estrito com:

- Hilt para injeção de dependência
- Room (reativo via `Flow`) como única fonte de dados ativa
- Sessão persistente de login com DataStore Preferences + EncryptedSharedPreferences
- Navigation Compose 2 com rotas type-safe
- Retrofit + OkHttp provisionados para integração futura com API (ainda não ativo)

---

## 3. Modelo de Domínio

Entidade de domínio única `Cartao` — definida em `model/Cartao.kt` com **zero importações de framework**:

```kotlin
data class Cartao(
    val id          : Long   = 0L,
    val nomeTitular : String = "",   // nome completo do titular
    val finalNumero : String = "",   // apenas os 4 últimos dígitos — ex.: "1234"
    val bandeira    : String = "",   // ex.: "Visa", "Mastercard", "Elo"
    val validade    : String = "",   // formato "MM/AA" — ex.: "12/28"
    val limite      : Double = 0.0,  // limite de crédito em BRL
)
```

Todos os campos têm valores padrão para que `Cartao()` possa ser usado como estado inicial nas classes `UiState` sem verificações de nulo.

---

## 4. Funcionalidades (Telas)

| Tela | Objeto de rota | Finalidade |
|---|---|---|
| Lista | `ListaRoute` (destino inicial) | Lista reativa de todos os cartões; exclusão a partir da lista |
| Detalhe | `DetalheRoute(id: Long)` | Visualização somente leitura; navegar para ajuste de limite e faturas |
| Fatura | `FaturaRoute(id: Long)` | Lista mock de lançamentos agrupados por fatura mensal |
| Cadastrar / Alterar | `CadastrarAlterarRoute(id: Long)` | Formulário unificado — inserção (`id=0`) ou atualização (`id>0`) |
| Cadastro de Usuário | `CadastroUsuarioRoute` | Formulário responsivo com abas, CEP automático e validação inline |

### Fluxo de navegação (fonte: `AppNavHost.kt`)

```
ListaRoute  ←── destino inicial
  ├── FAB ──────────────────────────► CadastrarAlterarRoute(id=0)   [novo cartão]
  └── toque no cartão ──────────────► DetalheRoute(id)
                                           ├── Botão Ajustar limite ──► AjustarLimiteRoute(id)
                                           ├── Botão Faturas ─────────► FaturaRoute(id)
                                           └── Voltar ────────────────► popBackStack() → ListaRoute
CadastrarAlterarRoute
  └── Salvar / Voltar ──────────────► popBackStack()
FaturaRoute
  └── Voltar ───────────────────────► popBackStack() → DetalheRoute
```

---

## 5. Persistência de Dados

| Item | Valor |
|---|---|
| Nome do banco | `gerenciador-cartoes-db` |
| Classe do banco | `AppDatabase` (Room, versão 1, `exportSchema = false`) |
| Tabela | `cartoes` — suportada por `CartaoEntity` |
| DAO | `CartaoDao` — leituras reativas com `Flow` + escritas `suspend` |

**Operações do `CartaoDao`:**

| Método | Assinatura | Consumidor |
|---|---|---|
| `observarTodos()` | `Flow<List<CartaoEntity>>` | `ListaViewModel` — lista em tempo real; ordenada por `nomeTitular ASC` |
| `observarPorId(id)` | `Flow<CartaoEntity?>` | `DetalheViewModel` — auto-atualização |
| `buscarPorId(id)` | `suspend CartaoEntity?` | `CadastrarAlterarViewModel` — pré-preenchimento do formulário |
| `inserir(entity)` | `suspend Long` | retorna a chave primária gerada |
| `atualizar(entity)` | `suspend Unit` | atualização completa do registro via `@Update` |
| `excluirPorId(id)` | `suspend Unit` | exclusão pela chave primária via `@Query` |

O diretório `data/local/converter/` existe mas está atualmente vazio — nenhum `@TypeConverters` é necessário para o schema atual.

### Sessão persistente (produção)

| Item | Valor |
|---|---|
| Contrato | `data/local/session/SessionManager.kt` |
| Implementação | `SessionManagerImpl` |
| Store principal | `DataStore<Preferences>` (`session.preferences_pb`) |
| Fallback criptografado | `EncryptedSharedPreferences` (`session_secure_prefs`) |
| Chaves | `session_is_logged_in`, `session_usuario_logado`, `session_last_login_time` |

Fluxo aplicado:
- Login com sucesso salva sessão (`saveSession(usuario)`) antes de navegar.
- Splash verifica `isLoggedIn()` e direciona para `ListaRoute` ou `LoginRoute`.
- Logout limpa sessão (`logout()`) antes de voltar para login.

---

## 6. Camada de Rede (Provisionada, Não Ativa)

`NetworkModule` provisiona completamente um stack Retrofit, mas nenhum endpoint ativo está conectado:

| Componente | Configuração |
|---|---|
| `Json` | `ignoreUnknownKeys = true`, `coerceInputValues = true` |
| `OkHttpClient` | `HttpLoggingInterceptor(BODY)`, timeouts de 30s para connect / read / write |
| `Retrofit` | URL base `https://api.gerenciadorcartoes.com/`, conversor `kotlinx-serialization` |
| `ApiService` | interface placeholder — um comentário `TODO`, sem endpoints declarados |

Todo o CRUD é executado exclusivamente via Room.

---

## 7. Injeção de Dependência

O Hilt é ancorado por `@HiltAndroidApp` em `GerenciadorCartoesApp`. Dois módulos ficam em `di/`:

| Módulo | Tipo Kotlin | Fornece |
|---|---|---|
| `AppModule` | `abstract class` | `AppDatabase`, `CartaoDao`, `DataStore<Preferences>`, `EncryptedSharedPreferences`, `@Binds CartaoRepository → CartaoRepositoryImpl`, `@Binds SessionManager → SessionManagerImpl` |
| `NetworkModule` | `object` | `Json`, `OkHttpClient`, `Retrofit`, `ApiService` |

`AppModule` é abstract pois combina `@Binds` (abstract) e `@Provides` (concreto, dentro do `companion object`).

---

## 8. Build e Ferramentas

| Ferramenta | Versão | Papel |
|---|---|---|
| Kotlin | 2.3.21 | Linguagem; compilador K2 |
| AGP | 9.0.0 | Android Gradle Plugin |
| KSP | 2.3.6 | Geração de código para Hilt e Room (substitui KAPT) |
| Compose BOM | 2026.05.00 | Fixa versões de todos os artefatos Compose |
| Hilt | 2.59.2 | Injeção de dependência |
| Room | 2.8.4 | Banco de dados SQLite local |
| DataStore Preferences | 1.0.0 | Persistência de sessão reativa |
| AndroidX Security Crypto | 1.1.0 | Criptografia de fallback para dados de sessão |
| Navigation Compose | 2.9.0 | Navegação Composable type-safe |
| Hilt Navigation Compose | 1.2.0 | `hiltViewModel()` dentro de `composable<T> {}` |
| Retrofit | 3.0.0 | Cliente HTTP (provisionado, não ativo) |
| Retrofit Gson Converter | 3.0.0 | Conversor Gson para integrações que usam JSON via Gson |
| OkHttp | 5.3.2 | Engine HTTP + `LoggingInterceptor` |
| kotlinx-serialization | 1.11.0 | Serialização JSON + rotas do Navigation Compose 2 |
| Serialization Converter | 3.0.0 | `com.squareup.retrofit2:converter-kotlinx-serialization` |

Build features habilitadas em `app/build.gradle.kts`:
- `compose = true`
- `buildConfig = true`
- Compatibilidade de source/target Java 17

---

## 9. Design System

Ponto de entrada do tema: `GerenciadorCartoesTheme` em `ui/theme/Theme.kt`.

- Suporte a **cor dinâmica** no Android 12+ (`Build.VERSION_CODES.S`)
- Usa fallback para `LightColorScheme` / `DarkColorScheme` estáticos em versões de API mais antigas
- `CompositionLocalProvider` expõe dois sistemas de tokens adicionais junto ao Material 3:

| Sistema de tokens | `CompositionLocal` | Propriedade de extensão | Escala (token: valor) |
|---|---|---|---|
| Spacing | `LocalSpacing` | `MaterialTheme.spacing` | `extraSmall` 4dp · `small` 8dp · `smallMedium` 12dp · `medium` 16dp · `large` 24dp · `extraLarge` 32dp |
| Icon size | `LocalIconSize` | `MaterialTheme.iconSize` | `extraSmall` 16dp · `small` 20dp · `medium` 24dp · `large` 40dp · `extraLarge` 48dp |

---

## 10. Árvore de Fontes do Projeto (módulo `:app`)

```
app/src/main/java/com/app/gerenciadorcartoes/
│
├── GerenciadorCartoesApp.kt          @HiltAndroidApp — ponto de entrada do DI
├── MainActivity.kt                   @AndroidEntryPoint — host da activity única
│
├── model/
│   └── Cartao.kt                     Modelo de domínio (zero importações de framework)
│
├── data/local/
│   ├── converter/                    (vazio — reservado para futuros @TypeConverters)
│   ├── dao/CartaoDao.kt              @Dao — leituras com Flow + escritas suspend
│   ├── database/AppDatabase.kt       @Database v1 — declara CartaoEntity, expõe cartaoDao()
│   ├── entity/CartaoEntity.kt        @Entity("cartoes") — espelha os campos de Cartao 1-a-1
│   └── session/
│       ├── SessionManager.kt         contrato de sessão
│       └── SessionManagerImpl.kt     DataStore + EncryptedSharedPreferences
│
├── repository/
│   ├── CartaoRepository.kt           Interface — contrato de domínio (apenas tipos Cartao)
│   ├── CartaoRepositoryImpl.kt       Implementação — delega para CartaoDao + aplica mapper
│   └── mapper/CartaoMapper.kt        funções de extensão toDomain() / toEntity()
│
├── network/
│   └── service/ApiService.kt         Interface Retrofit (placeholder — sem endpoints ativos)
│
├── di/
│   ├── AppModule.kt                  @Binds + @Provides para Room e repository
│   └── NetworkModule.kt              @Provides para OkHttp, Retrofit, ApiService
│
└── ui/
    ├── theme/                        Color, Theme, Type, Shape, Spacing, IconSize
    ├── components/                   AppScaffold, AppTopAppBar, AppLoading, EmptyState
    ├── navigation/                   Routes.kt, AppNavHost.kt
    ├── feature/
    │   ├── lista/                    ListaEvent, ListaUiEvent, ListaScreen, state/ListaUiState
    │   ├── detalhe/                  DetalheEvent, DetalheUiEvent, DetalheScreen, state/DetalheUiState
    │   ├── fatura/                   FaturaEvent, FaturaUiEvent, FaturaScreen, state/FaturaUiState
    │   └── cadastraralterar/         CadastrarAlterarEvent, CadastrarAlterarUiEvent,
    │                                 CadastrarAlterarScreen, state/CadastrarAlterarUiState
    └── viewmodel/
        ├── ListaViewModel.kt
        ├── DetalheViewModel.kt   
        ├── FaturaViewModel.kt
        └── CadastrarAlterarViewModel.kt
```
