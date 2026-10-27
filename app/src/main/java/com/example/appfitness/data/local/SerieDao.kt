package com.example.appfitness.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SerieDao {

    @Query("select * from series where entrenamientoId = :entrenamientoId order by numeroSerie asc")
    suspend fun obtenerSeriesPorEntrenamiento(entrenamientoId: Long): List<SerieEntity>

    @Insert
    suspend fun insertarSerie(serie: SerieEntity)

    @Insert
    suspend fun insertarListaSeries(series: List<SerieEntity>)
}