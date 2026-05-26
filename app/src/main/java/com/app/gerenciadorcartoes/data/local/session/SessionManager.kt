package com.app.gerenciadorcartoes.data.local.session

import kotlinx.coroutines.flow.Flow

interface SessionManager {
    suspend fun saveSession(userId: String)       // armazena o id do provedor de auth cifrado (AES-256-GCM)
    suspend fun logout()
    fun isLoggedIn(): Flow<Boolean>
    fun getSessionUserId(): Flow<String?>         // retorna o id do provedor de auth ou null
}
