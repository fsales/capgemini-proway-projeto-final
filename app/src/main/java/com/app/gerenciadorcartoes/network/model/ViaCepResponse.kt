package com.app.gerenciadorcartoes.network.model

import com.google.gson.annotations.SerializedName

data class ViaCepResponse(
    @SerializedName("logradouro") val logradouro : String  = "",
    @SerializedName("bairro")     val bairro     : String  = "",
    @SerializedName("uf")         val uf         : String  = "",
)

