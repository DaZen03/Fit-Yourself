package com.example.appfitness.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appfitness.data.LeaderboardAdapter
import com.example.appfitness.data.LeaderboardUser
import com.example.appfitness.data.local.AppDatabase
import com.example.appfitness.databinding.FragmentLeaderboardBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LeaderboardFragment : Fragment() {

    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: LeaderboardAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = LeaderboardAdapter()
        binding.rvLeaderboard.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLeaderboard.adapter = adapter

        cargarLeaderboard()
    }

    override fun onResume() {
        super.onResume()
        cargarLeaderboard()
    }

    private fun cargarLeaderboard() {
        val db = AppDatabase.obtenerBaseDatos(requireContext())
        val usuarioDao = db.usuarioDao()
        val entrenamientoDao = db.entrenamientoDao()

        lifecycleScope.launch(Dispatchers.IO) {
            val usuarios = usuarioDao.obtenerTodosUsuarios()
            val ranking = mutableListOf<LeaderboardUser>()

            for (usuario in usuarios) {
                val entrenamientos = entrenamientoDao.obtenerEntrenamientosPorUsuario(usuario.id)

                val diasUnicos = entrenamientos.map { it.fecha }.distinct().size

                ranking.add(LeaderboardUser(usuario.usuario, diasUnicos))
            }

            val rankingOrdenado = ranking.sortedByDescending { it.diasEntrenados }

            withContext(Dispatchers.Main) {
                adapter.actualizarLista(rankingOrdenado)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}