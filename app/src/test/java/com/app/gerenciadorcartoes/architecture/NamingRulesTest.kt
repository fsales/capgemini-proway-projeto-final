package com.app.gerenciadorcartoes.architecture

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.junit.ArchUnitRunner
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import org.junit.runner.RunWith

/**
 * Convenções de nomenclatura do projeto.
 *
 * Observação sobre mappers: funções top-level Kotlin (ex: CartaoMapper.kt)
 * compilam para CartaoMapperKt.class — as regras usam "MapperKt" como sufixo.
 */
@RunWith(ArchUnitRunner::class)
@AnalyzeClasses(
    packages = [ROOT_PACKAGE],
    importOptions = [ImportOption.DoNotIncludeTests::class],
)
class NamingRulesTest {

    /** Regra 6 — *ViewModel reside em viewmodel */
    @ArchTest
    val viewModelNoPackageCorreto: ArchRule = classes()
        .that().haveSimpleNameEndingWith("ViewModel")
        .and().doNotHaveSimpleName("ViewModel") // excluir a classe base do Jetpack
        .should().resideInAPackage(PKG_VIEWMODEL)
        .`as`("Classes *ViewModel devem residir em viewmodel/")

    /** Regra 7 — *Entity reside em data.local.entity */
    @ArchTest
    val entityNoPackageCorreto: ArchRule = classes()
        .that().haveSimpleNameEndingWith("Entity")
        .should().resideInAPackage("..data.local.entity..")
        .`as`("Classes *Entity devem residir em data/local/entity/")

    /** Regra 8 — *Dao reside em data.local.dao */
    @ArchTest
    val daoNoPackageCorreto: ArchRule = classes()
        .that().haveSimpleNameEndingWith("Dao")
        .should().resideInAPackage("..data.local.dao..")
        .`as`("Interfaces *Dao devem residir em data/local/dao/")

    /** Regra 9 — *Repository reside em repository */
    @ArchTest
    val repositoryNoPackageCorreto: ArchRule = classes()
        .that().haveSimpleNameEndingWith("Repository")
        .should().resideInAPackage(PKG_REPOSITORY)
        .`as`("Interfaces *Repository devem residir em repository/")

    /** Regra 10 — *RepositoryImpl reside em repository */
    @ArchTest
    val repositoryImplNoPackageCorreto: ArchRule = classes()
        .that().haveSimpleNameEndingWith("RepositoryImpl")
        .should().resideInAPackage(PKG_REPOSITORY)
        .`as`("Classes *RepositoryImpl devem residir em repository/")

    /** Regra 11 — *Database reside em data.local.database */
    @ArchTest
    val databaseNoPackageCorreto: ArchRule = classes()
        .that().haveSimpleNameEndingWith("Database")
        .should().resideInAPackage("..data.local.database..")
        .`as`("Classes *Database devem residir em data/local/database/")

    /** Regra 12 — *UiState reside em ui.feature.*.state */
    @ArchTest
    val uiStateNoPackageCorreto: ArchRule = classes()
        .that().haveSimpleNameEndingWith("UiState")
        .should().resideInAPackage("..ui.feature..state..")
        .`as`("Classes *UiState devem residir em ui/feature/<nome>/state/")

    /** Regra 13 — *Route reside em ui.navigation */
    @ArchTest
    val routeNoPackageCorreto: ArchRule = classes()
        .that().haveSimpleNameEndingWith("Route")
        .should().resideInAPackage(PKG_UI_NAVIGATION)
        .`as`("Classes *Route devem residir em ui/navigation/")

    /**
     * Regra 14 — *MapperKt reside em repository.mapper.
     *
     * Funções top-level Kotlin em arquivos *Mapper.kt compilam para *MapperKt.class.
     */
    @ArchTest
    val mapperNoPackageCorreto: ArchRule = classes()
        .that().haveSimpleNameEndingWith("MapperKt")
        .should().resideInAPackage("..repository.mapper..")
        .`as`("Classes *MapperKt (funções top-level de mapper) devem residir em repository/mapper/")

    /** Regra 15 — *Module reside em di */
    @ArchTest
    val moduleNoPackageCorreto: ArchRule = classes()
        .that().haveSimpleNameEndingWith("Module")
        .and().areNotMemberClasses()
        .and().resideInAPackage(ROOT_PACKAGE + "..")
        .should().resideInAPackage(PKG_DI)
        .`as`("Classes *Module devem residir em di/")

    /**
     * Regra 16 — *Event reside em ui.feature.
     *
     * Exclui inner classes de sealed interfaces (ex: ListaEvent$NavegaParaNovo)
     * verificando se o nome simples termina em "Event" sem "$".
     */
    @ArchTest
    val eventNoPackageCorreto: ArchRule = classes()
        .that().haveSimpleNameEndingWith("Event")
        .and().areNotAnonymousClasses()
        .and().areNotMemberClasses()
        .should().resideInAPackage(PKG_UI_FEATURE)
        .`as`("Classes *Event (eventos de usuário) devem residir em ui/feature/")

    /**
     * Regra 17 — *UiEvent reside em ui.feature.
     *
     * Exclui inner/anonymous classes.
     */
    @ArchTest
    val uiEventNoPackageCorreto: ArchRule = classes()
        .that().haveSimpleNameEndingWith("UiEvent")
        .and().areNotAnonymousClasses()
        .and().areNotMemberClasses()
        .should().resideInAPackage(PKG_UI_FEATURE)
        .`as`("Classes *UiEvent (efeitos pontuais VM→UI) devem residir em ui/feature/")

    /** Regra 18 — Classes em ui.navigation terminam com Route ou têm nome "AppNavHost" */
    @ArchTest
    val classesEmNavigationSaoRotas: ArchRule = classes()
        .that().resideInAPackage(PKG_UI_NAVIGATION)
        .and().areNotAnonymousClasses()
        .and().areNotMemberClasses()
        .should().haveSimpleNameEndingWith("Route")
        .orShould().haveSimpleName("AppNavHost")
        .orShould().haveSimpleName("AppNavHostKt")
        .`as`("Classes em ui/navigation/ devem ter nome terminando em Route ou ser AppNavHost")
}
