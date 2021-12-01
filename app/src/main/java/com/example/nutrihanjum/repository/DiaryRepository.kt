package com.example.nutrihanjum.repository

import android.net.Uri
import android.util.Log
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.model.FoodDTO
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


    fun loadAllDiary(date: Int) = callbackFlow {
        store.collection("posts")
            .whereEqualTo("uid", uid)
            .whereGreaterThanOrEqualTo("date", date)
            .get()
            .addOnSuccessListener {
                trySend(it.toObjects(ContentDTO::class.java))
                close()
            }
            .addOnFailureListener {
                Log.wtf("dsfa", it.stackTraceToString())
                close()
            }

        awaitClose()
    }


    fun loadDiaryById(docId: String) = callbackFlow {
        store.collection("posts").document(docId).get()
            .addOnSuccessListener {
                trySend(it.toObject(ContentDTO::class.java))
                close()
            }
            .addOnFailureListener {
                trySend(null)
                close()
            }

        awaitClose()
    }


    fun loadAllDiaryAtDate(date: Int) = callbackFlow {
        store.collection("posts")
            .whereEqualTo("date", date)
            .whereEqualTo("uid", uid)
            .get().continueWith {
                if (it.isSuccessful) {
                    trySend(it.result.toObjects(ContentDTO::class.java))
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
                "public", content.isPublic,
                "foods", content.foods,
                "nutritionInfo", content.nutritionInfo,
                "hashTagList", content.hashTagList
            )
            .continueWith {
                if (it.isSuccessful) {
                    trySend(content)
                } else {
                    trySend(null)
                }
                close()
            }


        awaitClose()
    }


    fun modifyDiaryWithPhoto(content: ContentDTO, imageUri: String) = callbackFlow {

        storage.getReferenceFromUrl(content.imageUrl)
            .putFile(Uri.parse(imageUri))
            .onSuccessTask {
                it.storage.downloadUrl
            }
            .onSuccessTask {
                content.imageUrl = it.toString()

                store.collection("posts").document(content.id).update(
                    "content", content.content,
                    "mealTime", content.mealTime,
                    "public", content.isPublic,
                    "imageUrl", content.imageUrl,
                    "foods", content.foods,
                    "nutritionInfo", content.nutritionInfo,
                    "hashTagList", content.hashTagList
                )
            }
            .continueWith { result ->
                if (result.isSuccessful) {
                    trySend(content)
                } else {
                    trySend(null)
                }
                close()
            }

        awaitClose()
    }


    fun deleteDiary(documentId: String, imageUrl: String) = callbackFlow {
        storage.getReferenceFromUrl(imageUrl).delete().onSuccessTask {
            store.runBatch { batch ->
                batch.update(
                    store.collection("users").document(uid!!),
                    "posts",
                    FieldValue.arrayRemove(documentId)
                )
                batch.delete(store.collection("posts").document(documentId))
            }
        }.continueWith {
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
            .onSuccessTask {
                it.storage.downloadUrl
            }
            .onSuccessTask {
                content.imageUrl = it.toString()
                content.uid = uid!!
                content.id = uid + content.timestamp
                content.profileName = userName!!
                content.profileUrl = userPhoto.toString()

                store.runBatch { batch ->
                    batch.set(store.collection("posts").document(content.id), content)
                    batch.update(
                        store.collection("users").document(uid!!),
                        "posts",
                        FieldValue.arrayUnion(content.id)
                    )
                }

            }.continueWith {
                if (it.isSuccessful) {
                    trySend(content)
                } else {
                    trySend(null)
                }

                close()
            }

        awaitClose()
    }


    fun loadFoodList(foodName: String) = callbackFlow {
        store.collection("foods")
            .orderBy("name")
            .whereArrayContains("keywords", foodName)
            .limit(10)
            .get()
            .addOnSuccessListener {
                Log.wtf(this@DiaryRepository.javaClass.simpleName, it.documents.size.toString())
                trySend(it.documents.map { doc -> FoodDTO(
                    doc["name"].toString(),
                    doc["calorie"].toString().toFloatOrNull() ?: 0f,
                    doc["carbohydrate"].toString().toFloatOrNull() ?: 0f,
                    doc["protein"].toString().toFloatOrNull() ?: 0f,
                    doc["fat"].toString().toFloatOrNull() ?: 0f
                )})

                close()
            }
            .addOnFailureListener {
                Log.wtf(this@DiaryRepository.javaClass.simpleName, it.stackTraceToString())
                close()
            }

        awaitClose()
    }
}