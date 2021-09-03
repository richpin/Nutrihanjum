package com.example.nutrihanjum.model

data class ContentDTO(
    var explain : String? = null,
    var imageUrl : String? = null,
    var uid : String? = null,
    var userId : String? = null,
    var timeStamp : Long? = null,
    var favoriteCount : Int = 0,
    var favorties : MutableMap<String,Boolean> = HashMap())
    {
    data class Comment(
        var uid: String? = null,
        var userId: String? = null,
        var comment: String? = null,
        var timeStamp: Long? = null
    )
}
