package com.app.gerenciadorcartoes.ui.feature.splash

sealed interface SplashUiEvent {
    data object NavigateToLista : SplashUiEvent
    data object NavigateToLogin : SplashUiEvent
}

