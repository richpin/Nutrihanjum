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
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firestore.v1.ArrayValue
import com.google.firestore.v1.MapValue
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

    fun loadAllDiaryAtDate(date: String) = callbackFlow {
        store.collection("posts")
            .whereEqualTo("date", date)
            .whereEqualTo("uid", uid)
            .get().continueWith {
                if (it.isSuccessful) {
                    it.result.documents.forEach { item ->
                        offer(item.toObject(ContentDTO::class.java)!!)
                    }
                } else {
                    Log.wtf("Repository", "${it.exception?.message}")
                }
                close()
            }

        awaitClose()
    }

    fun eventContents() = callbackFlow {
        val registration =
            store.collection("posts").orderBy("timestamp", Query.Direction.DESCENDING)
                .whereEqualTo("public", true)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.w("Repository", "listen:error", error)
                        return@addSnapshotListener
                    }

                    value?.documentChanges?.forEach {
                        offer(it)
                    }
                }

        awaitClose { registration.remove() }
    }

    fun modifyDiaryWithoutPhoto(content: ContentDTO) = callbackFlow {
        store.collection("posts")
            .document(content.id)
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
    fun modifyDiaryWithPhoto(content: ContentDTO, imageUri: String) = callbackFlow {

        storage.getReferenceFromUrl(content.imageUrl)
            .putFile(Uri.parse(imageUri))
            .continueWithTask {
                it.result.storage.downloadUrl
            }
            .continueWithTask {
                if (it.isSuccessful) {
                    content.imageUrl = it.result.toString()

                    store.collection("posts").document(content.id).set(content)
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
                    content.id = uid + content.timestamp

                    store.collection("posts").document(content.id).set(content)
                } else {
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

    fun authWithCredential(credential: AuthCredential) = callbackFlow {
        auth.signInWithCredential(credential).continueWith { result ->
            if (result.isSuccessful) {
                offer(Pair(true, ""))
            } else {
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
                        } else {
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