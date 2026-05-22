package com.app.gerenciadorcartoes.data.local.session

import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named

class SessionManagerImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @param:Named(SESSION_SECURE_PREFS)
    private val securePreferences: SharedPreferences,
) : SessionManager {

    override suspend fun saveSession(usuario: String) {
        val usuarioNormalizado = usuario.trim()
        val loginTime = System.currentTimeMillis()

        dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = true
            preferences[USUARIO_LOGADO] = usuarioNormalizado
            preferences[LAST_LOGIN_TIME] = loginTime
        }

        securePreferences.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USUARIO_LOGADO, usuarioNormalizado)
            .putLong(KEY_LAST_LOGIN_TIME, loginTime)
            .apply()
    }

    override suspend fun logout() {
        dataStore.edit { preferences ->
            preferences.remove(IS_LOGGED_IN)
            preferences.remove(USUARIO_LOGADO)
            preferences.remove(LAST_LOGIN_TIME)
        }

        securePreferences.edit()
            .remove(KEY_IS_LOGGED_IN)
            .remove(KEY_USUARIO_LOGADO)
            .remove(KEY_LAST_LOGIN_TIME)
            .apply()
    }

    override fun isLoggedIn(): Flow<Boolean> {
        return dataStore.data
            .map { preferences ->
                preferences[IS_LOGGED_IN]
                    ?: securePreferences.getBoolean(KEY_IS_LOGGED_IN, false)
            }
            .catch {
                emit(securePreferences.getBoolean(KEY_IS_LOGGED_IN, false))
            }
    }

    override fun getUsuarioLogado(): Flow<String?> {
        return dataStore.data
            .map { preferences ->
                preferences[USUARIO_LOGADO]
                    ?: securePreferences.getString(KEY_USUARIO_LOGADO, null)
            }
            .catch {
                emit(securePreferences.getString(KEY_USUARIO_LOGADO, null))
            }
    }

    companion object {
        const val SESSION_SECURE_PREFS = "session_secure_prefs"

        private const val KEY_IS_LOGGED_IN = "session_is_logged_in"
        private const val KEY_USUARIO_LOGADO = "session_usuario_logado"
        private const val KEY_LAST_LOGIN_TIME = "session_last_login_time"

        private val IS_LOGGED_IN = booleanPreferencesKey(KEY_IS_LOGGED_IN)
        private val USUARIO_LOGADO = stringPreferencesKey(KEY_USUARIO_LOGADO)
        private val LAST_LOGIN_TIME = longPreferencesKey(KEY_LAST_LOGIN_TIME)
    }
}


