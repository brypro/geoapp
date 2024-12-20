package com.brypro.socialgeo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class FotosActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var buttonCapturar: Button
    private var imageCapture: ImageCapture? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fotos)

        previewView = findViewById(R.id.previewView)
        buttonCapturar = findViewById(R.id.buttonCapturar)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        buttonCapturar.setOnClickListener {
            capturarFoto()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = androidx.camera.core.Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e("CameraX", "Error: ${exc.message}", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun capturarFoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = File(
            externalCacheDir,
            SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(this@FotosActivity, "Error al capturar: ${exception.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Toast.makeText(this@FotosActivity, "Foto capturada, subiendo a servidor ...", Toast.LENGTH_SHORT).show()
                    obtenerUbicacionYSubirFoto(photoFile)
                }
            }
        )
    }

    private fun obtenerUbicacionYSubirFoto(photoFile: File) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        finish()
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latitud = location.latitude
                    val longitud = location.longitude

                    // Generar un Uri válido para Firebase Storage
                    val fileUri = androidx.core.content.FileProvider.getUriForFile(
                        this,
                        "${applicationContext.packageName}.provider",
                        photoFile
                    )

                    // Obtener el ID del usuario actual
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId == null) {
                        Toast.makeText(this, "Error: Usuario no autenticado.", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val storageRef = Firebase.storage.reference.child("fotos/${photoFile.name}")
                    val uploadTask = storageRef.putFile(fileUri)

                    uploadTask.addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                            val data = hashMapOf(
                                "url" to uri.toString(),
                                "latitud" to latitud,
                                "longitud" to longitud,
                                "fecha" to System.currentTimeMillis(),
                                "userId" to userId
                            )

                            Firebase.firestore.collection("fotos")
                                .add(data)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Foto guardada con ubicación", Toast.LENGTH_SHORT).show()

                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore", "Error al guardar: ${e.message}", e)
                                }
                        }
                    }.addOnFailureListener { e ->
                        Log.e("FirebaseStorage", "Error al subir archivo: ${e.message}", e)
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "No se pudo obtener la ubicación: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun requestPermissions() {
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.CAMERA] == true &&
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                startCamera()
            } else {
                Toast.makeText(this, "Permisos denegados", Toast.LENGTH_SHORT).show()
            }
        }.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION))
    }
}

