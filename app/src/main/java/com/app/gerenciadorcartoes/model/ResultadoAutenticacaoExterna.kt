package com.app.gerenciadorcartoes.model

sealed interface ResultadoAutenticacaoExterna {
    data object Autenticado : ResultadoAutenticacaoExterna

    data class PrecisaCadastro(
        val userId : String,
        val email  : String,
        val nome   : String,
    ) : ResultadoAutenticacaoExterna
}
