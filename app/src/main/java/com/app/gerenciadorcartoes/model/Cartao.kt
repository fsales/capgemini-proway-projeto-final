package com.app.gerenciadorcartoes.model

// Modelo de domínio do cartão de crédito/débito.
// finalNumero: apenas os 4 últimos dígitos (exibição e armazenamento).
data class Cartao(
    val id          : Long   = 0L,
    val nomeTitular : String = "",
    val finalNumero : String = "",    // ex: "1234"
    val bandeira    : String = "",    // ex: "Visa", "Mastercard", "Elo"
    val validade    : String = "",    // ex: "12/28"
    val limite      : Double = 0.0,
    val template    : String = "default", // ex: "default", "bradesco", "itau", "nubank", "inter", "c6bank"
)
