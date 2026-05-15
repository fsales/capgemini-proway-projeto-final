package com.app.gerenciadorcartoes.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cartoes")
data class CartaoEntity(
    @PrimaryKey(autoGenerate = true)
    val id          : Long   = 0L,
    val nomeTitular : String,
    val finalNumero : String, // últimos 4 dígitos
    val bandeira    : String,
    val validade    : String, // MM/AA
    val limite      : Double,
)
