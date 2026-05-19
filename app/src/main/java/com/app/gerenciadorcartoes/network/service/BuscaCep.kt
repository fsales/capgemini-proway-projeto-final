package com.app.gerenciadorcartoes.network.service

import com.app.gerenciadorcartoes.network.model.ViaCepResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface BuscaCep {

    @GET("{cep}/json/")
    suspend fun getCep(@Path("cep") cep: String): Response<ViaCepResponse>

}