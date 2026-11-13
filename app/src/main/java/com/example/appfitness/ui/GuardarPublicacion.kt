package com.example.appfitness.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.currentCompositionErrors
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.appfitness.R
import com.example.appfitness.data.local.AppDatabase
import com.example.appfitness.data.local.EntrenamientoEntity
import com.example.appfitness.data.local.SerieEntity
import com.example.appfitness.databinding.FragmentGuardarPublicacionBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.android.MapLibre
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.OnMapReadyCallback
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GuardarPublicacion : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentGuardarPublicacionBinding? = null
    private val binding get() = _binding!!

    private var currentMarker: org.maplibre.android.annotations.Marker? = null
    private lateinit var mapView: MapView
    private var mapLibreMap: MapLibreMap? = null

    private var fotoRuta: String? = null
    private var fotoTiempo: Long? = null
    private lateinit var fotoUbicacion: Uri
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

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
            try {
                val context = requireContext()
                val inputStream = context.contentResolver.openInputStream(fotoUbicacion)
                val opciones = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
                android.graphics.BitmapFactory.decodeStream(inputStream, null, opciones)
                inputStream?.close()

                val maximo = 600
                var escala = 1
                while (opciones.outWidth / escala / 2 >= maximo && opciones.outHeight / escala / 2 >= maximo) {
                    escala *= 2
                }

                val inputStream2 = context.contentResolver.openInputStream(fotoUbicacion)
                val decodeOptions = android.graphics.BitmapFactory.Options().apply { inSampleSize = escala }
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream2, null, decodeOptions)
                inputStream2?.close()

                if (bitmap != null) {
                    val archivoOriginal = File(context.filesDir, fotoUbicacion.lastPathSegment ?: "foto.jpg")
                    val outputStream = java.io.FileOutputStream(archivoOriginal)
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
                    outputStream.flush()
                    outputStream.close()
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }


            fotoRuta = fotoUbicacion.toString()
            fotoTiempo = System.currentTimeMillis()
            Toast.makeText(requireContext(), "Foto tomada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        MapLibre.getInstance(requireContext())
        _binding = FragmentGuardarPublicacionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())


        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        val db = AppDatabase.obtenerBaseDatos(requireContext())
        val entrenamientoDao = db.entrenamientoDao()
        val serieDao = db.serieDao()

        binding.btnTomarfoto.setOnClickListener {
            verificarPermisoYCapturar()
        }

        binding.btnGuardarUbicacion.setOnClickListener {
            obtenerUbicacionGps()
        }

        binding.btnGuardar.setOnClickListener {
            val rutina = binding.eNombreRutina.text.toString().trim()
            val ejercicio = binding.eNombreEjercicio.text.toString().trim()
            val pesoTexto = binding.ePesoKg.text.toString().trim()
            val repsTexto = binding.eRepeticiones.text.toString().trim()
            val duracionTexto = binding.eDuracion.text.toString().trim()
            val ubicacionInput = binding.eUbicacion.text.toString().trim()

            if (rutina.isEmpty() || ejercicio.isEmpty() || pesoTexto.isEmpty() || repsTexto.isEmpty() || duracionTexto.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val peso = pesoTexto.toDouble()
            val repeticiones = repsTexto.toInt()
            val duracion = duracionTexto.toInt()
            val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val nombreUbicacionFinal = if (ubicacionInput.isNotEmpty()) ubicacionInput else null

            lifecycleScope.launch(Dispatchers.IO) {
                val nuevoEntrenamiento = EntrenamientoEntity(
                    usuarioId = 1,
                    nombreRutina = rutina,
                    fecha = fechaActual,
                    duracionMinutos = duracion,
                    ubicacionNombre = nombreUbicacionFinal,
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
                    Toast.makeText(requireContext(), "Entrenamiento guardado", Toast.LENGTH_SHORT).show()
                    limpiarFormulario()
                }
            }
        }
    }

    override fun onMapReady(map: MapLibreMap) {
        mapLibreMap = map

        map.setStyle("https://tiles.openfreemap.org/styles/liberty") { style ->
            Toast.makeText(requireContext(), "Mapa Listo", Toast.LENGTH_SHORT).show()
        }

        mapLibreMap?.addOnMapClickListener { punto ->
            val latitud = punto.latitude
            val longitud = punto.longitude

            currentMarker?.let { mapLibreMap?.removeMarker(it) }

            val markerOptions = org.maplibre.android.annotations.MarkerOptions().apply {
                position = org.maplibre.android.geometry.LatLng(latitud, longitud)
                title = "Ubicación de entreno"
            }
            currentMarker = mapLibreMap?.addMarker(markerOptions)

            binding.eUbicacion.setText(String.format(Locale.getDefault(), "Lat: %.4f, Lon: %.4f", latitud, longitud))
            Toast.makeText(requireContext(), "Ubicación seleccionada", Toast.LENGTH_SHORT).show()
            true
        }
    }

    private val solicitarPermisosUbicacion = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        val concedido = permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (concedido) {
            obtenerUbicacionGps()
        } else {
            Toast.makeText(requireContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun obtenerUbicacionGps() {
        val fineGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    val marcador = org.maplibre.android.geometry.LatLng(lat, lon)

                    currentMarker?.let { mapLibreMap?.removeMarker(it) }
                    currentMarker = mapLibreMap?.addMarker(
                        org.maplibre.android.annotations.MarkerOptions()
                            .position(marcador)
                            .title("Ubicacion GPS")
                    )

                    mapLibreMap?.cameraPosition = org.maplibre.android.camera.CameraPosition.Builder()
                        .target(marcador)
                        .zoom(15.0)
                        .build()

                    binding.eUbicacion.setText(String.format(Locale.getDefault(), "Lat: %.4f, Lon: %.4f", lat, lon))
                    Toast.makeText(requireContext(), "Ubicación GPS obtenida", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "GPS sin señal reciente.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {

            solicitarPermisosUbicacion.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    private fun verificarPermisoYCapturar() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispararCapturaCamera()
        } else {
            solicitarPermisoCamera.launch(Manifest.permission.CAMERA)
        }
    }

    private fun dispararCapturaCamera() {
        val archivoFoto = File(requireContext().filesDir, "foto_${System.currentTimeMillis()}.jpg")
        fotoUbicacion = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            archivoFoto
        )
        tomarFotoLauncher.launch(fotoUbicacion)
    }

    private fun limpiarFormulario() {
        binding.eNombreRutina.text?.clear()
        binding.eNombreEjercicio.text?.clear()
        binding.ePesoKg.text?.clear()
        binding.eRepeticiones.text?.clear()
        binding.eDuracion.text?.clear()
        binding.eUbicacion.text?.clear()
        fotoRuta = null
        fotoTiempo = null
    }

    override fun onStart() { super.onStart(); mapView.onStart() }
    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { super.onPause(); mapView.onPause() }
    override fun onStop() { super.onStop(); mapView.onStop() }
    override fun onSaveInstanceState(outState: Bundle) { super.onSaveInstanceState(outState); mapView.onSaveInstanceState(outState) }
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
        _binding = null
    }
}