package com.brypro.socialgeo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Referencias de los componentes
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)
        progressBar = findViewById(R.id.progressBar)

        emailInputLayout = findViewById(R.id.emailInputLayout)
        passwordInputLayout = findViewById(R.id.passwordInputLayout)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Acción de iniciar sesión
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (validarCampos(emailInputLayout, email, passwordInputLayout, password)) {
                login(email, password)
            }
        }

        // Acción de registro
        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (validarCampos(emailInputLayout, email, passwordInputLayout, password)) {
                register(email, password)
            }
        }
    }

    /**
     * Valida los campos de correo y contraseña.
     */
    private fun validarCampos(
        emailInputLayout: TextInputLayout,
        email: String,
        passwordInputLayout: TextInputLayout,
        password: String
    ): Boolean {
        var esValido = true

        // Validar correo
        when {
            email.isEmpty() -> {
                emailInputLayout.error = "El correo no puede estar vacío"
                esValido = false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                emailInputLayout.error = "Formato de correo no válido"
                esValido = false
            }
            else -> emailInputLayout.error = null
        }

        // Validar contraseña
        when {
            password.isEmpty() -> {
                passwordInputLayout.error = "La contraseña no puede estar vacía"
                esValido = false
            }
            password.length < 6 -> {
                passwordInputLayout.error = "La contraseña debe tener al menos 6 caracteres"
                esValido = false
            }
            else -> passwordInputLayout.error = null
        }

        return esValido
    }

    /**
     * Inicia sesión con el correo y la contraseña ingresados.
     */
    private fun login(email: String, password: String) {
        progressBar.visibility = View.VISIBLE
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else {
                    mostrarErrorFirebase(task.exception?.message)
                }
            }
    }

    /**
     * Registra un nuevo usuario con el correo y la contraseña ingresados.
     */
    private fun register(email: String, password: String) {
        progressBar.visibility = View.VISIBLE
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        crearUsuarioEnFirestore(user.uid, email)
                    }
                    Toast.makeText(this, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }


    /**
     * Muestra un error específico de Firebase.
     */
    private fun mostrarErrorFirebase(mensaje: String?) {
        val mensajeError = when {
            mensaje?.contains("password is invalid") == true -> "Contraseña incorrecta"
            mensaje?.contains("There is no user record") == true -> "No existe un usuario con ese correo"
            mensaje?.contains("email address is already in use") == true -> "El correo ya está registrado"
            else -> mensaje ?: "Ocurrió un error desconocido"
        }

        Toast.makeText(this, mensajeError, Toast.LENGTH_LONG).show()
    }


    private fun crearUsuarioEnFirestore(uid: String, email: String) {
        val usuario = hashMapOf(
            "email" to email,
            "fechaRegistro" to System.currentTimeMillis()
        )

        Firebase.firestore.collection("usuarios")
            .document(uid) // Usamos el mismo UID de Firebase Auth
            .set(usuario)
            .addOnSuccessListener {
                Toast.makeText(this, "Usuario creado en Firestore", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar el usuario: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
