package com.example.nutrihanjum.chatbot.model

import java.io.Serializable

data class ChatBotDTO(
    var id: String = "",
    var profileName: String = "",
    var profileUrl: String = "",
    var category: String = "",
    var content: String = "",
    var fallback: String = ""
) : Serializable
