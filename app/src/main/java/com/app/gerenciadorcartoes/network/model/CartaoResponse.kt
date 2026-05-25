package com.app.gerenciadorcartoes.network.model

import com.google.gson.annotations.SerializedName

data class CartaoResponse(
    @SerializedName("id") val id: String = "",
    @SerializedName("nomeTitular") val nomeTitular: String = "",
    @SerializedName("finalNumero") val finalNumero: String = "",
    @SerializedName("bandeira") val bandeira: String = "",
    @SerializedName("validade") val validade: String = "",
    @SerializedName("limite") val limite: String = "",
    @SerializedName("template") val template: String = "",
    @SerializedName("bloqueado") val bloqueado: String = "",
)

