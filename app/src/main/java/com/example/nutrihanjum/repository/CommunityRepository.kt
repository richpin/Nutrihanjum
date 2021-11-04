package com.example.nutrihanjum.repository


import android.util.Log
import androidx.core.content.contentValuesOf
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.repository.UserRepository.uid
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow as callbackFlow

object CommunityRepository {
    private val store get() = FirebaseFirestore.getInstance()
    private lateinit var lastVisible: DocumentSnapshot
    val boardLimit: Long = 3

    fun loadContentsInit() = callbackFlow {
        store.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .whereEqualTo("public", true)
            .limit(boardLimit)
            .get().continueWith {
                if (it.isSuccessful) {
                    it.result.documents.forEach { snapshot ->
                        trySend(snapshot.toObject(ContentDTO::class.java))
                    }
                    lastVisible = it.result.documents[it.result.size() - 1]
                } else {
                    Log.wtf("Repository", it.exception?.message)
                }
                close()
            }

        awaitClose()
    }

    fun loadContentsMore() = callbackFlow {
        store.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .whereEqualTo("public", true)
            .startAfter(lastVisible)
            .limit(boardLimit)
            .get().continueWith {
                if (it.isSuccessful) {
                    it.result.documents.forEach { snapshot ->
                        trySend(snapshot.toObject(ContentDTO::class.java))
                    }
                    lastVisible = it.result.documents[it.result.size() - 1]
                } else {
                    Log.wtf("Repository", it.exception?.message)
                }
                close()
            }

        awaitClose()
    }

    fun loadComments(contentId: String) = callbackFlow {
        store.collection("posts")
            .document(contentId)
            .collection("comments")
            .orderBy("timeStamp")
            .get().continueWith {
                if (it.isSuccessful) {
                    it.result.documents.forEach { snapshot ->
                        trySend(snapshot.toObject(ContentDTO.CommentDTO::class.java))
                    }
                } else {
                    Log.wtf("Repository", it.exception?.message)
                }
                close()
            }

        awaitClose()
    }

    fun addComment(contentId: String, commentDTO: ContentDTO.CommentDTO) = callbackFlow {
        val ref = store.collection("posts").document(contentId)


        ref.collection("comments")
            .document(commentDTO.id)
            .set(commentDTO).onSuccessTask {
                ref.update("commentCount", FieldValue.increment(1))
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

    fun deleteComment(contentId: String, commentId: String) = callbackFlow {
        val ref = store.collection("posts").document(contentId)

        ref.collection("comments")
            .document(commentId)
            .delete().onSuccessTask {
                ref.update("commentCount", FieldValue.increment(-1))
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
                trySend(true)
            } else {
                trySend(false)
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
                    trySend(false)
                    close()
                    null
                }
            }.continueWith {
                if (it.isSuccessful) trySend(true)
                else trySend(false)
            }
        } else {
            postRegistration.update("saved", FieldValue.arrayUnion(uid)).continueWithTask {
                if (it.isSuccessful) {
                    userRegistration.update("saved", FieldValue.arrayUnion(contentDTO.id))
                } else {
                    trySend(false)
                    close()
                    null
                }
            }.continueWith {
                if (it.isSuccessful) trySend(true)
                else trySend(false)
            }
        }

        awaitClose()
    }

    fun loadSavedContents() = callbackFlow {
        val ref = store.collection("users").document(uid!!)

        store.runTransaction { transaction ->
            val snapshot = transaction.get(ref)
            val savedId = snapshot.get("saved") as List<*>

            savedId.forEach {
                val post = transaction.get(store.collection("posts").document(it.toString()))
                trySend(post.toObject(ContentDTO::class.java))
            }
        }.continueWith {
            if (it.isSuccessful)
                close()
            else
                Log.wtf("Repository", it.exception?.message)
        }

        awaitClose()
    }

    fun loadMyContents() = callbackFlow {
        val ref = store.collection("users").document(uid!!)

        store.runTransaction { transaction ->
            val snapshot = transaction.get(ref)
            val savedId = snapshot.get("posts") as List<*>

            savedId.forEach {
                val post = transaction.get(store.collection("posts").document(it.toString()))
                trySend(post.toObject(ContentDTO::class.java))
            }
        }.continueWith {
            if (it.isSuccessful)
                close()
            else
                Log.wtf("Repository", it.exception?.message)
        }

        awaitClose()
    }
}