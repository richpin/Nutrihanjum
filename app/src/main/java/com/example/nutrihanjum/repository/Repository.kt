package com.example.nutrihanjum.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.model.UserDTO
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.text.SimpleDateFormat
import java.util.*

object Repository {
    private val auth get() = FirebaseAuth.getInstance()
    private val store get() = FirebaseFirestore.getInstance()
    private val storage get() = FirebaseStorage.getInstance()

    private val uid get() = auth.currentUser?.uid

    val userEmail get() = auth.currentUser?.email
    val userName get() = auth.currentUser?.displayName
    val userPhoto get() = auth.currentUser?.photoUrl

    fun eventContents() = callbackFlow {
        val registration =
            store.collection("posts")
                .orderBy("timestamp")
                .whereEqualTo("public", true)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.wtf("Repository", "listen:error", error)
                        return@addSnapshotListener
                    }

                    value?.documentChanges?.forEach {
                        offer(it)
                    }
                }

        awaitClose { registration.remove() }
    }

    fun is_liked(likes: List<String>): Boolean {
        return likes.contains(uid)
    }

    fun eventLikes(contentDTO: ContentDTO) = callbackFlow {
        val registration = store.collection("posts").document(contentDTO.id)

        with(registration) {
            if (contentDTO.likes.contains(uid)) {
                this.update("likes", FieldValue.arrayRemove(uid))
            } else {
                this.update("likes", FieldValue.arrayUnion(uid))
            }
        }.continueWith {
            if (it.isSuccessful) {
                offer(true)
            } else {
                offer(false)
            }
            close()
        }

        awaitClose()
    }




}