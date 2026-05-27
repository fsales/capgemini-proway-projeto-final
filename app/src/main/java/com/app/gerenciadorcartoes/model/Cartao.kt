package com.app.gerenciadorcartoes.model

import com.app.gerenciadorcartoes.extensions.toValidadeFormatada

// Modelo de domínio do cartão de crédito/débito.
// finalNumero: apenas os 4 últimos dígitos (exibição e armazenamento).
data class Cartao(
    val id          : Long   = 0L,
    val nomeTitular : String = "",
    val finalNumero : String = "",    // ex: "1234"
    val bandeira    : String = "",    // ex: "Visa", "Mastercard", "Elo"
    val validade    : String = "",    // ex: "12/28"
    val limite      : Double = 0.0,
    val limiteMaximo: Double = 0.0,
    val template    : String = "default", // ex: "default", "bradesco", "itau", "nubank", "inter", "c6bank"
    val bloqueado   : Boolean  = false,
    val usuarioId: Long? = null,
    // Identificador gerado pelo cliente para idempotência/sincronização (opcional)
    val clientId     : String? = null,
) {
    companion object {
        /**
         * Cria um Cartao a partir dos dados brutos da UI.
         * Centraliza a lógica de formatação (ex: MM/AA) no domínio.
         */
        fun fromUi(
            id: Long,
            nomeTitular: String,
            finalNumero: String,
            bandeira: String,
            validadeRaw: String, // ex: "1228"
            limite: Double,
            limiteMaximo: Double,
            template: String,
            usuarioId: Long?
        ) = Cartao(
            id = id,
            nomeTitular = nomeTitular.trim(),
            finalNumero = finalNumero.trim(),
            bandeira = bandeira.trim(),
            validade = validadeRaw.toValidadeFormatada(),
            limite = limite,
            limiteMaximo = limiteMaximo,
            template = template,
            usuarioId = usuarioId
        )
    }
}


