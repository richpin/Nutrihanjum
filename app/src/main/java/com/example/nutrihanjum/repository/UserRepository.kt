package com.example.nutrihanjum.repository

import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.nutrihanjum.model.UserDTO
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

object UserRepository {
    private val auth get() = FirebaseAuth.getInstance()
    private val store get() = FirebaseFirestore.getInstance()
    private val storage get() = FirebaseStorage.getInstance()

    val uid get() = auth.currentUser?.uid

    val userEmail get() = auth.currentUser?.email
    val userName get() = auth.currentUser?.displayName
    val userPhoto get() = auth.currentUser?.photoUrl


    fun updateUserProfile(email: String, name: String, photo: Uri?) = callbackFlow {
        val tasks = ArrayList<Task<Void>>()

        if (email != userEmail) {
            tasks.add(updateEmail(email))
        }
        if (name != userName) {
            tasks.add(updateName(name))
        }
        if (photo != null) {
            tasks.add(updateProfileImage(photo))
        }

        Tasks.whenAllComplete(tasks).onSuccessTask {
            trySend(true)
            updateUserDB(
                userEmail!!,
                userName!!,
                if (userPhoto == null) "" else userPhoto.toString()
            )

        }.continueWith {
            close()
        }

        awaitClose()
    }


    private fun updateProfileImage(photo: Uri): Task<Void> {
        return storage.reference.child("profileImages")
            .child(uid!!)
            .putFile(photo)
            .onSuccessTask {
                it.storage.downloadUrl
            }
            .continueWithTask {
                if (it.isSuccessful) {
                    auth.currentUser!!.updateProfile(userProfileChangeRequest {
                        photoUri = it.result
                    })
                } else {
                    throw Exception(it.exception)
                }
            }
    }


    private fun updateEmail(email: String): Task<Void> {
        return auth.currentUser!!.updateEmail(email)
    }


    private fun updateName(name: String): Task<Void> {
        return auth.currentUser!!.updateProfile(userProfileChangeRequest {
            displayName = name
        })
    }


    private fun updateUserDB(email: String, name: String, photo: String): Task<Void> {
        val doc = store.collection("users").document(uid!!)

        return store.runTransaction { transaction ->
            val snapshot = transaction.get(doc)
            val posts = snapshot.get("posts") as List<*>

            posts.forEach { postId ->
                transaction.update(
                    store.collection("posts").document(postId.toString()),
                    "profileName", name,
                    "profileUrl", photo
                )
            }

            transaction.update(
                doc,
                "email", email,
                "name", name,
                "profileUrl", photo
            )

            null
        }
    }


    fun checkEmailUnique(email: String) = callbackFlow {
        val registration = store.collection("users")
            .whereEqualTo("email", email)
            .addSnapshotListener { snapshot, err ->
                if (err != null || snapshot == null) return@addSnapshotListener

                trySend(snapshot.documents.isNullOrEmpty())
            }

        awaitClose { registration.remove() }
    }


    fun checkUserNameUnique(name: String) = callbackFlow {
        val registration = store.collection("users")
            .whereEqualTo("name", name)
            .addSnapshotListener { snapshot, err ->
                if (err != null || snapshot == null) return@addSnapshotListener

                trySend(snapshot.documents.isNullOrEmpty())
            }

        awaitClose { registration.remove() }
    }


    fun createUserWithEmail(email: String, password: String, name: String) = callbackFlow {
        var userID: String = ""

        auth.createUserWithEmailAndPassword(email, password).onSuccessTask { result ->
            userID = result.user!!.uid

            result.user!!.updateProfile(userProfileChangeRequest {
                displayName = name
            })

        }.onSuccessTask { result ->
            val user = UserDTO()
            user.name = name
            user.email = email
            user.userID = userID

            store.collection("users").document(userID).set(user)

        }.continueWith {
            if (it.isSuccessful) {
                trySend(true)
            } else {
                trySend(false)
            }
            auth.signOut()
            close()
        }

        awaitClose()
    }


    fun signInWithEmail(email: String, password: String) = callbackFlow {
        auth.signInWithEmailAndPassword(email, password).continueWith {
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
        auth.signInWithCredential(credential).onSuccessTask { result ->
            if (result.additionalUserInfo!!.isNewUser) {
                val user = UserDTO()
                with(result.user!!) {
                    user.name = this.displayName ?: ""
                    user.email = this.email ?: ""
                    user.userID = this.uid
                    user.profileUrl = this.photoUrl.toString()
                }
                store.collection("users").document(result.user!!.uid).set(user)
            } else {
                throw Exception("NOT NEW USER")
            }
        }.continueWith { task ->
            if (task.isSuccessful) {
                trySend(true)
            } else if ("NOT NEW USER".contentEquals(task.exception?.message)) {
                trySend(true)
            } else {
                trySend(false)
            }
            close()
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

    fun updateToken(newToken: String) {
        val uref = store.collection("users").document(uid!!)

        store.runTransaction { transaction ->
            val snapshot = transaction.get(uref)

            val tokens = snapshot.get("tokens") as List<String>
            if(!tokens.contains(newToken)) {
                transaction.update(uref, "tokens", FieldValue.arrayUnion(newToken))
            }
        }.addOnSuccessListener { Log.d(TAG, "Token registration success!") }
            .addOnFailureListener { e -> Log.w(TAG, "Token registration failure.", e) }
    }

    fun isSigned() = auth.currentUser != null
}