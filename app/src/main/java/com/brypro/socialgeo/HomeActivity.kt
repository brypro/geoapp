package com.brypro.socialgeo
import android.Manifest

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.CircleLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager

class HomeActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var btnLogout: View
    private lateinit var btnShareLocation: FloatingActionButton
    private lateinit var btnCamera: FloatingActionButton
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var btnUpdateLocations: FloatingActionButton
    private lateinit var btnRequests: FloatingActionButton



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        // Inicializar MapTiler
        mapView = findViewById(R.id.mapView)
        mapView.getMapboxMap().loadStyleUri("https://api.maptiler.com/maps/openstreetmap/style.json?key=K6PKHn4V7ixZYsDwdZOu"){
            //obtenerUbicacionYPosicionarCamara()
        }

        // Obtener las coordenadas desde el intent
        val amigoLatitud = intent.getDoubleExtra("amigo_latitud", 0.0)
        val amigoLongitud = intent.getDoubleExtra("amigo_longitud", 0.0)
        val amigoEmail = intent.getStringExtra("amigo_email")

        // Verificar si las coordenadas son válidas (no son 0.0)

        if (amigoLatitud != 0.0 && amigoLongitud != 0.0) {
            centrarCamaraEnUbicacion( Location("").apply {
                latitude = amigoLatitud
                longitude = amigoLongitud
            })
            agregarMarcador(amigoLatitud, amigoLongitud, amigoEmail ?: "Amigo")
            Toast.makeText(this, "Amigo en ${amigoLatitud}, ${amigoLongitud}", Toast.LENGTH_SHORT).show()
        }

        // Inicializar el cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Referencias de botones
        btnLogout = findViewById(R.id.btnLogout)
        btnShareLocation = findViewById(R.id.btnShareLocation)
        btnCamera = findViewById(R.id.btnCamera)
        btnUpdateLocations = findViewById(R.id.btnUpdateLocations)
        btnRequests = findViewById(R.id.btnRequests)

        // Acción de botón cerrar sesión
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))

            finish()
        }

        // Acción de botón compartir ubicación
        btnShareLocation.setOnClickListener {
            Toast.makeText(this, "Ubicación compartida", Toast.LENGTH_SHORT).show()
            // Aquí iría la lógica para subir la ubicación a Firestore
            actualizarCoordenadasEnFirestore()
        }

        // Acción de botón cámara
        btnCamera.setOnClickListener {
            Toast.makeText(this, "Abriendo cámara...", Toast.LENGTH_SHORT).show()
            // Aquí se implementaría la funcionalidad de tomar fotos
            val intent = Intent(this, FotosActivity::class.java)
            startActivity(intent)
        }

        // Acción de botón actualizar ubicaciones
        btnUpdateLocations.setOnClickListener {
            Toast.makeText(this, "Actualizando ubicaciones...", Toast.LENGTH_SHORT).show()
            // Aquí se implementaría la funcionalidad de actualizar las ubicaciones de los amigos
            mostrarUbicacionesDeAmigos()
        }

        btnRequests.setOnClickListener {
            val intent = Intent(this, BuscarUsuariosActivity::class.java)
            //val intent = Intent(this, SolicitudesActivity::class.java)

            startActivity(intent)
        }


        // Configurar el BottomNavigationView
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_friends -> {
                    Toast.makeText(this, "Amigos seleccionados", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, AmigosActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_history -> {
                    Toast.makeText(this, "Historial seleccionado", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HistorialActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_settings -> {
                    Toast.makeText(this, "Configuración seleccionada", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Obtiene la ubicación actual del usuario y mueve la cámara a su ubicación.
     */
    private fun obtenerUbicacionYPosicionarCamara() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            solicitarPermisosDeUbicacion()
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    Toast.makeText(this, "Ubicación encontrada ${location.latitude}, ${location.longitude}", Toast.LENGTH_SHORT).show()
                    centrarCamaraEnUbicacion(location)
                    agregarMarcador(location.latitude, location.longitude, "Tú")

                } else {
                    Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al obtener la ubicación: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Mueve la cámara a la ubicación del usuario.
     */
    private fun centrarCamaraEnUbicacion(location: Location) {
        val cameraOptions = CameraOptions.Builder()
            .center(com.mapbox.geojson.Point.fromLngLat(location.longitude, location.latitude))
            .zoom(15.0) // Nivel de zoom (puedes ajustarlo a tu gusto)
            .build()
        mapView.getMapboxMap().setCamera(cameraOptions)
    }

    /**
     * Solicita permisos de ubicación al usuario.
     */
    private fun solicitarPermisosDeUbicacion() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            PERMISOS_UBICACION
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISOS_UBICACION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacionYPosicionarCamara()
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }
//    override fun onDestroy() {
//        super.onDestroy()
//        mapView.onDestroy()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        mapView.onResume()
//    }
//
//    override fun onPause() {
//        super.onPause()
//        mapView.onPause()
//    }

    companion object {
        private const val PERMISOS_UBICACION = 1001
    }

    private fun actualizarCoordenadasEnFirestore() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            solicitarPermisosDeUbicacion()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val coordenadas = hashMapOf(
                    "usuarioId" to FirebaseAuth.getInstance().currentUser!!.uid,
                    "nombre" to FirebaseAuth.getInstance().currentUser!!.email,
                    "latitud" to location.latitude,
                    "longitud" to location.longitude,
                    "timestamp" to System.currentTimeMillis()
                )
                Firebase.firestore.collection("coordenadas")
                    .document(FirebaseAuth.getInstance().currentUser!!.uid)
                    .set(coordenadas)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Ubicación compartida", Toast.LENGTH_SHORT).show()
                        centrarCamaraEnUbicacion(location)
                        agregarMarcador(location.latitude, location.longitude, "Tú")
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Error al actualizar coordenadas: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }

    }

    private fun mostrarUbicacionesDeAmigos() {
        Firebase.firestore.collection("coordenadas").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if(document.id == FirebaseAuth.getInstance().currentUser!!.uid) continue
                    val latitud = document.getDouble("latitud") ?: continue
                    val longitud = document.getDouble("longitud") ?: continue
                    val nombreUsuario = document.getString("nombre") ?: "Amigo"

                    // Agregar marcador en el mapa
                    agregarMarcador(latitud, longitud, nombreUsuario)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar ubicaciones: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun agregarMarcador(lat: Double, lng: Double, titulo: String) {
        // Crear el PointAnnotationManager
        val annotationApi = mapView.annotations
        val pointAnnotationManager = annotationApi.createPointAnnotationManager()

        // Crear un marcador (anotación) con las coordenadas y el título
        val marcador = com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions()
            .withPoint(com.mapbox.geojson.Point.fromLngLat(lng, lat))
            .withTextField(titulo) // Título que se muestra al usuario

        // Agregar el marcador al mapa
        pointAnnotationManager.create(marcador)



    }


}
