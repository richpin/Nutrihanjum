package com.example.nutrihanjum.chatbot.model


data class ChatBotResponseDTO(
    var text: String = "",
    var quickReplies: ArrayList<QuickReplyOption> = arrayListOf(),
)