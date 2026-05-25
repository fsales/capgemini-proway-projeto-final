package com.app.gerenciadorcartoes.network.service

import com.app.gerenciadorcartoes.data.local.entity.CartaoEntity
import com.app.gerenciadorcartoes.network.model.CartaoResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface ApiService {
    @GET("cards") suspend fun getCards(): Response<CartaoResponse>

    @POST("cards") suspend fun addCard(cartao: CartaoEntity)

    @POST("cards/{id}/block") suspend fun blockCard(@Path("id") id:Int)

    @POST("cards/{id}/unblock") suspend fun unblockCard(@Path("id") id:Int)
}
