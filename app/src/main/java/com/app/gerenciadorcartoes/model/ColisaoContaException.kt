package com.app.gerenciadorcartoes.model

/**
 * Lançada por [com.app.gerenciadorcartoes.network.auth.AuthDataSource.autenticarComToken]
 * quando o e-mail Google já existe como conta email+senha no Firebase.
 *
 * Carrega [email] e [idToken] para que a camada de ViewModel possa orquestrar o
 * fluxo de vinculação sem conhecer detalhes da implementação do provedor.
 *
 * Zero importações de framework — pertence ao pacote model/.
 */
class ColisaoContaException(
    val email   : String,
    val idToken : String,
) : Exception("Este e-mail já possui uma conta criada com senha.")

