package com.example.nutrihanjum.model

data class NewsDTO(
    var id: String = "",
    var sourceUrl: String = "",
    var title: String = "",
    var source: String = "",
    var imageUrl: String = "",
    var isHead: Boolean = false
)
