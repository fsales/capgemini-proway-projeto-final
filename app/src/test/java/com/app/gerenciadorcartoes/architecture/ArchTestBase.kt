package com.app.gerenciadorcartoes.architecture

/** Pacote raiz analisado pelo ArchUnit em todos os testes de arquitetura. */
const val ROOT_PACKAGE = "com.app.gerenciadorcartoes"

// ── Pacotes por camada ────────────────────────────────────────────────────────
const val PKG_MODEL         = "com.app.gerenciadorcartoes.model.."
const val PKG_DATA          = "com.app.gerenciadorcartoes.data.."
const val PKG_REPOSITORY    = "com.app.gerenciadorcartoes.repository.."
const val PKG_DI            = "com.app.gerenciadorcartoes.di.."
const val PKG_NETWORK       = "com.app.gerenciadorcartoes.network.."
const val PKG_UI            = "com.app.gerenciadorcartoes.ui.."
const val PKG_VIEWMODEL     = "com.app.gerenciadorcartoes.viewmodel.."

// ── Pacotes de sub-camadas da UI ─────────────────────────────────────────────
const val PKG_UI_FEATURE    = "com.app.gerenciadorcartoes.ui.feature.."
const val PKG_UI_NAVIGATION = "com.app.gerenciadorcartoes.ui.navigation.."
const val PKG_UI_COMPONENTS = "com.app.gerenciadorcartoes.ui.components.."
const val PKG_UI_THEME      = "com.app.gerenciadorcartoes.ui.theme.."
