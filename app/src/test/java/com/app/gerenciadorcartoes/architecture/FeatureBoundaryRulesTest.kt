package com.app.gerenciadorcartoes.architecture

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.junit.ArchUnitRunner
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition
import org.junit.runner.RunWith

/**
 * Regras de isolamento de boundaries de features.
 *
 * Features devem ser independentes entre si. Toda comunicação entre features
 * é mediada por: (1) rotas de navegação em ui/navigation/, (2) eventos UiEvent
 * do ViewModel, ou (3) o Repository compartilhado.
 */
@RunWith(ArchUnitRunner::class)
@AnalyzeClasses(
    packages = [ROOT_PACKAGE],
    importOptions = [ImportOption.DoNotIncludeTests::class],
)
class FeatureBoundaryRulesTest {

    /**
     * Regra 33 — Features não devem depender umas das outras.
     *
     * Cada slice é definido pelo segmento de pacote logo após "ui.feature."
     * (ex: lista, detalhe, cadastraralterar, ajustarlimite, login, splash, cadastrousuario).
     *
     * Um feature slice não deve importar classes de outro feature slice.
     */
    @ArchTest
    val featuresSaoIndependentes: ArchRule = SlicesRuleDefinition.slices()
        .matching("com.app.gerenciadorcartoes.ui.feature.(*)..")
        .should().notDependOnEachOther()
        .`as`("Features de UI não devem depender umas das outras")

    /**
     * Regra 34 — Componentes compartilhados não devem depender de nenhuma feature.
     *
     * AppScaffold, AppTopAppBar, AppLoading e demais composables em ui/components/
     * são reutilizáveis e agnósticos de feature. Importar de ui.feature.* tornaria
     * um componente compartilhado acoplado a uma feature específica.
     */
    @ArchTest
    val componentesNaoDepemDeFeatures: ArchRule = noClasses()
        .that().resideInAPackage(PKG_UI_COMPONENTS)
        .should().accessClassesThat().resideInAPackage(PKG_UI_FEATURE)
        .`as`("Componentes compartilhados (ui/components/) não devem depender de features")
}
