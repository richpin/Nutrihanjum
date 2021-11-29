package com.example.nutrihanjum.model

import java.io.Serializable

data class UserDTO(
    var email: String = "",
    var name: String = "",
    var userID: String = "",
    var profileUrl: String = "",
    var token: String = "",
    var getNotice: Boolean = true,
    var saved: List<String> = listOf(),
    var posts: List<String> = listOf()
) : Serializable {
    data class NoticeDTO(
        var uid: String = "",
        var senderName: String = "",
        var contentId: String = "",
        var contentUrl: String = "",
        var content:String = "",
        var kind: Int = -1,
        var timestamp: Long = 0
    )
}
