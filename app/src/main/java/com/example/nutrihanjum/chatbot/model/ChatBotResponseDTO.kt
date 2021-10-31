package com.example.nutrihanjum.chatbot.model

data class ChatBotResponseDTO(
    var queryResult: QueryResult = QueryResult()
) {
    fun getReplyMessage() = queryResult.fulfillmentText
}


data class QueryResult(
    var fulfillmentText: String = ""
)