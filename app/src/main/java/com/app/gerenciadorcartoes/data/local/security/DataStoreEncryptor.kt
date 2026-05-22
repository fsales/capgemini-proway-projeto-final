package com.app.gerenciadorcartoes.data.local.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Criptografia de valores sensíveis via Android Keystore (AES-256-GCM).
 *
 * Substitui `EncryptedSharedPreferences` (depreciada em security-crypto 1.1.0) para proteção
 * dos valores armazenados no DataStore, seguindo a orientação oficial do Jetpack:
 * "uso direto do Android Keystore".
 *
 * ### Como funciona
 * - Gera (ou reutiliza) uma chave AES-256 no AndroidKeyStore sob o alias [KEY_ALIAS].
 * - A chave nunca sai do hardware seguro — apenas o ciphertext trafega na memória do processo.
 * - Cada chamada a [encrypt] usa um IV aleatório de 12 bytes (requisito de segurança do GCM).
 * - O formato armazenado é: `Base64( IV[12] || ciphertext || GCM-Tag[16] )`.
 * - [decrypt] verifica a GCM authentication tag — qualquer adulteração retorna `null`.
 *
 * ### Limitações conhecidas
 * - Requer que o dispositivo já tenha sido desbloqueado ao menos uma vez após a inicialização
 *   (`setUnlockedDeviceRequired(true)` — API 28+, compatível com minSdk = 28 deste projeto).
 * - Se a chave for removida do Keystore (ex.: reset de PIN), [decrypt] retorna `null`.
 *   O chamador deve tratar isso como sessão expirada.
 */
object DataStoreEncryptor {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS        = "gerenciador_cartoes_ds_key"
    private const val ALGORITHM        = "AES/GCM/NoPadding"
    private const val GCM_IV_SIZE      = 12    // bytes — padrão NIST para GCM
    private const val GCM_TAG_SIZE     = 128   // bits  — tag de autenticação

    // ── Keystore ─────────────────────────────────────────────────────────────

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (keyStore.containsAlias(KEY_ALIAS)) {
            return (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
        }
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUnlockedDeviceRequired(true)    // API 28+ — minSdk deste projeto = 28
            .build()

        return KeyGenerator
            .getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            .apply { init(spec) }
            .generateKey()
    }

    // ── API pública ───────────────────────────────────────────────────────────

    /**
     * Criptografa [plaintext] e retorna uma string Base64 de `IV || ciphertext`.
     * Um IV aleatório é gerado a cada chamada — garantia de segurança GCM.
     */
    fun encrypt(plaintext: String): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())

        val iv         = cipher.iv                                        // 12 bytes aleatórios
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        return Base64.encodeToString(iv + ciphertext, Base64.NO_WRAP)
    }

    /**
     * Decifra um valor produzido por [encrypt].
     * Retorna `null` se o Base64 for inválido ou se a GCM authentication tag falhar
     * (dado adulterado ou chave substituída no Keystore).
     */
    fun decrypt(encoded: String): String? =
        try {
            val data       = Base64.decode(encoded, Base64.NO_WRAP)
            val iv         = data.copyOfRange(0, GCM_IV_SIZE)
            val ciphertext = data.copyOfRange(GCM_IV_SIZE, data.size)

            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_SIZE, iv))

            cipher.doFinal(ciphertext).toString(Charsets.UTF_8)
        } catch (_: Exception) { null }
}

