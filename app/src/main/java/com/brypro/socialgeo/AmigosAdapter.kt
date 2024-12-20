package com.brypro.socialgeo

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AmigosAdapter(
    private val onAmigoClick: (Amigo) -> Unit
) : RecyclerView.Adapter<AmigosAdapter.AmigoViewHolder>() {

    private val amigos = mutableListOf<Amigo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AmigoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_amigo, parent, false)
        return AmigoViewHolder(view)
    }

    override fun onBindViewHolder(holder: AmigoViewHolder, position: Int) {
        val amigo = amigos[position]
        holder.bind(amigo)
    }

    override fun getItemCount(): Int = amigos.size

    fun setAmigos(nuevosAmigos: List<Amigo>) {
        amigos.clear()
        amigos.addAll(nuevosAmigos)
        notifyDataSetChanged()
    }

    inner class AmigoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewNombre: TextView = itemView.findViewById(R.id.textViewNombre)
        private val buttonVer: Button = itemView.findViewById(R.id.buttonVer)

        fun bind(amigo: Amigo) {
            textViewNombre.text = amigo.email
            buttonVer.setOnClickListener {
                onAmigoClick(amigo)
            }
        }
    }
}
