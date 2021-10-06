package com.example.nutrihanjum.repository

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

object Repository {
    private val auth get() = FirebaseAuth.getInstance()
    private val store get() = FirebaseFirestore.getInstance()
    private val storage get() = FirebaseStorage.getInstance()

    val userID get() = auth.currentUser?.uid
    val userEmail get() = auth.currentUser?.email

    @ExperimentalCoroutinesApi
    fun authWithGoogle(credential : AuthCredential) = callbackFlow {
        auth.signInWithCredential(credential).continueWith { result ->
            if (result.isSuccessful) {
                offer(Pair(true, ""))
            }
            else {
                offer(Pair(false, result.exception?.message))
            }
            close()
        }

        awaitClose()
    }

    fun signOut() {
        auth.signOut()
    }
}