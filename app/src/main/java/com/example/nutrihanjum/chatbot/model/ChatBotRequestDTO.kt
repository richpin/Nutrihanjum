package com.example.nutrihanjum.chatbot.model

data class ChatBotRequestDTO(
    var botId: String = "",
    var query: String = "",
    var type: String = ""
) {
    fun toMap(): Map<String, Any> {

        return hashMapOf(
            "botId" to botId,
            "query" to query,
            "type" to type
        )
    }
}