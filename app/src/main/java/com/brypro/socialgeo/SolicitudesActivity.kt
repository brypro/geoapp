package com.brypro.socialgeo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SolicitudesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SolicitudesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solicitudes)

        recyclerView = findViewById(R.id.recyclerViewSolicitudes)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Inicializar el adaptador
        adapter = SolicitudesAdapter { solicitudId, aceptar ->
            responderSolicitudAmistad(solicitudId, aceptar)
        }
        recyclerView.adapter = adapter

        // Cargar las solicitudes pendientes
        cargarSolicitudes()
    }

    private fun cargarSolicitudes() {
        val usuarioId = FirebaseAuth.getInstance().currentUser!!.uid
        val solicitudes : MutableList<Solicitud> = mutableListOf()
        Firebase.firestore.collection("amistades")
            .whereEqualTo("usuarioB", usuarioId)
            .whereEqualTo("status", "pendiente")
            .get()
            .addOnSuccessListener { documents ->
                documents.map { r ->
                    Toast.makeText(this, "${r.getString("usuarioA")}", Toast.LENGTH_SHORT).show()
                    r.getString("usuarioA")?.let {
                        Firebase.firestore.collection("usuarios")
                            .document(it)
                            .get()
                            .addOnSuccessListener { result ->
                                val solicitud = Solicitud(
                                    id = r.id,
                                    usuarioA = result.getString("email") ?: ""
                                )
                                solicitudes.add(solicitud)
                                this.adapter.setSolicitudes(solicitudes)
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error al cargar usuarios: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar solicitudes: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun responderSolicitudAmistad(solicitudId: String, aceptar: Boolean) {
        val nuevoEstado = if (aceptar) "aceptado" else "rechazado"

        Firebase.firestore.collection("amistades").document(solicitudId)
            .update("status", nuevoEstado)
            .addOnSuccessListener {
                val mensaje = if (aceptar) "Solicitud aceptada" else "Solicitud rechazada"
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
                cargarSolicitudes() // Recargar la lista
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al responder: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
