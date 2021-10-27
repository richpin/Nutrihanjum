package com.example.nutrihanjum.repository


import android.util.Log
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.repository.UserRepository.uid
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

object CommunityRepository {
    private val store get() = FirebaseFirestore.getInstance()

    fun loadContents() = callbackFlow {
        store.collection("posts")
            .orderBy("timestamp")
            .whereEqualTo("public", true)
            .get().continueWith {
                if (it.isSuccessful) {
                    it.result.documents.forEach { snapshot ->
                        trySend(snapshot.toObject(ContentDTO::class.java))
                    }
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
            .set(commentDTO).continueWithTask {
                if (it.isSuccessful) {
                    ref.update("commentCount", FieldValue.increment(1))
                } else {
                    trySend(false)
                    close()
                    null
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

    fun deleteComment(contentId: String, commentId: String) = callbackFlow {
        val ref = store.collection("posts").document(contentId)

        ref.collection("comments")
            .document(commentId)
            .delete().continueWithTask {
                if (it.isSuccessful) {
                    ref.update("commentCount", FieldValue.increment(-1))
                } else {
                    trySend(false)
                    close()
                    null
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
}