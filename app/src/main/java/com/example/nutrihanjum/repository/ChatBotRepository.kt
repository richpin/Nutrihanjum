package com.example.nutrihanjum.repository

import android.util.Log
import com.example.nutrihanjum.chatbot.model.ChatBotDTO
import com.example.nutrihanjum.chatbot.model.ChatBotRequestDTO
import com.example.nutrihanjum.chatbot.model.ChatBotResponseDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

object ChatBotRepository {
    private val TAG = this.javaClass.simpleName

    private val auth get() = FirebaseAuth.getInstance()
    private val store get() = FirebaseFirestore.getInstance()
    private val storage get() = FirebaseStorage.getInstance()
    private val function = FirebaseFunctions.getInstance("asia-northeast1")

    val uid get() = auth.currentUser?.uid

    val userEmail get() = auth.currentUser?.email
    val userName get() = auth.currentUser?.displayName
    val userPhoto get() = auth.currentUser?.photoUrl


    fun loadAllChatBots() = callbackFlow {
        store.collection("chat_bot").get().continueWith { task ->
            if (task.isSuccessful) {
                trySend(task.result.toObjects(ChatBotDTO::class.java))
            }
            close()
        }

        awaitClose()
    }


    fun initChatBot(chatBot: ChatBotDTO) {
        //function.useEmulator("10.0.2.2", 5001)
    }


    fun sendMessage(message: String, type: String, chatBot: ChatBotDTO) = callbackFlow {
        val data = ChatBotRequestDTO(
            chatBot.id,
            userName!!,
            userEmail!!,
            message,
            type,
            "ko",
        )

        Log.wtf(TAG, data.toString())
        function.getHttpsCallable("detectIntent")
            .call(data.toMap())
            .addOnSuccessListener {
                val responseDTO = Gson().fromJson(it.data.toString(), ChatBotResponseDTO::class.java)

                trySend(responseDTO)
                close()
            }
            .addOnFailureListener {
                Log.wtf(TAG, it.stackTraceToString())
                trySend(null)
                close()
            }

        awaitClose()
    }
}