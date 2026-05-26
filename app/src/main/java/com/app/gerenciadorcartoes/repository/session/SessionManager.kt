package com.app.gerenciadorcartoes.repository.session

import kotlinx.coroutines.flow.Flow

interface SessionManager {
    suspend fun saveSession(usuario: String)
    suspend fun logout()
    fun isLoggedIn(): Flow<Boolean>
    fun getUsuarioLogado(): Flow<String?>
}
