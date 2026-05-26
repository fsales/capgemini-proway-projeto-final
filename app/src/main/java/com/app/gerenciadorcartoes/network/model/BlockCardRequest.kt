package com.app.gerenciadorcartoes.network.model

import kotlinx.serialization.Serializable

/**
 * Corpo da requisição POST /cards.
 *
 * [idUsuario] identifica o usuário dono do cartão no servidor remoto.
 * Os demais campos espelham [com.app.gerenciadorcartoes.model.Cartao].
 */
@Serializable
data class BlockCardRequest(
    val idUsuario   : String,
    val id    : String,
)

