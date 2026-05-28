package com.app.gerenciadorcartoes.network.model

import kotlinx.serialization.Serializable

@Serializable
data class MastraChatRequest(
    val messages : String,
    val system   : String,
    val memory   : MastraChatMemoryRequest,
)

@Serializable
data class MastraGenerateResponse(
    val text : String? = null,
)

@Serializable
data class MastraChatMemoryRequest(
    val thread   : String,
    val resource : String,
)
