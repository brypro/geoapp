package com.brypro.socialgeo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AmigosActivity : AppCompatActivity() {

    private lateinit var buttonVerSolicitudes: Button
    private lateinit var recyclerViewAmigos: RecyclerView
    private lateinit var adapter: AmigosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_amigos)

        buttonVerSolicitudes = findViewById(R.id.buttonVerSolicitudes)
        recyclerViewAmigos = findViewById(R.id.recyclerViewAmigos)

        // Configurar el RecyclerView
        recyclerViewAmigos.layoutManager = LinearLayoutManager(this)
        adapter = AmigosAdapter { amigo ->
            // Acción al presionar "Ver"
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("amigo_latitud", amigo.latitud)
            intent.putExtra("amigo_longitud", amigo.longitud)
            intent.putExtra("amigo_email", amigo.email)
            startActivity(intent)
        }
        recyclerViewAmigos.adapter = adapter

        // Botón para ver solicitudes pendientes
        buttonVerSolicitudes.setOnClickListener {
            startActivity(Intent(this, SolicitudesActivity::class.java))
        }

        // Cargar lista de amigos
        cargarAmigos()
    }

    private fun cargarAmigos() {
        val usuarioId = FirebaseAuth.getInstance().currentUser!!.uid
        val idsAmigos = mutableListOf<String>()

        // Obtener los amigos donde el usuario actual es "usuarioA"
        Firebase.firestore.collection("amistades")
            .whereEqualTo("usuarioA", usuarioId)
            .whereEqualTo("status", "aceptado")
            .get()
            .addOnSuccessListener { documents ->
                val idsUsuarioA = documents.mapNotNull { it.getString("usuarioB") }
                idsAmigos.addAll(idsUsuarioA)

                // Obtener los amigos donde el usuario actual es "usuarioB"
                Firebase.firestore.collection("amistades")
                    .whereEqualTo("usuarioB", usuarioId)
                    .whereEqualTo("status", "aceptado")
                    .get()
                    .addOnSuccessListener { docs ->
                        val idsUsuarioB = docs.mapNotNull { it.getString("usuarioA") }
                        idsAmigos.addAll(idsUsuarioB)

                        // Eliminar duplicados
                        val idsUnicos = idsAmigos.distinct()

                        if (idsUnicos.isNotEmpty()) {
                            // Obtener la información de los amigos desde la colección "usuarios"
                            Firebase.firestore.collection("coordenadas")
                                .whereIn(FieldPath.documentId(), idsUnicos)
                                .get()
                                .addOnSuccessListener { userDocs ->
                                    val amigos = userDocs.map { doc ->
                                        Amigo(
                                            id = doc.id,
                                            email = doc.getString("nombre") ?: "",
                                            latitud = doc.getDouble("latitud") ?: 0.0,
                                            longitud = doc.getDouble("longitud") ?: 0.0
                                        )

                                    }
                                    adapter.setAmigos(amigos)
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error al cargar usuarios: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            adapter.setAmigos(emptyList())
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al cargar amigos: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar amigos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}