package com.example.nutrihanjum.repository

import android.net.Uri
import android.util.Log
import com.example.nutrihanjum.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.text.SimpleDateFormat
import java.util.*

object DiaryRepository {
    private val auth get() = FirebaseAuth.getInstance()
    private val store get() = FirebaseFirestore.getInstance()
    private val storage get() = FirebaseStorage.getInstance()

    private val uid get() = auth.currentUser?.uid

    val userEmail get() = auth.currentUser?.email
    val userName get() = auth.currentUser?.displayName
    val userPhoto get() = auth.currentUser?.photoUrl


    fun loadAllDiaryAtDate(date: String) = callbackFlow {
        store.collection("posts")
            .whereEqualTo("date", date)
            .whereEqualTo("uid", uid)
            .get().continueWith {
                if (it.isSuccessful) {
                    it.result.documents.forEach { item ->
                        trySend(item.toObject(ContentDTO::class.java)!!)
                    }
                } else {
                    Log.wtf("Repository", "${it.exception?.message}")
                }
                close()
            }

        awaitClose()
    }


    fun modifyDiaryWithoutPhoto(content: ContentDTO) = callbackFlow {
        store.collection("posts")
            .document(content.id)
            .update(
                "content", content.content,
                "mealTime", content.mealTime,
                "public", content.isPublic
            )
            .continueWith {
                if (it.isSuccessful) {
                    trySend(true)
                } else {
                    trySend(false)
                }
                close()
            }


        awaitClose()
    }


    fun modifyDiaryWithPhoto(content: ContentDTO, imageUri: String) = callbackFlow {

        storage.getReferenceFromUrl(content.imageUrl)
            .putFile(Uri.parse(imageUri))
            .continueWithTask {
                if (it.isSuccessful) {
                    it.result.storage.downloadUrl
                } else {
                    trySend(false)
                    close()
                    null
                }
            }
            .continueWithTask {
                if (it.isSuccessful) {
                    content.imageUrl = it.result.toString()

                    store.collection("posts").document(content.id).update(
                        "content", content.content,
                        "mealTime", content.mealTime,
                        "public", content.isPublic,
                        "imageUrl", content.imageUrl
                    )
                } else {
                    trySend(false)
                    close()
                    null
                }
            }
            .continueWith { result ->
                if (result.isSuccessful) {
                    trySend(true)
                } else {
                    trySend(false)
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
                } else {
                    trySend(false)
                    close()
                    null
                }
            }
            .continueWithTask {
                if (it.isSuccessful) {
                    store.collection("users").document(uid!!).update(
                        "posts",
                        FieldValue.arrayRemove(documentId)
                    )
                } else {
                    trySend(false)
                    close()
                    null
                }
            }
            .continueWith {
                if (it.isSuccessful) {
                    trySend(true)
                } else {
                    trySend(false)
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
                    trySend(false)
                    close()
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
                    trySend(false)
                    close()
                    null
                }
            }
            .continueWithTask { result ->
                if (result.isSuccessful) {
                    store.collection("users").document(uid!!).update(
                        "posts",
                        FieldValue.arrayUnion(content.id)
                    )
                } else {
                    trySend(false)
                    close()
                    null
                }
            }
            .continueWith {
                if (it.isSuccessful) {
                    trySend(true)
                } else {
                    trySend(false)
                }

                close()
            }

        awaitClose()
    }
}