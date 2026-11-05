package com.example.appfitness.data

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appfitness.databinding.ItemLeaderboardBinding

data class LeaderboardUser(
    val usuario: String,
    val rachaDias: Int
)

class LeaderboardAdapter(
    private var lista: List<LeaderboardUser> = emptyList()
) : RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {

    class LeaderboardViewHolder(val binding: ItemLeaderboardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val binding = ItemLeaderboardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LeaderboardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val item = lista[position]
        holder.binding.tvPosicion.text = "${position + 1}"
        holder.binding.tvNombreUsuario.text = item.usuario
        holder.binding.tvDetalleEntrenamientos.text = "${item.rachaDias} días de racha"
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nuevaLista: List<LeaderboardUser>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}


