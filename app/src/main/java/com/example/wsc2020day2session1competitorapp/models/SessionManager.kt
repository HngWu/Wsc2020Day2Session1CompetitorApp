package com.example.wsc2020day2session1competitorapp.models

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)

    fun saveSession(session: UserSession) {
        sharedPreferences.edit().apply {
            putString("token", session.token)
            putString("role", session.role)
            putString("userId", session.userId)
            apply()
        }
    }

    fun getSession(): UserSession? {
        val token = sharedPreferences.getString("token", null)
        val role = sharedPreferences.getString("role", null)
        val userId = sharedPreferences.getString("userId", null)

        return if (token != null && role != null && userId != null) {
            UserSession(token, role, userId)
        } else null
    }

    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }
}