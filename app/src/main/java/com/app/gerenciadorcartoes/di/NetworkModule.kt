package com.app.gerenciadorcartoes.di

import com.app.gerenciadorcartoes.BuildConfig
import com.app.gerenciadorcartoes.network.mastra.MastraBaseUrlProvider
import com.app.gerenciadorcartoes.network.service.ApiService
import com.app.gerenciadorcartoes.network.service.BuscaCep
import com.app.gerenciadorcartoes.network.service.MastraAgentService
import com.app.gerenciadorcartoes.network.ssl.UnsafeSslConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues  = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                            else                   HttpLoggingInterceptor.Level.NONE
                },
            )
            // ⚠️ UnsafeSslConfig: aceita qualquer certificado SSL.
            // Necessário porque o servidor de treinamento usa certificado não confiável pelo Android.
            // NÃO use em produção.
            .sslSocketFactory(UnsafeSslConfig.sslSocketFactory, UnsafeSslConfig.trustManager)
            .hostnameVerifier { _, _ -> true }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://cardholder-nup0.onrender.com/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)


    @Provides
    @Singleton
    @Named("viacep")
    fun provideViaCepRetrofit(json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://viacep.com.br/ws/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideBuscaCep(@Named("viacep") retrofit: Retrofit): BuscaCep =
        retrofit.create(BuscaCep::class.java)

    @Provides
    @Singleton
    @Named("mastra")
    fun provideMastraOkHttpClient(okHttpClient: OkHttpClient): OkHttpClient =
        okHttpClient
            .newBuilder()
            .readTimeout(0, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    @Named("mastra")
    fun provideMastraRetrofit(
        @Named("mastra") okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(MastraBaseUrlProvider.resolveBaseUrl())
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideMastraAgentService(@Named("mastra") retrofit: Retrofit): MastraAgentService =
        retrofit.create(MastraAgentService::class.java)
}
