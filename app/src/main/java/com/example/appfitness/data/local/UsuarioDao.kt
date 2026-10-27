package com.example.appfitness.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UsuarioDao {
    @Query("select * from login where email = :email limit 1")
    suspend fun buscarCorreo(email: String): UsuarioEntity?


   @Insert
   suspend fun insertarUsuario(usuario: UsuarioEntity)
}