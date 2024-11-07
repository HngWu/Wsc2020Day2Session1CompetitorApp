package com.example.wsc2020day2session1competitorapp.models

import kotlinx.serialization.Serializable

@Serializable
data class UserSession(
    val token: String,
    val role: String,
    val userId: String
)