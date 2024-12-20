package com.brypro.socialgeo

data class Usuario(
    val id: String,
    val email: String
)

data class Amigo(
    val id: String,
    val email: String,
    val latitud: Double,
    val longitud: Double
)

data class Solicitud(
    val id: String,
    val usuarioA: String
)

data class Foto(
    val url: String,
    val latitud: Double,
    val longitud: Double,
    val fecha: Long
)
