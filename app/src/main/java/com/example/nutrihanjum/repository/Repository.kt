package com.example.nutrihanjum.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.nutrihanjum.R
import com.example.nutrihanjum.model.ContentDTO
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
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

    fun loadAllDiaryAtDate(date: String) = callbackFlow {
        store.collection("posts")
            .whereEqualTo("date", date)
            .whereEqualTo("uid", uid)
            .get().continueWith {
                if (it.isSuccessful) {
                    it.result.documents.forEach { item ->
                        offer(Pair(item.toObject(ContentDTO::class.java)!!, item.id))
                    }
                }
                else {
                    Log.wtf("Repository", "${it.exception?.message}")
                }
                close()
            }

        awaitClose()
    }

    fun modifyDiaryWithoutPhoto(content: ContentDTO, documentId: String) = callbackFlow {
        store.collection("posts")
            .document(documentId)
            .set(content)
            .continueWith {
                if (it.isSuccessful) {
                    offer(true)
                } else {
                    offer(false)
                }
                close()
            }

        awaitClose()
    }
    fun modifyDiaryWithPhoto(content: ContentDTO, documentId: String, imageUri: String) = callbackFlow {

        storage.getReferenceFromUrl(content.imageUrl)
            .putFile(Uri.parse(imageUri))
            .continueWithTask {
                it.result.storage.downloadUrl
            }
            .continueWithTask {
                if (it.isSuccessful) {
                    content.imageUrl = it.result.toString()

                    store.collection("posts").document(documentId).set(content)
                }
                else {
                    offer(false)
                    close()
                    null
                }
            }
            .continueWith { result ->
                if (result.isSuccessful) {
                    offer(true)
                } else {
                    offer(false)
                }
                close()
            }

        awaitClose()
    }

    fun deleteDiary(documentId: String, imageUrl: String) = callbackFlow {
        storage.getReferenceFromUrl(imageUrl).delete()
            .continueWithTask {
                if (it.isSuccessful) {
                    store.collection("posts").document(documentId).delete()
                }
                else {
                    offer(false)
                    close()
                    null
                }
            }
            .continueWith {
                if (it.isSuccessful) {
                    offer(true)
                } else {
                    offer(false)
                }
                close()
            }

        awaitClose()
    }

    fun addDiary(content: ContentDTO, imageUri: String) = callbackFlow {
        val filename = "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())}.png"
        storage.reference.child("images").child(filename)
            .putFile(Uri.parse(imageUri))
            .continueWithTask {
                if (it.isSuccessful) {
                    it.result.storage.downloadUrl
                } else {
                    null
                }
            }
            .continueWithTask {
                if (it.isSuccessful) {
                    content.imageUrl = it.result.toString()
                    content.uid = uid!!

                    store.collection("posts").document().set(content)
                }
                else {
                    offer(false)
                    close()
                    null
                }
            }
            .continueWith { result ->
                if (result.isSuccessful) {
                    offer(true)
                } else {
                    offer(false)
                }
                close()
            }

        awaitClose()
    }

    fun authWithCredential(credential : AuthCredential) = callbackFlow {
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

    fun signOut(context: Context) = callbackFlow {
        for (provider in auth.currentUser!!.providerData) {
            when (provider.providerId) {
                FirebaseAuthProvider.PROVIDER_ID -> {
                    auth.signOut()
                }
                GoogleAuthProvider.PROVIDER_ID -> {
                    val gso = GoogleSignInOptions
                        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .build()

                    GoogleSignIn.getClient(context, gso).signOut().continueWith {
                        if (it.isSuccessful) {
                            offer(true)
                        }
                        else {
                            offer(false)
                        }
                        close()
                    }
                }
            }
        }

        awaitClose()
    }

    fun isSigned() = auth.currentUser != null
}