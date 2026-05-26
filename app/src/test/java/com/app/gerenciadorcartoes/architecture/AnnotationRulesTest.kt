package com.app.gerenciadorcartoes.architecture

import androidx.lifecycle.ViewModel
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.junit.ArchUnitRunner
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import dagger.hilt.android.lifecycle.HiltViewModel
import com.tngtech.archunit.core.domain.JavaModifier
import androidx.room.Database as RoomDatabaseAnnotation
import androidx.room.Entity as RoomEntity
import androidx.room.Dao as RoomDao
import dagger.Module as DaggerModule
import kotlinx.serialization.Serializable as KotlinxSerializable
import org.junit.runner.RunWith

/**
 * Regras de anotações obrigatórias por convenção do projeto.
 */
@RunWith(ArchUnitRunner::class)
@AnalyzeClasses(
    packages = [ROOT_PACKAGE],
    importOptions = [ImportOption.DoNotIncludeTests::class],
)
class AnnotationRulesTest {

    /**
     * Regra 19 — Classes em viewmodel/ que estendem ViewModel devem ter @HiltViewModel.
     *
     * Garante que nenhum ViewModel seja criado sem injeção de dependência via Hilt.
     */
    @ArchTest
    val viewModelDeveSerHiltViewModel: ArchRule = classes()
        .that().resideInAPackage(PKG_VIEWMODEL)
        .and().areAssignableTo(ViewModel::class.java)
        // Excluir a classe base ViewModel
        .and().doNotHaveSimpleName("ViewModel")
        .should().beAnnotatedWith(HiltViewModel::class.java)
        .`as`("Todos os ViewModels devem ser anotados com @HiltViewModel")

    /**
     * Regra 20 — Classes com @HiltViewModel devem residir em viewmodel/.
     *
     * Impede que ViewModels sejam colocados em pacotes incorretos (ex: ui.feature.*).
     */
    @ArchTest
    val hiltViewModelNoPackageCorreto: ArchRule = classes()
        .that().areAnnotatedWith(HiltViewModel::class.java)
        .should().resideInAPackage(PKG_VIEWMODEL)
        .`as`("Classes @HiltViewModel devem residir em viewmodel/")

    /**
     * Regra 21 — Classes em data.local.entity/ devem ter @Entity.
     *
     * Entidades Room sempre precisam desta anotação para o compilador KSP processar.
     */
    @ArchTest
    val entityDeveSerAnotada: ArchRule = classes()
        .that().resideInAPackage("..data.local.entity..")
        .and().areNotAnonymousClasses()
        .and().areNotMemberClasses()
        .should().beAnnotatedWith(RoomEntity::class.java)
        .`as`("Classes em data/local/entity/ devem ser anotadas com @Entity")

    /**
     * Regra 22 — Interfaces em data.local.dao/ devem ter @Dao.
     */
    @ArchTest
    val daoDeveSerAnotado: ArchRule = classes()
        .that().resideInAPackage("..data.local.dao..")
        .and().areInterfaces()
        .should().beAnnotatedWith(RoomDao::class.java)
        .`as`("Interfaces em data/local/dao/ devem ser anotadas com @Dao")

    /**
     * Regra 23 — Classes em data.local.database/ devem ter @Database.
     */
    @ArchTest
    val databaseDeveSerAnotado: ArchRule = classes()
        .that().resideInAPackage("..data.local.database..")
        .and().areAssignableTo(androidx.room.RoomDatabase::class.java)
        .and().haveModifier(JavaModifier.ABSTRACT)
        .should().beAnnotatedWith(RoomDatabaseAnnotation::class.java)
        .`as`("Classes que estendem RoomDatabase em data/local/database/ devem ser anotadas com @Database")

    /**
     * Regra 24 — Classes *Module em di/ devem ter @Module.
     */
    @ArchTest
    val moduleDeveSerAnotado: ArchRule = classes()
        .that().resideInAPackage(PKG_DI)
        .and().haveSimpleNameEndingWith("Module")
        .should().beAnnotatedWith(DaggerModule::class.java)
        .`as`("Classes *Module em di/ devem ser anotadas com @Module")

    /**
     * Regra 25 — Classes *Route em ui.navigation/ devem ter @Serializable.
     *
     * Rotas type-safe do Navigation Compose 2 exigem @Serializable para
     * serialização correta dos parâmetros de navegação.
     */
    @ArchTest
    val routeDeveSerSerializable: ArchRule = classes()
        .that().resideInAPackage(PKG_UI_NAVIGATION)
        .and().haveSimpleNameEndingWith("Route")
        .should().beAnnotatedWith(KotlinxSerializable::class.java)
        .`as`("Classes *Route devem ser anotadas com @Serializable")
}
