package com.app.gerenciadorcartoes.ui.navigation

import kotlinx.serialization.Serializable

// Destino: tela de login (tela inicial do app)
@Serializable
object LoginRoute

// Destino: lista de cartões
@Serializable
object ListaRoute

// Destino: detalhes de um cartão específico
@Serializable
data class DetalheRoute(val id: Long)

// Destino: formulário de cadastro (id=0) ou edição (id>0)
@Serializable
data class CadastrarAlterarRoute(val id: Long = 0L)

// Destino: cadastro de novo usuário
@Serializable
object CadastroUsuarioRoute

