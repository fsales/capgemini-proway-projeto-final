package com.app.gerenciadorcartoes.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ViaCepResponse(
    @SerialName("logradouro") val logradouro : String = "",
    @SerialName("bairro")     val bairro     : String = "",
    @SerialName("localidade") val localidade : String = "",
    @SerialName("uf")         val uf         : String = "",
)

