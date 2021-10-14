package com.example.nutrihanjum.model

import java.io.Serializable

data class ContentDTO(
    var content: String = "",
    var imageUrl: String = "",
    var uid: String = "",
    var timestamp: Long = 0,
    var date: String = "",
    var likes: MutableMap<String, Boolean> = HashMap(),
    var mealTime: String = "",
    var isPublic: Boolean = true,
    ): Serializable{
    data class Comment(var uid : String = "",
                       var comment : String = "",
                       var timeStamp: Long = 0)
}
