package com.example.nutrihanjum.chatbot.model

data class ChatBotRequestDTO(
    private var queryInput: QueryInput = QueryInput()
) {
    fun setMessage(message: String) {
        queryInput.text.text = message
    }
    fun setLanguageCode(code: String) {
        queryInput.text.languageCode = code
    }
}

data class QueryInput (
    var text: QueryInputBody = QueryInputBody()
)

data class QueryInputBody (
    var text: String = "",
    var languageCode: String = "",
)