package com.example.nutrihanjum.model

data class NewsDTO(
    var sourceUrl: String = "",
    var title: String = "",
    var source: String = "",
    var explain: String = "",
    var imageUrl: String = "",
    var isHead: Boolean = false
)
