package com.example.appfitness.data

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appfitness.data.local.EntrenamientoEntity
import com.example.appfitness.databinding.ItemUsuarioBinding

class EntrenamientoAdapter(
    private var entrenamientos: List<EntrenamientoEntity> = emptyList()
) : RecyclerView.Adapter<EntrenamientoAdapter.EntrenamientoViewHolder>() {

    class EntrenamientoViewHolder(val binding: ItemUsuarioBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntrenamientoViewHolder {
        val binding = ItemUsuarioBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return EntrenamientoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EntrenamientoViewHolder, position: Int) {
        val entreno = entrenamientos[position]
        holder.binding.tvTitulo.text = entreno.nombreRutina
        holder.binding.tvDescripcion.text = "Fecha: ${entreno.fecha} | Duración: ${entreno.duracionMinutos} min"

        if (!entreno.fotoRuta.isNullOrEmpty()) {
            holder.binding.miFoto.visibility = View.VISIBLE
            holder.binding.miFoto.setImageURI(Uri.parse(entreno.fotoRuta))
        } else {
            holder.binding.miFoto.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = entrenamientos.size

    fun actualizarLista(nuevaLista: List<EntrenamientoEntity>) {
        entrenamientos = nuevaLista
        notifyDataSetChanged()
    }
}