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

    fun updateUserProfileName(name: String) = callbackFlow {
        auth.currentUser!!.updateProfile(userProfileChangeRequest {
            displayName = name
        }).continueWithTask {
            if (it.isSuccessful) {
                store.collection("users").document(uid!!).update("name", name)
            } else {
                trySend(false)
                close()
                null
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

    fun updateUserProfilePhoto(photo: Uri) = callbackFlow {
        storage.reference.child("profileImages").child(uid!!)
            .putFile(photo)
            .continueWithTask {
                if (it.isSuccessful) {
                    it.result.storage.downloadUrl
                } else {
                    trySend(false)
                    close()
                    null
                }
            }.continueWithTask {
                if (it.isSuccessful) {
                    auth.currentUser!!.updateProfile(userProfileChangeRequest {
                        photoUri = it.result
                    })
                } else {
                    trySend(false)
                    close()
                    null
                }
            }
            .continueWithTask {
                if (it.isSuccessful) {
                    store.collection("users").document(uid!!).update("profileUrl", userPhoto)
                } else {
                    trySend(false)
                    close()
                    null
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
                    offer(false)
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

    fun authWithCredential(credential: AuthCredential) = callbackFlow {
        auth.signInWithCredential(credential).continueWithTask { task ->
            if (task.isSuccessful) {
                Log.wtf("Repository", "${task.result.additionalUserInfo!!.isNewUser}")
                if (task.result.additionalUserInfo!!.isNewUser) {
                    val user = UserDTO()
                    with(task.result.user!!) {
                        user.name = this.displayName ?: ""
                        user.userID = this.uid
                        user.profileUrl = this.photoUrl.toString()
                    }
                    store.collection("users").document(task.result.user!!.uid).set(user)
                } else {
                    trySend(Pair(true, ""))
                    close()
                    null
                }
            } else {
                trySend(Pair(false, task.exception?.message))
                close()
                null
            }
        }.continueWith { task ->
            if (task.isSuccessful) {
                trySend(Pair(true, ""))
            } else {
                trySend(Pair(false, task.exception?.message))
            }
        }

        awaitClose()
    }

    fun signOut(context: Context) {
        for (provider in auth.currentUser!!.providerData) {
            when (provider.providerId) {
                FirebaseAuthProvider.PROVIDER_ID -> {
                    auth.signOut()
                }
                GoogleAuthProvider.PROVIDER_ID -> {
                    val gso = GoogleSignInOptions
                        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .build()

                    GoogleSignIn.getClient(context, gso).signOut()
                }
            }
        }
    }

    fun isSigned() = auth.currentUser != null
}