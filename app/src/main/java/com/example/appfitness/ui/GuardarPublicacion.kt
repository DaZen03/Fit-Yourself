package com.example.appfitness.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.appfitness.data.local.AppDatabase
import com.example.appfitness.data.local.EntrenamientoEntity
import com.example.appfitness.data.local.SerieEntity
import com.example.appfitness.databinding.FragmentGuardarPublicacionBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.appfitness.data.local.EntrenamientoDao


class GuardarPublicacion : Fragment() {

    private var _binding: FragmentGuardarPublicacionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGuardarPublicacionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.obtenerBaseDatos(requireContext())
        val entrenamientoDao = db.entrenamientoDao()
        val serieDao = db.serieDao()

        binding.btnGuardar.setOnClickListener {
            val rutina = binding.eNombreRutina.text.toString().trim()
            val ejercicio = binding.eNombreEjercicio.text.toString().trim()
            val pesoTexto = binding.ePesoKg.text.toString().trim()
            val repsTexto = binding.eRepeticiones.text.toString().trim()
            val duracionTexto = binding.eDuracion.text.toString().trim()

            if (rutina.isEmpty() || ejercicio.isEmpty() || pesoTexto.isEmpty() || repsTexto.isEmpty() || duracionTexto.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val peso = pesoTexto.toDoubleOrNull() ?: 0.0
            val repeticiones = repsTexto.toIntOrNull() ?: 0
            val duracion = duracionTexto.toIntOrNull() ?: 0
            val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            lifecycleScope.launch(Dispatchers.IO) {

                val nuevoEntrenamiento = EntrenamientoEntity(
                    usuarioId = 1,
                    nombreRutina = rutina,
                    fecha = fechaActual,
                    duracionMinutos = duracion
                )
                val entrenamientoId = entrenamientoDao.insertarEntrenamiento(nuevoEntrenamiento)


                val nuevaSerie = SerieEntity(
                    entrenamientoId = entrenamientoId,
                    nombreEjercicio = ejercicio,
                    numeroSerie = 1,
                    pesoKg = peso,
                    repeticiones = repeticiones
                )
                serieDao.insertarSerie(nuevaSerie)

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "¡Entrenamiento guardado con éxito!", Toast.LENGTH_SHORT).show()
                    limpiarFormulario()
                }
            }
        }
    }

    private fun limpiarFormulario() {
        binding.eNombreRutina.text?.clear()
        binding.eNombreEjercicio.text?.clear()
        binding.ePesoKg.text?.clear()
        binding.eRepeticiones.text?.clear()
        binding.eDuracion.text?.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}