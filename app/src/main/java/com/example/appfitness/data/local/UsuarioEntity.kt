package com.example.appfitness.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "login")
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuario: String,
    val email: String,
    val contrasena: String
)