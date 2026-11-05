package com.example.appfitness.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appfitness.data.EntrenamientoAdapter
import com.example.appfitness.data.local.AppDatabase
import com.example.appfitness.databinding.FragmentFragmentoFeedBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentoFeed : Fragment() {

    private var _binding: FragmentFragmentoFeedBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: EntrenamientoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFragmentoFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = EntrenamientoAdapter()
        binding.rvNotas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotas.adapter = adapter

        cargarEntrenamientos()
    }

    override fun onResume() {
        super.onResume()
        cargarEntrenamientos()
    }

    private fun cargarEntrenamientos() {
        val dao = AppDatabase.obtenerBaseDatos(requireContext()).entrenamientoDao()
        lifecycleScope.launch(Dispatchers.IO) {
            val lista = dao.obtenerTodosEntrenamientos()
            withContext(Dispatchers.Main) {
                adapter.actualizarLista(lista)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}