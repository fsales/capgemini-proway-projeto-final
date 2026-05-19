package com.app.gerenciadorcartoes.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CadastroUsuario")
data class CadastroUsuarioEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val nome: String = "",

    val cpf: String = "",

    val cep: Int = 0,

    val endereco: String = "",

    val number: String = "",

    val bairro: String = "",

    val estado: String = "",

    val email: String = "",

    val senha: String = "",
)
