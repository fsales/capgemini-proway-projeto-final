package com.app.gerenciadorcartoes.data.local.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.app.gerenciadorcartoes.data.local.security.DataStoreEncryptor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SessionManagerImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SessionManager {

    override suspend fun saveSession(usuario: String) {
        val usuarioNormalizado = usuario.trim()
        val usuarioCifrado     = DataStoreEncryptor.encrypt(usuarioNormalizado)   // AES-256-GCM
        val loginTime          = System.currentTimeMillis()

        dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN]    = true
            preferences[USUARIO_LOGADO]  = usuarioCifrado   // armazenado cifrado
            preferences[LAST_LOGIN_TIME] = loginTime
        }
    }

    override suspend fun logout() {
        dataStore.edit { preferences ->
            preferences.remove(IS_LOGGED_IN)
            preferences.remove(USUARIO_LOGADO)
            preferences.remove(LAST_LOGIN_TIME)
        }
    }

    override fun isLoggedIn(): Flow<Boolean> =
        dataStore.data
            .map { preferences -> preferences[IS_LOGGED_IN] ?: false }
            .catch { emit(false) }

    // Decifra antes de emitir — null se chave ausente ou dado adulterado (sessão expirada)
    override fun getUsuarioLogado(): Flow<String?> =
        dataStore.data
            .map { preferences ->
                preferences[USUARIO_LOGADO]?.let { DataStoreEncryptor.decrypt(it) }
            }
            .catch { emit(null) }

    companion object {
        private const val KEY_IS_LOGGED_IN    = "session_is_logged_in"
        private const val KEY_USUARIO_LOGADO  = "session_usuario_logado"
        private const val KEY_LAST_LOGIN_TIME = "session_last_login_time"

        private val IS_LOGGED_IN    = booleanPreferencesKey(KEY_IS_LOGGED_IN)
        private val USUARIO_LOGADO  = stringPreferencesKey(KEY_USUARIO_LOGADO)
        private val LAST_LOGIN_TIME = longPreferencesKey(KEY_LAST_LOGIN_TIME)
    }
}
