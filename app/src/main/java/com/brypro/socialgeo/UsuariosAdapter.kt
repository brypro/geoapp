package com.brypro.socialgeo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UsuariosAdapter(
    private val onUsuarioClick: (Usuario) -> Unit
) : RecyclerView.Adapter<UsuariosAdapter.UsuarioViewHolder>() {

    private val usuarios = mutableListOf<Usuario>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_usuario, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = usuarios[position]
        holder.bind(usuario)
    }

    override fun getItemCount(): Int = usuarios.size

    fun setUsuarios(nuevosUsuarios: List<Usuario>) {
        usuarios.clear()
        usuarios.addAll(nuevosUsuarios)
        notifyDataSetChanged()
    }

    inner class UsuarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewNombre: TextView = itemView.findViewById(R.id.textViewNombre)
        private val buttonSolicitar: Button = itemView.findViewById(R.id.buttonSolicitar)

        fun bind(usuario: Usuario) {
            textViewNombre.text = usuario.email
            buttonSolicitar.setOnClickListener {
                onUsuarioClick(usuario)
            }
        }
    }
}
