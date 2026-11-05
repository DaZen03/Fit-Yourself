package com.example.appfitness.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UsuarioDao {
    @Query("select * from login where email = :email limit 1")
    suspend fun buscarCorreo(email: String): UsuarioEntity?

    @Query("select * from login order by id asc limit 1")
    suspend fun obtenerUsuarioActual(): UsuarioEntity?

   @Insert
   suspend fun insertarUsuario(usuario: UsuarioEntity)

    @Query("select * from login")
    suspend fun obtenerTodosUsuarios(): List<UsuarioEntity>

}
