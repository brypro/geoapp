package com.brypro.socialgeo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.brypro.socialgeo.ui.theme.SocialgeoTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        setContent {
//            SocialgeoTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
//            }
//        }
        // Verificar si el usuario está autenticado
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // Usuario autenticado, redirigir al HomeActivity
            startActivity(Intent(this, HomeActivity::class.java))
        } else {
            // Usuario no autenticado, redirigir al LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Finalizar la MainActivity para que no se pueda volver atrás
        finish()
    }
}
