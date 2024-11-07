package com.example.wsc2020day2session1competitorapp.api

import com.example.wsc2020day2session1competitorapp.models.Announcement
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class getAnnouncement {
    fun getFunction(): MutableList<Announcement>? {
        val url = URL("http://10.0.2.2:5006/api/Hospitality/announcements")

        try {
            val con = url.openConnection() as HttpURLConnection
            con.requestMethod = "GET"
            con.setRequestProperty("Content-Type", "application/json; utf-8")
            con.setRequestProperty("Accept", "application/json")
            con.connectTimeout = 1000

            val status = con.responseCode
            if (status == 200) {
                val reader = BufferedReader(InputStreamReader(con.inputStream))
                val jsonData = reader.use { it.readText() }
                reader.close()

                val objectList = Json.decodeFromString(
                    ListSerializer(Announcement.serializer()),
                    jsonData
                ) as MutableList<Announcement>

                return objectList
            }
            con.disconnect()
        } catch (e: Exception) {
            return null
        }
        return null
    }

}