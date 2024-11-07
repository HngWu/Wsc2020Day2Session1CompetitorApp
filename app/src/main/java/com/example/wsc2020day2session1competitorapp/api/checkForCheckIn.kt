package com.example.wsc2020day2session1competitorapp.api

import com.example.wsc2020day2session1competitorapp.models.Competitor
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class checkForCheckIn {
    fun getFunction(competitorId: String): Boolean? {
        val url = URL("http://10.0.2.2:5006/api/Hospitality/checkin/$competitorId")

        try {
            val con = url.openConnection() as HttpURLConnection
            con.requestMethod = "GET"
            con.setRequestProperty("Content-Type", "application/json; utf-8")
            con.setRequestProperty("Accept", "application/json")
            con.connectTimeout = 1000





            val status = con.responseCode
            if (status == 200) {


                return true
            }
            con.disconnect()
        } catch (e: Exception) {
            return false
        }
        return true
    }

}