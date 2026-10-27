package com.example.appfitness.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp

@Entity(tableName = "entrenamientos")
data class EntrenamientoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val nombreRutina: String,
    val fecha: String,
    val duracionMinutos: Int = 0,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val ubicacionNombre: String? = null,
    val fotoRuta: String? = null,
    val fotoTiempo: Long? = null
)