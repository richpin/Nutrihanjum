package com.example.nutrihanjum.chatbot.model

data class ChatBotResponseDTO(
    var queryResult: QueryResult = QueryResult()
) {
    fun getMessage() = queryResult.fulfillmentText
    fun getQuickReplies() = queryResult.fulfillmentMessages[1].payload.quickReplies
}


data class QueryResult(
    var fulfillmentText: String = "",
    var fulfillmentMessages: List<BotResponse> = listOf(),
    var intent: BotIntent = BotIntent(),
    var outputContexts: List<BotContext> = listOf(),
)

data class BotResponse(
    var text: BotText = BotText(),
    var payload: Payload = Payload()
)

data class BotText(
    var text: List<String> = listOf()
)

data class Payload(
    var quickReplies: List<QuickReply> = listOf()
)

data class QuickReply(
    var action: String = "",
    var text: String = ""
)

data class BotIntent(
    var name: String = "",
    var displayName: String = "",
)

data class BotContext(
    var name: String = "",
    var lifespanCount: Int = 0,
)
