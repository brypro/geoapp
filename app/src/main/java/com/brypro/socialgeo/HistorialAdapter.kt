package com.brypro.socialgeo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Locale

class HistorialAdapter(
    private val onImageClick: (Foto) -> Unit,
    private val onViewLocationClick: (Foto) -> Unit
) : RecyclerView.Adapter<HistorialAdapter.FotoViewHolder>() {

    private var fotos = listOf<Foto>()

    fun setFotos(fotos: List<Foto>) {
        this.fotos = fotos
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_historial, parent, false)
        return FotoViewHolder(view, onImageClick, onViewLocationClick)
    }

    override fun onBindViewHolder(holder: FotoViewHolder, position: Int) {
        val foto = fotos[position]
        holder.bind(foto)
    }

    override fun getItemCount() = fotos.size

    class FotoViewHolder(
        itemView: View,
        private val onImageClick: (Foto) -> Unit,
        private val onViewLocationClick: (Foto) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val imageViewFoto: ImageView = itemView.findViewById(R.id.imageViewFoto)
        private val textViewCoordenadas: TextView = itemView.findViewById(R.id.textViewCoordenadas)
        private val textViewFecha: TextView = itemView.findViewById(R.id.textViewFecha)
        private val buttonVerUbicacion: Button = itemView.findViewById(R.id.buttonVerUbicacion)

        fun bind(foto: Foto) {
            // Cargar imagen con Glide
            Glide.with(itemView.context).load(foto.url).into(imageViewFoto)

            // Mostrar coordenadas
            textViewCoordenadas.text = "Lat: ${foto.latitud}, Lon: ${foto.longitud}"

            // Mostrar fecha formateada
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            textViewFecha.text = "Fecha: ${sdf.format(foto.fecha)}"

            // Clic en la imagen
            imageViewFoto.setOnClickListener {
                onImageClick(foto)
            }

            // Clic en el botón Ver ubicación
            buttonVerUbicacion.setOnClickListener {
                onViewLocationClick(foto)
            }
        }
    }
}
