package com.example.wsc2020day2session1competitorapp.models

import kotlinx.serialization.Serializable

@Serializable
class Competitor {
    var id: String = ""
    var userTypeId: Int = 0
    var fullName: String = ""
    var email: String = ""
    var password: String = ""
}