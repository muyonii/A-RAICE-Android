package com.example.a_raiceandrd.data.repository.api

data class LoginRequest (val email: String, val password: String)

data class ApiUser(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
)

data class LoginResponse(val token: String, val user: ApiUser)