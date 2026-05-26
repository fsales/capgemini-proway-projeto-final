package com.app.gerenciadorcartoes.architecture

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.junit.ArchUnitRunner
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.library.Architectures.layeredArchitecture
import org.junit.runner.RunWith

/**
 * Regra abrangente de arquitetura em camadas: verifica a hierarquia de dependências
 * do MVVM adotado no projeto.
 *
 * Hierarquia permitida (→ significa "pode depender de"):
 *   UI → ViewModel → Repository → Data
 *                  ↘ Model ↙
 *              Network → Repository
 *   DI → todas as camadas (wiring)
 *
 * Exceção documentada: SessionManagerImpl (Data) implementa SessionManager (Repository).
 * Isso é uma dependência Data → Repository legítima, ignorada explicitamente.
 */
@RunWith(ArchUnitRunner::class)
@AnalyzeClasses(
    packages = [ROOT_PACKAGE],
    importOptions = [ImportOption.DoNotIncludeTests::class],
)
class LayeredArchitectureTest {

    /**
     * Regra 1 — Camadas da arquitetura MVVM devem respeitar a hierarquia de dependências.
     *
     * Usa consideringOnlyDependenciesInLayers() para que dependências a libs externas
     * (Android SDK, Hilt, Compose etc.) não disparem falsos positivos.
     */
    @ArchTest
    val regraArquiteturaCamadas: ArchRule = layeredArchitecture()
        .consideringOnlyDependenciesInLayers()
        .layer("Model").definedBy("com.app.gerenciadorcartoes.model..")
        .layer("Data").definedBy("com.app.gerenciadorcartoes.data..")
        .layer("Repository").definedBy("com.app.gerenciadorcartoes.repository..")
        .layer("Network").definedBy("com.app.gerenciadorcartoes.network..")
        .layer("DI").definedBy("com.app.gerenciadorcartoes.di..")
        .layer("ViewModel").definedBy("com.app.gerenciadorcartoes.viewmodel..")
        .layer("UI").definedBy("com.app.gerenciadorcartoes.ui..")
        // Apenas ViewModel importa classes de UI (UiState e Events residem em ui.feature.*)
        .whereLayer("UI").mayOnlyBeAccessedByLayers("ViewModel")
        // Apenas UI pode usar ViewModel (via hiltViewModel())
        .whereLayer("ViewModel").mayOnlyBeAccessedByLayers("UI")
        // Repository é acessado por ViewModel e DI; Data implementa interfaces de Repository
        .whereLayer("Repository").mayOnlyBeAccessedByLayers("ViewModel", "DI", "Data")
        // Network apenas por Repository e DI
        .whereLayer("Network").mayOnlyBeAccessedByLayers("Repository", "DI")
        // Data apenas por Repository e DI
        .whereLayer("Data").mayOnlyBeAccessedByLayers("Repository", "DI")
        // Model é a camada mais interna — pode ser usada por todas
        .whereLayer("Model").mayOnlyBeAccessedByLayers(
            "Repository", "ViewModel", "Data", "DI", "UI", "Network",
        )
        // Exceção: SessionManagerImpl (Data) implementa SessionManager (Repository)
        .ignoreDependency(
            "com.app.gerenciadorcartoes.data.local.session.SessionManagerImpl",
            "com.app.gerenciadorcartoes.repository.session.SessionManager",
        )
        .`as`("Camadas da arquitetura MVVM devem respeitar a hierarquia de dependências")
}
