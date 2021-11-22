package com.example.nutrihanjum.repository


import android.util.Log
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.model.UserDTO
import com.example.nutrihanjum.repository.UserRepository.uid
import com.example.nutrihanjum.repository.UserRepository.userName
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.HttpsCallableResult
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow as callbackFlow

object CommunityRepository {
    private val store get() = FirebaseFirestore.getInstance()
    private val functions = Firebase.functions
    private lateinit var lastVisibleContent: DocumentSnapshot
    private lateinit var lastVisibleNotice: DocumentSnapshot
    val boardLimit: Long = 20

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
                    lastVisibleContent = it.result.documents[it.result.size() - 1]
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
            .startAfter(lastVisibleContent)
            .limit(boardLimit)
            .get().continueWith {
                if (it.isSuccessful) {
                    it.result.documents.forEach { snapshot ->
                        trySend(snapshot.toObject(ContentDTO::class.java))
                    }
                    lastVisibleContent = it.result.documents[it.result.size() - 1]
                } else {
                    Log.wtf("Repository", it.exception?.message)
                }
                close()
            }

        awaitClose()
    }

    fun loadNoticesInit() = callbackFlow {
        store.collection("users")
            .document(uid!!)
            .collection("notices")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(boardLimit)
            .get().continueWith {
                if (it.isSuccessful) {
                    it.result.documents.forEach { snapshot ->
                        trySend(snapshot.toObject(UserDTO.NoticeDTO::class.java))
                    }
                    lastVisibleNotice = it.result.documents[it.result.size() - 1]
                } else {
                    Log.wtf("Repository", it.exception?.message)
                }
                close()
            }
        awaitClose()
    }

    fun loadNoticesMore() = callbackFlow {
        store.collection("users")
            .document(uid!!)
            .collection("notices")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .startAfter(lastVisibleNotice)
            .limit(boardLimit)
            .get().continueWith {
                if (it.isSuccessful) {
                    it.result.documents.forEach { snapshot ->
                        trySend(snapshot.toObject(UserDTO.NoticeDTO::class.java))
                    }
                    lastVisibleNotice = it.result.documents[it.result.size() - 1]
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

    fun loadUserInfo(uid: String) = callbackFlow {
        store.collection("users")
            .document(uid)
            .get().continueWith {
                if (it.isSuccessful) {
                    val name = it.result.get("name") as String
                    val url = it.result.get("profileUrl") as String
                    trySend(Pair(name, url))
                } else {
                    Log.wtf("Repository", it.exception?.message)
                }
                close()
            }
        awaitClose()
    }

    fun addComment(contentDTO: ContentDTO, commentDTO: ContentDTO.CommentDTO) = callbackFlow {
        val ptref = store.collection("posts").document(contentDTO.id)
        val ntref = store.collection("users").document(contentDTO.uid)
            .collection("notices").document()

        store.runBatch { batch ->
            batch.set(ptref.collection("comments").document(commentDTO.id), commentDTO)
            batch.update(ptref, "commentCount", FieldValue.increment(1))

            val newNotice = UserDTO.NoticeDTO()
            newNotice.kind = 1
            newNotice.timestamp = System.currentTimeMillis()
            newNotice.uid = contentDTO.uid
            newNotice.senderName = userName!!
            newNotice.content = '"' + commentDTO.comment + '"'
            newNotice.contentId = contentDTO.id
            newNotice.contentUrl = contentDTO.imageUrl

            batch.set(ntref, newNotice)
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

        ref.collection("comments").document(commentId).delete().onSuccessTask {
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
        val ptref = store.collection("posts").document(contentDTO.id)
        val ntref =
            store.collection("users").document(contentDTO.uid).collection("notices").document()

        store.runBatch { batch ->
            if (isLiked) {
                batch.update(ptref, "likes", FieldValue.arrayRemove(uid))
            } else {
                batch.update(ptref, "likes", FieldValue.arrayUnion(uid))

                val newNotice = UserDTO.NoticeDTO()
                newNotice.kind = 0
                newNotice.timestamp = System.currentTimeMillis()
                newNotice.uid = contentDTO.uid
                newNotice.senderName = userName!!
                newNotice.contentId = contentDTO.id
                newNotice.contentUrl = contentDTO.imageUrl

                batch.set(ntref, newNotice)
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

    fun loadSelectedContent(contentId: String) = callbackFlow {
        store.collection("posts")
            .document(contentId)
            .get().continueWith {
                if (it.isSuccessful) {
                    trySend(it.result.toObject(ContentDTO::class.java))
                } else {
                    Log.wtf("Repository", it.exception?.message)
                }
                close()
            }
        awaitClose()
    }

    fun sendReportMail(type: Int, contentId: String, commentId: String? = null): Task<HttpsCallableResult> {
        val data = hashMapOf(
            "type" to type,
            "contentId" to contentId,
            "commentId" to commentId
        )

        return functions.getHttpsCallable("sendReportMail").call(data)
    }
}