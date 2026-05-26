package com.app.gerenciadorcartoes.ui.feature.splash

sealed interface SplashUiEvent {
    data object NavigateToLista : SplashUiEvent
    data object NavigateToLogin : SplashUiEvent
    data class  NavigateToCadastroIncompleto(
        val userId : String,
        val email  : String,
        val nome   : String,
    ) : SplashUiEvent
}
