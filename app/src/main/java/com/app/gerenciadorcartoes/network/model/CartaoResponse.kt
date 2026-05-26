package com.app.gerenciadorcartoes.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
@Serializable
data class CartaoResponse(
    @SerialName("id") val id: String = "",
    @SerialName("nomeTitular") val nomeTitular: String = "",
    @SerialName("finalNumero") val finalNumero: String = "",
    @SerialName("bandeira") val bandeira: String = "",
    @SerialName("validade") val validade: String = "",
    @SerialName("limite") val limite: String = "",
    @SerialName("template") val template: String = "",
    @SerialName("bloqueado") val bloqueado: String = "",
)

