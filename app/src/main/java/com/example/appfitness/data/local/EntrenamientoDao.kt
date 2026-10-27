package com.example.appfitness.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface EntrenamientoDao {

    @Query("select * from entrenamientos where usuarioId = :usuarioId order by id desc")
    suspend fun obtenerEntrenamientosPorUsuario(usuarioId: Int): List<EntrenamientoEntity>

    @Query("select * from entrenamientos order by id desc")
    suspend fun obtenerTodosEntrenamientos(): List<EntrenamientoEntity>

    @Insert
    suspend fun insertarEntrenamiento(entrenamiento: EntrenamientoEntity): Long

    @Query("select * from entrenamientos where fotoRuta is not null and fotoTiempo is not null")
    suspend fun obtenerFotoConEntreno(ahora: Long = System.currentTimeMillis()): List<EntrenamientoEntity>

    @Query("Update entrenamientos set fotoRuta = null, fotoTiempo = null " )
    suspend fun resetearTiempo(): Int


}




