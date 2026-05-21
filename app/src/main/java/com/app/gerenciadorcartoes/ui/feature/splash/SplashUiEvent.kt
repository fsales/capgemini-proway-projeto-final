package com.app.gerenciadorcartoes.ui.feature.splash

sealed interface SplashUiEvent {
    data object NavigateToLogin : SplashUiEvent
}

