package com.example.wsc2020day2session1competitorapp.api

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.example.wsc2020day2session1competitorapp.models.SessionManager
import com.example.wsc2020day2session1competitorapp.models.User
import com.example.wsc2020day2session1competitorapp.models.UserSession
import kotlinx.serialization.encodeToString
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.net.URL

class login {
    fun postFunction(
        user: User,
        context: Context,
        onSuccess: (Boolean) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val url = URL("http://10.0.2.2:5006/api/Hospitality/login")

        try {
            val con = url.openConnection() as HttpURLConnection
            con.requestMethod = "POST"
            con.setRequestProperty("Content-Type", "application/json; utf-8")
            con.setRequestProperty("Accept", "application/json")
            con.doOutput = true

            val json = Json.encodeToString(user)
            val os = OutputStreamWriter(con.outputStream)

            os.write(json)
            os.flush()
            os.close()

            val status = con.responseCode
            if (status == 200) {


                val response = con.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)

                val session = UserSession(
                    token = jsonResponse.getString("token"),
                    role = jsonResponse.getString("role"),
                    userId = jsonResponse.getString("userId")
                )

                if (session.token == null || session.role == null || session.userId == null) {
                    throw Exception("Invalid session")
                }

                if (session.token.isEmpty() || session.role.isEmpty() || session.userId.isEmpty()) {
                    throw Exception("Invalid session")
                }

                if (session.role == "competitor") {
                    val sessionManager = SessionManager(context)
                    sessionManager.saveSession(session)
                    Handler(Looper.getMainLooper()).post() {
                        onSuccess(true)
                    }
                }else{
                    Handler(Looper.getMainLooper()).post() {
                        onFailure(Throwable("Post asset failed"))
                    }
                }

            } else {
                Handler(Looper.getMainLooper()).post {
                    onFailure(Throwable("Post asset failed"))
                }
            }
        } catch (e: Exception) {
            onFailure(e)
        }
    }


}