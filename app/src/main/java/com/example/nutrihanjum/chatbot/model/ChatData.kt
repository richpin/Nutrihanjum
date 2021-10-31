package com.example.nutrihanjum.chatbot.model

interface ChatData {
    companion object {
        val BOT = 0
        val USER = 1
    }
    val type: Int
}

data class BotData(
    var message: String = "",
    override val type: Int = ChatData.BOT,
    var name: String = "",
    var profileUrl: String = ""
): ChatData {

}


data class UserData(
    var message: String = "",
    override val type: Int = ChatData.USER,
    var name: String = "",
    var profileUrl: String = ""
): ChatData {

}