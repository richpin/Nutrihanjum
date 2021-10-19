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

    private val uid get() = auth.currentUser?.uid

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