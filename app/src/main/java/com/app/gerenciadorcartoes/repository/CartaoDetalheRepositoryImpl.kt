package com.app.gerenciadorcartoes.repository

import com.app.gerenciadorcartoes.model.Cartao
import com.app.gerenciadorcartoes.model.CartaoDetalhe
import com.app.gerenciadorcartoes.model.InstituicaoFinanceira
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CartaoDetalheRepositoryImpl @Inject constructor(
    private val cartaoRepository: CartaoRepository,
) : CartaoDetalheRepository {

    override fun observarDetalhePorId(id: Long): Flow<CartaoDetalhe?> =
        cartaoRepository.observarPorId(id).map { cartao ->
            cartao?.let(::montarDetalheMockado)
    }

    // Ponto de troca futura: substituir este mock por dados reais de fatura/limite.
    private fun montarDetalheMockado(cartao: Cartao): CartaoDetalhe =
        CartaoDetalhe(
            cartao          = cartao,
            instituicao     = InstituicaoFinanceira(nome = instituicaoPorTemplate(cartao.template)),
            limiteUtilizado = LIMITE_UTILIZADO_PADRAO,
        )

    private fun instituicaoPorTemplate(template: String): String =
        when (template) {
            "bradesco" -> "Bradesco"
            "itau"     -> "Itaú"
            "nubank"   -> "Nubank"
            "inter"    -> "Inter"
            "c6bank"   -> "C6 Bank"
            else       -> "Cartão"
        }

    private companion object {
        const val LIMITE_UTILIZADO_PADRAO = 500.0
    }
}
