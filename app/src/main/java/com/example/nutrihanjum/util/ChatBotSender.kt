package com.example.nutrihanjum.util

import android.util.Log
import com.example.nutrihanjum.chatbot.model.ChatBotRequestDTO
import com.example.nutrihanjum.chatbot.model.ChatBotRequestInfo
import com.example.nutrihanjum.chatbot.model.ChatBotResponseDTO
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

class ChatBotSender: Callback {
    private val client = OkHttpClient()
    private val mediaType = MediaType.parse("application/json; charset=utf-8")
    private val gson = Gson()

    var responseCallback: ((response: ChatBotResponseDTO) -> Unit)? = null
    var failureCallback: (() -> Unit)? = null

    fun sendMessage(message: String, info: ChatBotRequestInfo, id: String) {
        val data = ChatBotRequestDTO()

        data.setMessage(message)
        data.setLanguageCode("ko")

        val body = RequestBody.create(mediaType, gson.toJson(data))
        val request = Request.Builder()
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer ${info.credential!!.accessToken.tokenValue}")
            .addHeader("Accept", "application/json")
            .url("${info.baseUrl}${id}${info.method}")
            .post(body)
            .build()

        client.newCall(request).enqueue(this)
    }

    override fun onResponse(call: Call, response: Response) {
        if (response.isSuccessful) {
            responseCallback?.let {
                it(gson.fromJson(response.body()?.string(), ChatBotResponseDTO::class.java))
            }
        } else {
            failureCallback?.let { it() }
            Log.wtf(this.javaClass.simpleName, response.message())
        }
    }

    override fun onFailure(call: Call, e: IOException) {
        failureCallback?.let { it() }
        Log.wtf(this.javaClass.simpleName, e.message)
    }
}