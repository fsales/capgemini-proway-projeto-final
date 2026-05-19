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
    template    = template,
)

fun Cartao.toEntity(): CartaoEntity = CartaoEntity(
    id          = id,
    nomeTitular = nomeTitular,
    finalNumero = finalNumero,
    bandeira    = bandeira,
    validade    = validade,
    limite      = limite,
    template    = template,
)
