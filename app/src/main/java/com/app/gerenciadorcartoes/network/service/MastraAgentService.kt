package com.app.gerenciadorcartoes.network.service

import com.app.gerenciadorcartoes.network.model.MastraChatRequest
import com.app.gerenciadorcartoes.network.model.MastraGenerateResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface MastraAgentService {

    @POST("agents/card-spending-agent/generate")
    suspend fun generateCardSpendingAgent(
        @Body request: MastraChatRequest,
    ): Response<MastraGenerateResponse>
}
