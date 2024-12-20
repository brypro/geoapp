package com.brypro.socialgeo

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class SolicitudesAdapter(
    private val onAccionClick: (String, Boolean) -> Unit
) : RecyclerView.Adapter<SolicitudesAdapter.SolicitudViewHolder>() {

    private val solicitudes = mutableListOf<Solicitud>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SolicitudViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_solicitud, parent, false)
        return SolicitudViewHolder(view)
    }

    override fun onBindViewHolder(holder: SolicitudViewHolder, position: Int) {
        val solicitud = solicitudes[position]
        holder.bind(solicitud)
    }

    override fun getItemCount(): Int = solicitudes.size

    fun setSolicitudes(nuevasSolicitudes: List<Solicitud>) {
        solicitudes.clear()
        solicitudes.addAll(nuevasSolicitudes)
        this.notifyDataSetChanged()
    }

    inner class SolicitudViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewUsuarioA: TextView = itemView.findViewById(R.id.textViewUsuarioA)
        private val buttonAceptar: Button = itemView.findViewById(R.id.buttonAceptar)
        private val buttonRechazar: Button = itemView.findViewById(R.id.buttonRechazar)

        fun bind(solicitud: Solicitud) {
            textViewUsuarioA.text = solicitud.usuarioA

            buttonAceptar.setOnClickListener {
                onAccionClick(solicitud.id, true)
            }

            buttonRechazar.setOnClickListener {
                onAccionClick(solicitud.id, false)
            }
        }
    }
}
