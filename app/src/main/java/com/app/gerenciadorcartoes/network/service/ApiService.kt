package com.app.gerenciadorcartoes.network.service

import com.app.gerenciadorcartoes.network.model.AddCardRequest
import com.app.gerenciadorcartoes.network.model.BlockCardRequest
import com.app.gerenciadorcartoes.network.model.CartaoResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface ApiService {
    @GET("cards") suspend fun getCards(): Response<CartaoResponse>

    @POST("cards") suspend fun addCard(@Body request: AddCardRequest)

    @POST("cards/block") suspend fun blockCard(@Body request: BlockCardRequest)

    @POST("cards/unblock") suspend fun unblockCard(@Body request: BlockCardRequest)
}
