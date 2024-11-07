package com.example.wsc2020day2session1competitorapp.models

import kotlinx.serialization.Serializable

@Serializable
class Announcement (
    var id: Int,
    var announcementDate: String ,
    var announcementTitle: String ,
    var announcementDescription: String
)