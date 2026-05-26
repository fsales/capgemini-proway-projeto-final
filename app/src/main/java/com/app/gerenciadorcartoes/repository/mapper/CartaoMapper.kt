package com.app.gerenciadorcartoes.repository.mapper

import com.app.gerenciadorcartoes.data.local.entity.CartaoEntity
import com.app.gerenciadorcartoes.model.Cartao

fun CartaoEntity.toDomain(): Cartao = Cartao(
    id          = id,
    nomeTitular = nomeTitular,
    finalNumero = finalNumero,
    bandeira    = bandeira,
    validade    = validade,
    limite      = limite,
    limiteMaximo= limiteMaximo,
    template    = template,
    bloqueado   = bloqueado,
)

fun Cartao.toEntity(): CartaoEntity = CartaoEntity(
    id          = id,
    nomeTitular = nomeTitular,
    finalNumero = finalNumero,
    bandeira    = bandeira,
    validade    = validade,
    limite      = limite,
    limiteMaximo= limiteMaximo.takeIf { it > 0.0 } ?: limite,
    template    = template,
    bloqueado   = bloqueado,
)
