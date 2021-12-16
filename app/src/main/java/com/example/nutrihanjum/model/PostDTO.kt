package com.example.nutrihanjum.model

import java.io.Serializable

data class PostDTO(
    var title: String = "",
    var timestamp: Long = 0,
    var imageUrl: String = ""
): Serializable
