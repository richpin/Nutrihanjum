package com.example.nutrihanjum.model

data class ContentDTO(
    var content : String? = null,
    var imageUrl : String? = null,
    var profileUrl : String? = null,
    var uid : String? = null,
    var userId : String? = null,
    var timeStamp : Long? = null,
    var like_count : Int? = 0,
    var comment_count : Int? = 0){
}
