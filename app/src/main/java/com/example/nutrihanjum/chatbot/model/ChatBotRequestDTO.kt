package com.example.nutrihanjum.chatbot.model

data class ChatBotRequestDTO(
    var projectId: String = "",
    var userName: String = "",
    var userEmail: String = "",
    var query: String = "",
    var type: String = "",
    var languageCode: String = ""
) {
    fun toMap(): Map<String, Any> {

        return hashMapOf(
            "projectId" to projectId,
            "userName" to userName,
            "userEmail" to userEmail,
            "query" to query,
            "type" to type,
            "languageCode" to languageCode,
        )
    }
}