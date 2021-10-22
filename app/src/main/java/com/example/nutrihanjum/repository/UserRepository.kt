package com.example.nutrihanjum.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.nutrihanjum.model.UserDTO
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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
                trySend(true)
            } else {
                trySend(false)
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
                    store.collection("users").document(uid!!).update("profileUrl",
                        Repository.userPhoto
                    )
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


    fun checkEmailValid(email: String) = callbackFlow {
        val registration = store.collection("users")
            .whereEqualTo("email", email)
            .addSnapshotListener { snapshot, err ->
                if (err != null || snapshot == null) return@addSnapshotListener

                if (snapshot.documents.isEmpty()) {
                    trySend(true)
                } else {
                    trySend(false)
                }
            }

        awaitClose { registration.remove() }
    }


    fun checkUserNameValid(name: String) = callbackFlow {
        val registration = store.collection("users")
            .whereEqualTo("name", name)
            .addSnapshotListener { snapshot, err ->
                if (err != null || snapshot == null) return@addSnapshotListener

                if (snapshot.documents.isEmpty()) {
                    trySend(true)
                } else {
                    trySend(false)
                }
            }

        awaitClose { registration.remove() }
    }


    fun createUserWithEmail(email: String, password: String, name: String) = callbackFlow {
        var userID: String = ""

        auth.createUserWithEmailAndPassword(email, password).continueWithTask { task ->
            if (task.isSuccessful) {
                userID = task.result.user!!.uid

                task.result.user!!.updateProfile(userProfileChangeRequest {
                    displayName = name
                })
            }
            else {
                trySend(false)
                close()
                null
            }
        }.continueWithTask { task ->
            if (task.isSuccessful) {
                val user = UserDTO()
                user.name = name
                user.email = email
                user.userID = userID

                store.collection("users").document(userID).set(user)
            }
            else {
                trySend(false)
                close()
                null
            }
        }.continueWith {
            if (it.isSuccessful) {
                trySend(true)
                auth.signOut()
            } else {
                trySend(false)
            }
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
        }

        awaitClose()
    }


    fun authWithCredential(credential: AuthCredential) = callbackFlow {
        auth.signInWithCredential(credential).continueWithTask { task ->
            if (task.isSuccessful) {
                if (task.result.additionalUserInfo!!.isNewUser) {
                    val user = UserDTO()
                    with(task.result.user!!) {
                        user.name = this.displayName ?: ""
                        user.email = this.email ?: ""
                        user.userID = this.uid
                        user.profileUrl = this.photoUrl.toString()
                    }
                    store.collection("users").document(task.result.user!!.uid).set(user)
                } else {
                    trySend(true)
                    close()
                    null
                }
            } else {
                trySend(false)
                close()
                null
            }
        }.continueWith { task ->
            if (task.isSuccessful) {
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


    fun isSigned() = auth.currentUser != null
}