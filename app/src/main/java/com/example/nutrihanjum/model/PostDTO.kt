package com.example.nutrihanjum.model

import com.google.firebase.Timestamp
import java.io.Serializable

data class PostDTO(
    var title: String = "",
    var timeStamp: Long = 0,
    var imageUrl: String = ""
): Serializable
