package com.example.appfitness.data

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appfitness.data.local.UsuarioEntity
import com.example.appfitness.databinding.ItemUsuarioBinding

class UsuarioAdapter (
    private var listarUsuarios: List<UsuarioEntity> = emptyList(),
    private val onUsuarioClick: (UsuarioEntity) -> Unit
): RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder>() {


    inner class UsuarioViewHolder(val binding: ItemUsuarioBinding):
    RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val binding = ItemUsuarioBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return UsuarioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = listarUsuarios[position]
        holder.binding.tvTitulo.text = usuario.usuario
        holder.binding.tvDescripcion.text = usuario.email

        holder.itemView.setOnClickListener {
            onUsuarioClick(usuario)
        }
    }

    override fun getItemCount(): Int = listarUsuarios.size


}