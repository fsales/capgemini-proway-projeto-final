package com.app.gerenciadorcartoes.architecture

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.junit.ArchUnitRunner
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass
import org.junit.runner.RunWith

/**
 * Regras que garantem a conformidade com o padrão MVVM + UDF (Unidirecional Data Flow)
 * adotado no projeto.
 */
@RunWith(ArchUnitRunner::class)
@AnalyzeClasses(
    packages = [ROOT_PACKAGE],
    importOptions = [ImportOption.DoNotIncludeTests::class],
)
class MvvmRulesTest {

    // Predicate para excluir classes geradas / factories / continuations
    private val naoGeradaNemFactory = object : DescribedPredicate<JavaClass>("não gerada nem factory") {
        override fun test(input: JavaClass): Boolean {
            val fqn = input.name ?: return true
            val simple = input.simpleName ?: fqn

            // Excluir classes sintéticas/inner geradas pelo compilador
            if (fqn.contains("$")) return false

            // Padrões Dagger/Hilt/Factory/Provider gerados
            if (simple.endsWith("_Factory") || simple.endsWith("ProvideFactory") || simple.endsWith("Factory")) return false
            if (fqn.contains("HiltModules") || fqn.contains("_HiltModules") || fqn.contains("Hilt_")) return false
            if (fqn.contains("_Provide") || fqn.contains("_Inject") || simple.contains("InjectAdapter")) return false
            if (simple.startsWith("Dagger") || fqn.contains("dagger.internal")) return false

            // Kotlin / Coroutines generated artifacts (continuations, inlined, etc.)
            if (fqn.startsWith("kotlin.coroutines.jvm.internal") || fqn.contains("kotlinx.coroutines")) return false
            if (fqn.contains("\$\$inlined") || fqn.contains("__inlined")) return false

            // Excluir classes relacionadas a Hilt/Dagger/Gradle plugin naming
            if (fqn.contains("Hilt") || fqn.contains("Dagger") || fqn.contains("_Hilt_")) return false

            return true
        }
    }

    private val viewModelNaoGerada = object : DescribedPredicate<JavaClass>("viewmodel não gerada") {
        override fun test(input: JavaClass): Boolean {
            val fqn = input.name ?: return false
            return fqn.startsWith(PKG_VIEWMODEL) && naoGeradaNemFactory.test(input)
        }
    }

    /**
     * Regra 26 — ViewModels não devem expor MutableStateFlow publicamente.
     *
     * O padrão UDF exige que o estado mutável seja privado (_uiState) e exposto
     * somente como StateFlow imutável (uiState).
     */
    @ArchTest
    val mutableStateFlowDeveSerPrivado: ArchRule = fields()
        .that().areDeclaredInClassesThat().haveSimpleNameEndingWith("ViewModel")
        .and().haveRawType(MutableStateFlow::class.java)
        .should().bePrivate()
        .`as`("MutableStateFlow em ViewModels deve ser privado (expor somente StateFlow)")

    /**
     * Regra 27 — ViewModels não devem expor Channel publicamente.
     *
     * O Channel de eventos (_uiEvent) deve ser privado; a UI consume via
     * receiveAsFlow() exposto como Flow imutável.
     */
    @ArchTest
    val channelDeveSerPrivado: ArchRule = fields()
        .that().areDeclaredInClassesThat().haveSimpleNameEndingWith("ViewModel")
        .and().haveRawType(Channel::class.java)
        .should().bePrivate()
        .`as`("Channel em ViewModels deve ser privado (expor somente Flow via receiveAsFlow())")

    /**
     * Regra 28 — ViewModels não devem importar android.content.Context.
     *
     * Context torna o ViewModel acoplado ao Android e dificulta testes unitários.
     * Use @ApplicationContext somente em classes da camada data/ se indispensável.
     */
    @ArchTest
    val viewModelNaoUsaContext: ArchRule = noClasses()
        .that().resideInAPackage(PKG_VIEWMODEL)
        .should().accessClassesThat().haveFullyQualifiedName("android.content.Context")
        .`as`("ViewModels não devem depender de android.content.Context")

    /**
     * Regra 29 — ViewModels não devem importar Activity.
     */
    @ArchTest
    val viewModelNaoUsaActivity: ArchRule = noClasses()
        .that().resideInAPackage(PKG_VIEWMODEL)
        .should().accessClassesThat().haveFullyQualifiedName("android.app.Activity")
        .`as`("ViewModels não devem depender de android.app.Activity")

    /**
     * Regra 30 — ViewModels não devem acessar a classe R do próprio pacote.
     *
     * Recursos Android (strings, drawables) devem ser resolvidos na camada UI,
     * não no ViewModel.
     */
    @ArchTest
    val viewModelNaoUsaClasseR: ArchRule = noClasses()
        .that().resideInAPackage(PKG_VIEWMODEL)
        .should().accessClassesThat()
        .haveFullyQualifiedName("com.app.gerenciadorcartoes.R")
        .`as`("ViewModels não devem acessar a classe R — resolva recursos na camada UI")

    /**
     * Regra 31 — ViewModels não devem depender de outros ViewModels.
     *
     * Cada ViewModel é independente; comunicação entre features é feita por
     * navegação (UiEvent) ou pelo Repository compartilhado.
     */
    @ArchTest
    val viewModelNaoDependeDeOutroViewModel: ArchRule = noClasses()
        .that().resideInAPackage(PKG_VIEWMODEL)
        .and().areNotMemberClasses()
        .and().areNotAnonymousClasses()
        .should().accessClassesThat(viewModelNaoGerada)
        .`as`("ViewModels não devem depender de outros ViewModels — use Repository ou UiEvent")

    /**
     * Regra 32 — Classes em di/ não devem importar da camada UI.
     *
     * Módulos Hilt fazem wiring de Repository, Data e Network — nunca de composables.
     */
    @ArchTest
    val diNaoAcessaUi: ArchRule = noClasses()
        .that().resideInAPackage(PKG_DI)
        .should().accessClassesThat().resideInAPackage(PKG_UI)
        .`as`("Classes em di/ não devem depender da camada UI")
}
