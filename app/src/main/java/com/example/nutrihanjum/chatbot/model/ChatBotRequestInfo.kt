package com.example.nutrihanjum.chatbot.model

import com.google.auth.oauth2.GoogleCredentials

data class ChatBotRequestInfo(
    var baseUrl: String? = null,
    var method: String? = null,
    var credentialUrl: String? = null,
    var scope: List<String>? = null,
    var welcome: ChatBotResponseDTO? = null,
    var credential: GoogleCredentials? = null,
)
