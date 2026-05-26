package com.app.gerenciadorcartoes.architecture

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.junit.ArchUnitRunner
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.runner.RunWith

/**
 * Regras específicas de isolamento de camadas.
 * Complementam o LayeredArchitectureTest com mensagens de erro mais descritivas.
 */
@RunWith(ArchUnitRunner::class)
@AnalyzeClasses(
    packages = [ROOT_PACKAGE],
    importOptions = [ImportOption.DoNotIncludeTests::class],
)
class LayerRulesTest {

    /**
     * Regra 2 — Classes @Entity só são importadas em data, repository e di.
     *
     * CartaoEntity e CadastroUsuarioEntity não devem vazar para ViewModel ou UI.
     */
    @ArchTest
    val entitySoAcessadaPorDataRepositoryEDi: ArchRule = noClasses()
        .that().resideOutsideOfPackages(PKG_DATA, PKG_REPOSITORY, PKG_DI)
        .should().accessClassesThat().resideInAPackage("..data.local.entity..")
        .`as`("Classes @Entity só devem ser acessadas em data, repository e di")

    /**
     * Regra 3 — Classes @Dao só são importadas em data, repository e di.
     *
     * CartaoDao e CadastroUsuarioDao não devem ser injetados diretamente em ViewModels.
     */
    @ArchTest
    val daoSoAcessadoPorDataRepositoryEDi: ArchRule = noClasses()
        .that().resideOutsideOfPackages(PKG_DATA, PKG_REPOSITORY, PKG_DI)
        .should().accessClassesThat().resideInAPackage("..data.local.dao..")
        .`as`("Classes @Dao só devem ser acessadas em data, repository e di")

    /**
     * Regra 4 — *RepositoryImpl não deve ser importado por viewmodel.
     *
     * ViewModels injetam apenas a interface (CartaoRepository), nunca a implementação
     * (CartaoRepositoryImpl).
     */
    @ArchTest
    val repositoryImplNaoAcessadoPorViewModel: ArchRule = noClasses()
        .that().resideInAPackage(PKG_VIEWMODEL)
        .should().accessClassesThat().haveSimpleNameEndingWith("RepositoryImpl")
        .`as`("ViewModels não devem depender de RepositoryImpl — use a interface")

    /**
     * Regra 5 — AppDatabase não deve ser importado por viewmodel ou ui.
     *
     * O banco de dados é um detalhe de infraestrutura; apenas di e repository
     * fazem o wiring com o banco.
     */
    @ArchTest
    val appDatabaseNaoAcessadoPorViewModelOuUi: ArchRule = noClasses()
        .that().resideInAnyPackage(PKG_VIEWMODEL, PKG_UI)
        .should().accessClassesThat().haveSimpleName("AppDatabase")
        .`as`("AppDatabase não deve ser acessado por ViewModel ou UI")
}
