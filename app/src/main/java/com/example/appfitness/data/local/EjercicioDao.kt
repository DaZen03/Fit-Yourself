package com.example.appfitness.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface EjercicioDao {

    @Query("select * from ejercicios order by nombre asc")
    suspend fun obtenerTodosEjercicios(): List<EjercicioEntity>

    @Query("select * from ejercicios where grupoMuscular = :grupo order by nombre asc")
    suspend fun obtenerEjerciciosPorGrupo(grupo: String): List<EjercicioEntity>

    @Insert
    suspend fun insertarEjercicio(ejercicio: EjercicioEntity)

    @Insert
    suspend fun insertarListaEjercicios(lista: List<EjercicioEntity>)
}