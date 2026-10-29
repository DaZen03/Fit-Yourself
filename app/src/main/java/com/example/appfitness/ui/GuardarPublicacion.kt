package com.example.appfitness.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.appfitness.data.local.AppDatabase
import com.example.appfitness.data.local.EntrenamientoEntity
import com.example.appfitness.data.local.SerieEntity
import com.example.appfitness.databinding.FragmentGuardarPublicacionBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GuardarPublicacion : Fragment() {

    private var _binding: FragmentGuardarPublicacionBinding? = null
    private val binding get() = _binding!!

    private var fotoRuta: String? = null
    private var fotoTiempo: Long? = null
    private lateinit var fotoUri: Uri

    private val solicitarPermisoCamera = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            dispararCapturaCamera()
        } else {
            Toast.makeText(requireContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private val tomarFotoLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito ->
        if (exito) {
            binding.fotoPrev.visibility = View.VISIBLE
            binding.fotoPrev.setImageURI(fotoUri)
            fotoRuta = fotoUri.toString()
            fotoTiempo = System.currentTimeMillis()
            Toast.makeText(requireContext(), "Foto tomada con éxito", Toast.LENGTH_SHORT).show()
        }
    }

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

        binding.btnTomarfoto.setOnClickListener {
            verificarPermisoYCapturar()
        }

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
                    duracionMinutos = duracion,
                    fotoRuta = fotoRuta,
                    fotoTiempo = fotoTiempo
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
                    Toast.makeText(requireContext(), "Entrenamiento guardado con éxito!", Toast.LENGTH_SHORT).show()
                    limpiarFormulario()
                }
            }
        }
    }

    private fun verificarPermisoYCapturar() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispararCapturaCamera()
        } else {
            solicitarPermisoCamera.launch(Manifest.permission.CAMERA)
        }

        private fun verificarPermisoYCapturar() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                dispararCapturaCamera()
            }
            else -> {
                solicitarPermisoCamera.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun dispararCapturaCamera() {
        val archivoFoto = File(requireContext().filesDir, "foto_${System.currentTimeMillis()}.jpg")
        fotoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            archivoFoto
        )
        tomarFotoLauncher.launch(fotoUri)
    }

    private fun limpiarFormulario() {
        binding.eNombreRutina.text?.clear()
        binding.eNombreEjercicio.text?.clear()
        binding.ePesoKg.text?.clear()
        binding.eRepeticiones.text?.clear()
        binding.eDuracion.text?.clear()
        binding.fotoPrev.visibility = View.GONE
        fotoRuta = null
        fotoTiempo = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}