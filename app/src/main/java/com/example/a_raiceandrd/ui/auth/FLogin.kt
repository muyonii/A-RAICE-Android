package com.example.a_raiceandrd.ui.auth

import android.content.Context
import android.content.SharedPreferences
import com.example.a_raiceandrd.data.repository.api.ApiClient
import com.example.a_raiceandrd.data.repository.api.LoginRequest
import com.example.a_raiceandrd.data.repository.api.UserDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object FLogin {

    private const val PREFS_NAME = "araice_auth_prefs"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_ROLE = "user_role"
    private const val KEY_EMAIL = "user_email"
    private const val KEY_NAME = "user_name"

    private var prefs: SharedPreferences? = null

    private var currentRole: Role? = null
    private var currentEmail: String? = null
    private var currentName: String? = null

    fun init(context: Context) {
        val sp = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs = sp

        val token = sp.getString(KEY_TOKEN, null)
        val roleStr = sp.getString(KEY_ROLE, null)
        val email = sp.getString(KEY_EMAIL, null)
        val name = sp.getString(KEY_NAME, null)

        if (!token.isNullOrBlank()) {
            ApiClient.token = token
            currentRole = roleStr?.let { runCatching { Role.valueOf(it) }.getOrNull() }
            currentEmail = email
            currentName = name
            ApiClient.userName = name
        }
    }

    fun hasSession(): Boolean = !ApiClient.token.isNullOrBlank() && currentRole != null

    fun getCurrentRole(): Role? = currentRole
    fun getCurrentEmail(): String? = currentEmail
    fun getCurrentName(): String? = currentName

    fun saveSession(token: String?, user: UserDto?) {
        val role = mapRole(user?.role)
        ApiClient.token = token
        currentRole = role
        currentEmail = user?.email
        currentName = user?.name
        ApiClient.userName = user?.name

        prefs?.edit()
            ?.putString(KEY_TOKEN, token)
            ?.putString(KEY_ROLE, role?.name)
            ?.putString(KEY_EMAIL, user?.email)
            ?.putString(KEY_NAME, user?.name)
            ?.apply()
    }

    fun updateCurrentUser(user: UserDto?) {
        if (user == null) return
        currentEmail = user.email
        currentName = user.name
        ApiClient.userName = user.name
        prefs?.edit()
            ?.putString(KEY_EMAIL, user.email)
            ?.putString(KEY_NAME, user.name)
            ?.apply()
    }

    // Calls POST /api/login. Throws if the login fails.
    suspend fun login(email: String, password: String): Role {
        val response = ApiClient.service.login(LoginRequest(email, password))
        val role = mapRole(response.user?.role)
            ?: throw IllegalStateException("This account type can't use the app")

        saveSession(response.token, response.user)
        return role
    }

    fun clearSessionLocally() {
        ApiClient.token = null
        ApiClient.userName = null
        currentRole = null
        currentEmail = null
        currentName = null

        prefs?.edit()?.clear()?.apply()
    }

    fun logout() {
        val token = ApiClient.token
        clearSessionLocally()

        // Tell the server to delete the token (best effort)
        if (token != null) {
            CoroutineScope(Dispatchers.IO).launch {
                runCatching { ApiClient.service.logout() }
            }
        }
    }

    fun mapRole(roleString: String?): Role? = when (roleString?.lowercase()?.trim()) {
        "farmer" -> Role.FARMER
        "lgu_staff", "admin" -> Role.ADMIN
        else -> null
    }
}