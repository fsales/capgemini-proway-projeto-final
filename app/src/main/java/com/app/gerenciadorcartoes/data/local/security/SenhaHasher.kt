package com.app.gerenciadorcartoes.data.local.security

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Utilitário de hash de senhas usando **PBKDF2WithHmacSHA256**.
 *
 * Algoritmo escolhido por estar disponível no SDK Android (≥ API 19) sem
 * dependências externas e por ser recomendado pelo OWASP para armazenamento
 * seguro de senhas.
 *
 * ### Formato armazenado
 * A string gravada no banco tem o formato `"<salt_b64>:<hash_b64>"`, permitindo
 * que salt e hash sejam recuperados e comparados sem nenhuma coluna extra.
 *
 * ### Parâmetros de segurança
 * | Parâmetro | Valor | Justificativa |
 * |-----------|-------|---------------|
 * | Salt      | 16 bytes aleatórios (SecureRandom) | Único por usuário; evita ataques de rainbow table |
 * | Iterações | 120 000 | Dobro do mínimo recomendado pelo OWASP 2023 para PBKDF2-HMAC-SHA256 |
 * | Chave     | 256 bits | Tamanho nativo do HmacSHA256 — sem truncamento |
 *
 * ### Comparação em tempo constante
 * [verificar] usa [MessageDigest.isEqual] para comparação byte a byte em tempo
 * constante, impedindo ataques de temporização (*timing attacks*).
 */
object SenhaHasher {

    private const val ALGORITMO     = "PBKDF2WithHmacSHA256"
    private const val ITERACOES     = 120_000
    private const val TAMANHO_CHAVE = 256   // bits
    private const val TAMANHO_SALT  = 16    // bytes
    private const val SEPARADOR     = ":"

    /**
     * Gera um hash seguro para [senha] e retorna `"<salt_b64>:<hash_b64>"`.
     * Chame este método **uma única vez** no cadastro — nunca no login.
     */
    fun hash(senha: String): String {
        val salt = ByteArray(TAMANHO_SALT).also { SecureRandom().nextBytes(it) }
        val hash = derivar(senha.toCharArray(), salt)
        return "${Base64.encodeToString(salt, Base64.NO_WRAP)}$SEPARADOR${Base64.encodeToString(hash, Base64.NO_WRAP)}"
    }

    /**
     * Retorna `true` se [senhaPlana] corresponde ao [hashArmazenado] no formato `"salt:hash"`.
     * A comparação é feita em **tempo constante** para prevenir timing attacks.
     */
    fun verificar(senhaPlana: String, hashArmazenado: String): Boolean {
        val partes = hashArmazenado.split(SEPARADOR, limit = 2)
        if (partes.size != 2) return false
        return try {
            val salt          = Base64.decode(partes[0], Base64.NO_WRAP)
            val hashEsperado  = Base64.decode(partes[1], Base64.NO_WRAP)
            val hashCalculado = derivar(senhaPlana.toCharArray(), salt)
            // Comparação em tempo constante — evita timing attacks
            MessageDigest.isEqual(hashCalculado, hashEsperado)
        } catch (_: Exception) {
            false
        }
    }

    private fun derivar(senha: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(senha, salt, ITERACOES, TAMANHO_CHAVE)
        return try {
            SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).encoded
        } finally {
            spec.clearPassword() // limpa a senha da memória após o uso
        }
    }
}

