package com.example.nutrihanjum.model

import java.io.Serializable

data class ContentDTO(
    var id: String = "",
    var content: String = "",
    var imageUrl: String = "",
    var uid: String = "",
    var profileName: String = "",
    var profileUrl: String = "",
    var timestamp: Long = 0,
    var date: String = "",
    var likes: MutableList<String> = mutableListOf(),
    var commentCount: Int = 0,
    var saved: MutableList<String> = mutableListOf(),
    var mealTime: String = "",
    var isPublic: Boolean = true,
) : Serializable {
    data class CommentDTO(
        var id: String = "",
        var uid: String = "",
        var comment: String = "",
        var timeStamp: Long = 0
    )
}
