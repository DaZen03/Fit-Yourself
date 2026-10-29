package com.example.appfitness.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "series")
data class SerieEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val entrenamientoId: Long,
    val nombreEjercicio: String,
    val numeroSerie: Int,
    val pesoKg: Double,
    val repeticiones: Int
)