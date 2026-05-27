package com.app.gerenciadorcartoes.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object SplashRoute

@Serializable
object LoginRoute

@Serializable
data class ListaRoute(val exibirConfirmacao: Boolean = false)

@Serializable
data class DetalheRoute(val id: Long)

// Destino: faturas de um cartão específico
@Serializable
data class FaturaRoute(val id: Long)

// Destino: ajuste do limite de um cartão específico
@Serializable
data class AjustarLimiteRoute(val id: Long)

@Serializable
data class CadastrarAlterarRoute(val id: Long = 0L)

@Serializable
data class CadastroUsuarioRoute(
    val userId      : String  = "",
    val emailExterno: String  = "",
    val nomeExterno : String  = "",
    val modoEdicao  : Boolean = false,
)

@Serializable
data class RecuperarSenhaRoute(val emailInicial: String = "")
