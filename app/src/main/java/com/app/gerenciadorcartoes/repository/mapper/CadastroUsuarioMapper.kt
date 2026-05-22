package com.app.gerenciadorcartoes.repository.mapper

import com.app.gerenciadorcartoes.data.local.entity.CadastroUsuarioEntity
import com.app.gerenciadorcartoes.model.CadastroUsuario

fun CadastroUsuarioEntity.toDomain(): CadastroUsuario = CadastroUsuario(
    id       = id,
    nome     = nome,
    cpf      = cpf,
    cep      = cep,
    endereco = endereco,
    number   = number,
    bairro   = bairro,
    estado   = estado,
    email    = email,
    senha    = senha,
)

fun CadastroUsuario.toEntity(): CadastroUsuarioEntity = CadastroUsuarioEntity(
    id       = id,
    nome     = nome,
    cpf      = cpf,
    cep      = cep,
    endereco = endereco,
    number   = number,
    bairro   = bairro,
    estado   = estado,
    email    = email,
    senha    = senha,
)

