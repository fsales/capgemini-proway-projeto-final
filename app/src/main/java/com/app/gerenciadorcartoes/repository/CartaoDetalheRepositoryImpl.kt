package com.app.gerenciadorcartoes.repository

import com.app.gerenciadorcartoes.model.Cartao
import com.app.gerenciadorcartoes.model.CartaoDetalhe
import com.app.gerenciadorcartoes.model.InstituicaoFinanceira
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CartaoDetalheRepositoryImpl @Inject constructor(
    private val cartaoRepository: CartaoRepository,
    private val faturaRepository: FaturaRepository,
) : CartaoDetalheRepository {

    override fun observarDetalhePorId(id: Long): Flow<CartaoDetalhe?> =
        cartaoRepository.observarPorId(id).map { cartao ->
            cartao?.let(::montarDetalheMockado)
        }

    private fun montarDetalheMockado(cartao: Cartao): CartaoDetalhe =
        CartaoDetalhe(
            cartao          = cartao,
            instituicao     = InstituicaoFinanceira(nome = instituicaoPorTemplate(cartao.template)),
            limiteUtilizado = faturaRepository.totalAPartirDoMesAtual(cartao.id),
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

}
