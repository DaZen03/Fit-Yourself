package com.example.appfitness.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.appfitness.data.local.AppDatabase
import com.example.appfitness.databinding.FragmentPerfilBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cargarDatosUsuario()
    }

    override fun onResume() {
        super.onResume()
        cargarDatosUsuario()
    }

    private fun cargarDatosUsuario() {
        val contexto = context ?: return
        val db = AppDatabase.obtenerBaseDatos(contexto)
        val usuarioDao = db.usuarioDao()
        val entrenamientoDao = db.entrenamientoDao()

        lifecycleScope.launch(Dispatchers.IO) {
            val usuario = usuarioDao.obtenerUsuarioActual()
            val entrenamientos = usuario?.let {
                entrenamientoDao.obtenerEntrenamientosPorUsuario(it.id)
            } ?: entrenamientoDao.obtenerTodosEntrenamientos()

            withContext(Dispatchers.Main) {
                _binding?.let { bindingg ->
                    if (usuario != null) {
                        bindingg.tvNombreUsuario.text = usuario.usuario
                        bindingg.tvEmailUsuario.text = usuario.email
                    } else {
                        bindingg.tvNombreUsuario.text = "Usuario"
                        bindingg.tvEmailUsuario.text = "Sin correo registrado"
                    }
                    bindingg.tvTotalEntrenamientos.text = "Total de entrenamientos: ${entrenamientos.size}"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}