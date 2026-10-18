package com.example.a_raiceandrd.ui.auth

import android.content.Context
import com.example.a_raiceandrd.data.repository.api.ApiClient
import com.example.a_raiceandrd.data.repository.api.LoginRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object FLogin {

    private var currentRole: Role? = null
    private var currentEmail: String? = null
    private var currentName: String? = null

    // The session lives in memory for now, so you log in again each time the app starts.
    fun init(context: Context) { }

    fun getCurrentRole(): Role? = currentRole
    fun getCurrentEmail(): String? = currentEmail
    fun getCurrentName(): String? = currentName

    // Calls POST /api/login. Throws if the login fails.
    suspend fun login(email: String, password: String): Role {
        val response = ApiClient.service.login(LoginRequest(email, password))

        val role = when (response.user.role) {
            "farmer" -> Role.FARMER
            "lgu_staff", "admin" -> Role.ADMIN
            else -> throw IllegalStateException("This account type can't use the app")
        }

        ApiClient.token = response.token
        currentRole = role
        currentEmail = response.user.email
        currentName = response.user.name
        return role
    }

    fun logout() {
        val token = ApiClient.token
        ApiClient.token = null
        currentRole = null
        currentEmail = null
        currentName = null

        // Tell the server to delete the token (best effort)
        if (token != null) {
            CoroutineScope(Dispatchers.IO).launch {
                runCatching { ApiClient.service.logout("Bearer $token") }
            }
        }
    }
}