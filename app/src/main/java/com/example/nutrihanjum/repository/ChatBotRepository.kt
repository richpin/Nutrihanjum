package com.example.nutrihanjum.repository

import android.util.Log
import com.example.nutrihanjum.chatbot.model.ChatBotProfileDTO
import com.example.nutrihanjum.chatbot.model.ChatBotRequestInfo
import com.example.nutrihanjum.util.ChatBotSender
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.io.IOException
import java.lang.Exception

object ChatBotRepository {
    private val TAG = this.javaClass.simpleName

    private val auth get() = FirebaseAuth.getInstance()
    private val store get() = FirebaseFirestore.getInstance()
    private val storage get() = FirebaseStorage.getInstance()

    val uid get() = auth.currentUser?.uid

    val userEmail get() = auth.currentUser?.email
    val userName get() = auth.currentUser?.displayName
    val userPhoto get() = auth.currentUser?.photoUrl


    fun loadAllChatBots() = callbackFlow {
        store.collection("chat_bot").get().continueWith { task ->
            if (task.isSuccessful) {
                trySend(task.result.toObjects(ChatBotProfileDTO::class.java))
            }
            close()
        }

        awaitClose()
    }


    fun initChatBot(id: String, scope: CoroutineScope) = callbackFlow {
        var info = ChatBotRequestInfo()

        store.collection("chat_bot_credentials").document(id).get().onSuccessTask {
            info = it.toObject(ChatBotRequestInfo::class.java)!!
            storage.getReferenceFromUrl(info.credentialUrl!!).stream

        }.addOnSuccessListener {
            scope.launch(Dispatchers.Default) {
                try {
                    info.credential = ServiceAccountCredentials.fromStream(it.stream)
                        .createScoped(info.scope)

                    info.credential?.refreshIfExpired()
                    trySend(info)
                }
                catch (e: Exception) {
                    Log.wtf(TAG, e.stackTraceToString())
                    trySend(null)
                }
                finally {
                    close()
                }
            }
        }.addOnFailureListener {
            trySend(null)
            close()
        }

        awaitClose()
    }


    private val sender = ChatBotSender()

    fun sendMessage(message: String, info: ChatBotRequestInfo) = callbackFlow {
        sender.responseCallback = {
            trySend(it)
            close()
        }

        sender.failureCallback = {
            trySend(null)
            close()
        }

        sender.sendMessage(message, info, uid ?: "none")

        awaitClose()
    }
}