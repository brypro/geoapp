package com.brypro.socialgeo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HistorialActivity : AppCompatActivity() {

    private lateinit var recyclerViewHistorial: RecyclerView
    private lateinit var adapter: HistorialAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        recyclerViewHistorial = findViewById(R.id.recyclerViewHistorial)

        // Configurar RecyclerView
        recyclerViewHistorial.layoutManager = LinearLayoutManager(this)
        adapter = HistorialAdapter(
            onImageClick = { foto -> mostrarFotoEnPantallaCompleta(foto) },
            onViewLocationClick = { foto -> abrirMapaConUbicacion(foto) }
        )
        recyclerViewHistorial.adapter = adapter

        // Cargar datos desde Firestore
        cargarHistorial()
    }

    private fun cargarHistorial() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Error: Usuario no autenticado.", Toast.LENGTH_SHORT).show()
            return
        }

        Firebase.firestore.collection("fotos")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val fotos = documents.map { doc ->
                    Foto(
                        url = doc.getString("url") ?: "",
                        latitud = doc.getDouble("latitud") ?: 0.0,
                        longitud = doc.getDouble("longitud") ?: 0.0,
                        fecha = doc.getLong("fecha") ?: 0L
                    )
                }
                adapter.setFotos(fotos)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al cargar el historial: ${e.message}", e)
                Toast.makeText(this, "Error al cargar el historial.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarFotoEnPantallaCompleta(foto: Foto) {
        val intent = Intent(this, FotoCompletaActivity::class.java)
        intent.putExtra("foto_url", foto.url)
        startActivity(intent)
    }

    private fun abrirMapaConUbicacion(foto: Foto) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("amigo_latitud", foto.latitud)
        intent.putExtra("amigo_longitud", foto.longitud)
        intent.putExtra("amigo_email", "lugar de captura")
        startActivity(intent)
    }
}
