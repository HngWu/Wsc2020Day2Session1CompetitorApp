package com.example.wsc2020day2session1competitorapp.models

import kotlinx.serialization.Serializable


@Serializable
data class User(
    val email: String,
    val password: String
)