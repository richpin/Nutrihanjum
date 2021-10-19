package com.example.nutrihanjum.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.nutrihanjum.model.ContentDTO
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
    val userID get() = auth.currentUser?.displayName
    val userPhoto get() = auth.currentUser?.photoUrl

    fun loadContents() = callbackFlow {
        store.collection("posts")
            .orderBy("timestamp")
            .whereEqualTo("public", true)
            .get().continueWith {
                if (it.isSuccessful) {
                    it.result.documents.forEach {
                        offer(it.toObject(ContentDTO::class.java))
                    }
                } else {
                    Log.wtf("Repository", it.exception?.message)
                }
                close()
            }

        awaitClose()
    }

    fun eventLikes(contentDTO: ContentDTO, isLiked: Boolean) = callbackFlow {
        val registration = store.collection("posts").document(contentDTO.id)

        with(registration) {
            if (isLiked) {
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

    fun eventSaved(contentDTO: ContentDTO, isSaved: Boolean) = callbackFlow {
        val postRegistration = store.collection("posts").document(contentDTO.id)
        val userRegistration = store.collection("users").document(uid!!)

        if (isSaved) {
            postRegistration.update("saved", FieldValue.arrayRemove(uid)).continueWithTask {
                if (it.isSuccessful) {
                    userRegistration.update("saved", FieldValue.arrayRemove(contentDTO.id))
                } else {
                    offer(false)
                    close()
                    null
                }
            }.continueWith {
                if (it.isSuccessful) offer(true)
                else offer(false)
            }
        } else {
            postRegistration.update("saved", FieldValue.arrayUnion(uid)).continueWithTask {
                if (it.isSuccessful) {
                    userRegistration.update("saved", FieldValue.arrayUnion(contentDTO.id))
                } else {
                    offer(false)
                    close()
                    null
                }
            }.continueWith {
                if (it.isSuccessful) offer(true)
                else offer(false)
            }
        }

        awaitClose()
    }
}