package com.brypro.socialgeo

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class FotoCompletaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_foto_completa)

        // Referencia al ImageView
        val imageViewFotoCompleta: ImageView = findViewById(R.id.imageViewFotoCompleta)

        // Obtener la URL de la foto desde el intent
        val fotoUrl = intent.getStringExtra("foto_url")
        if (fotoUrl != null) {
            // Cargar la imagen con Glide
            Glide.with(this)
                .load(fotoUrl)
                .into(imageViewFotoCompleta)
        } else {
            // Manejar el caso de que la URL no esté disponible
            finish() // Finalizar la actividad si no hay URL
        }
    }
}
