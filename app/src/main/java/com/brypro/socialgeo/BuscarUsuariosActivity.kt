package com.brypro.socialgeo

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class BuscarUsuariosActivity : AppCompatActivity() {

    private lateinit var editTextBuscar: EditText
    private lateinit var buttonBuscar: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UsuariosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buscar_usuarios)

        editTextBuscar = findViewById(R.id.editTextBuscar)
        buttonBuscar = findViewById(R.id.buttonBuscar)
        recyclerView = findViewById(R.id.recyclerViewUsuarios)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = UsuariosAdapter { usuario ->
            enviarSolicitudAmistad(usuario.id)
        }
        recyclerView.adapter = adapter

        buttonBuscar.setOnClickListener {
            val email = editTextBuscar.text.toString()
            if (email.isNotEmpty()) {
                buscarUsuarios(email) { usuarios ->
                    adapter.setUsuarios(usuarios)
                }
            } else {
                Toast.makeText(this, "Ingresa un nombre para buscar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun enviarSolicitudAmistad(amigoId: String) {
        val usuarioId = FirebaseAuth.getInstance().currentUser!!.uid
        val solicitud = hashMapOf(
            "usuarioA" to usuarioId,
            "usuarioB" to amigoId,
            "status" to "pendiente",
            "fecha" to System.currentTimeMillis()
        )

        Firebase.firestore.collection("amistades")
            .add(solicitud)
            .addOnSuccessListener {
                Toast.makeText(this, "Solicitud enviada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun buscarUsuarios(email: String, callback: (List<Usuario>) -> Unit) {
        Firebase.firestore.collection("usuarios")
            .whereGreaterThanOrEqualTo("email", email)
            .whereLessThanOrEqualTo("email", email + '\uf8ff')
            .get()
            .addOnSuccessListener { documents ->
                val usuarios = documents.map { document ->
                    Usuario(
                        id = document.id,
                        email = document.getString("email") ?: ""
                    )
                }
                callback(usuarios)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al buscar usuarios: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
