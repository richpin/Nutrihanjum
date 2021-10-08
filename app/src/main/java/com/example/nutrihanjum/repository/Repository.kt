package com.example.nutrihanjum.repository

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.nutrihanjum.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

object Repository {
    private val auth get() = FirebaseAuth.getInstance()
    private val store get() = FirebaseFirestore.getInstance()
    private val storage get() = FirebaseStorage.getInstance()

    private val uid get() = auth.currentUser?.uid

    val userEmail get() = auth.currentUser?.email
    val userID get() = auth.currentUser?.displayName
    val userPhoto get() = auth.currentUser?.photoUrl

    @ExperimentalCoroutinesApi
    fun authWithCredential(credential : AuthCredential) = callbackFlow {
        auth.signInWithCredential(credential).continueWith { result ->
            if (result.isSuccessful) {
                offer(Pair(true, ""))
            }
            else {
                offer(Pair(false, result.exception?.message))
            }
            close()
        }

        awaitClose()
    }

    fun signOut(context: Context) {
        if (!isSigned()) return

        for (provider in auth.currentUser!!.providerData) {
            when (provider.providerId) {
                FirebaseAuthProvider.PROVIDER_ID -> {
                    auth.signOut()
                }
                GoogleAuthProvider.PROVIDER_ID -> {
                    Log.wtf("SING_OUT", "IN")
                    val gso = GoogleSignInOptions
                        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .build()

                    GoogleSignIn.getClient(context, gso).signOut().continueWith {
                        if (it.isSuccessful) {
                            Log.wtf("SING_OUT", "Success")
                        }
                        else {
                            Log.wtf("SING_OUT", "Failed")
                        }
                    }
                }
            }
        }
    }

    fun isSigned() = auth.currentUser != null
}