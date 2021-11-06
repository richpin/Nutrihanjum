package com.example.nutrihanjum.repository

import android.util.Log
import com.example.nutrihanjum.model.NewsDTO
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

object NewsRepository {
    private val store get() = FirebaseFirestore.getInstance()
    private lateinit var lastVisible: DocumentSnapshot
    val boardLimit: Long = 1

    fun loadNewsInit() = callbackFlow {
        store.collection("news")
            .whereEqualTo("isHead", false)
            .limit(boardLimit)
            .get().continueWith {
                if (it.isSuccessful) {
                    it.result.documents.forEach { snapshot ->
                        trySend(snapshot.toObject(NewsDTO::class.java))
                    }
                    lastVisible = it.result.documents[it.result.size() - 1]
                } else {
                    Log.wtf("Repository", it.exception?.message)
                }
                close()
            }

        awaitClose()
    }

    fun loadNewsMore() = callbackFlow {
        store.collection("news")
            .whereEqualTo("isHead", false)
            .startAfter(lastVisible)
            .limit(boardLimit)
            .get().continueWith {
                if (it.isSuccessful) {
                    it.result.documents.forEach { snapshot ->
                        trySend(snapshot.toObject(NewsDTO::class.java))
                    }
                    lastVisible = it.result.documents[it.result.size() - 1]
                } else {
                    Log.wtf("Repository", it.exception?.message)
                }
                close()
            }

        awaitClose()
    }

    fun loadHeadNews() = callbackFlow {
        store.collection("news")
            .whereEqualTo("isHead", true)
            .get().continueWith {
                if(it.isSuccessful) {
                    it.result.documents.forEach { snapshot ->
                        trySend(snapshot.toObject(NewsDTO::class.java))
                    }
                } else {
                    Log.wtf("Repository", it.exception?.message)
                }
                close()
        }

        awaitClose()
    }
}