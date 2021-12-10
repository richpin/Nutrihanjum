package com.example.nutrihanjum.repository

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.example.nutrihanjum.R
import com.example.nutrihanjum.model.UserDTO
import com.example.nutrihanjum.user.login.LoginFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.storage.FirebaseStorage
import com.kakao.sdk.user.UserApiClient
import com.nhn.android.naverlogin.OAuthLogin
import com.nhn.android.naverlogin.OAuthLoginHandler
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

object UserRepository {
    private val auth get() = FirebaseAuth.getInstance()
    private val store get() = FirebaseFirestore.getInstance()
    private val storage get() = FirebaseStorage.getInstance()
    private val userClient get() = UserApiClient.instance
    private val functions = FirebaseFunctions.getInstance("asia-northeast3")

    val uid get() = auth.currentUser?.uid

    val userEmail get() = auth.currentUser?.email
    val userName get() = auth.currentUser?.displayName
    val userPhoto get() = auth.currentUser?.photoUrl
    val signedDate get() = auth.currentUser?.metadata?.creationTimestamp


    fun reAuthenticate(password: String) = callbackFlow {
        val credential = EmailAuthProvider.getCredential(userEmail!!, password)

        auth.currentUser!!.reauthenticate(credential).addOnSuccessListener {
            trySend(true)
            close()

        }.addOnFailureListener {
            trySend(false)
            close()
        }

        awaitClose()
    }


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

    fun signInWithKakaotalk(mContext: Context) = callbackFlow{
        if (!userClient.isKakaoTalkLoginAvailable(mContext)) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data =
                Uri.parse("market://details?id=" + mContext.getString(R.string.kakaotalk_packagename))
            startActivity(mContext, intent, null)
            trySend(false)
            close()
        } else userClient.loginWithKakaoTalk(mContext) { token, error ->
            if (error != null) {
                Log.wtf("로그인 실패", error)
                trySend(false)
                close()
            } else if (token != null) {
                Log.wtf("로그인 성공", token.accessToken)
                getKaKaoFirebaseJwt(token.accessToken).onSuccessTask { result ->
                    auth.signInWithCustomToken(result)
                }.continueWith {  task ->
                    if (task.isSuccessful) {
                        trySend(true)
                    } else {
                        val e = task.exception
                        if(e is FirebaseFunctionsException){
                            Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(mContext, mContext.getString(R.string.kakaotalk_login_failed), Toast.LENGTH_LONG).show()
                        }
                        Log.wtf("Firebase Auth Failed with Kakaotalk", task.exception.toString())
                        trySend(false)
                    }
                    close()
                }
            }
        }
        awaitClose()
    }

    private fun getKaKaoFirebaseJwt(accessToken: String): Task<String> {
        val source = TaskCompletionSource<String>()

        val data = hashMapOf(
            "token" to accessToken
        )

        functions.getHttpsCallable("kakaoCustomAuth")
            .call(data).continueWith { task->
                if(task.isSuccessful) source.setResult(task.result.data as String)
                else task.exception?.let { exception -> source.setException(exception) }
            }

        return source.task
    }

    fun signInWithNaver(accessToken: String, mContext: Context) = callbackFlow {
        getNaverFirebaseJwt(accessToken).onSuccessTask { result ->
            auth.signInWithCustomToken(result)
        }.continueWith { task ->
            if (task.isSuccessful) {
                trySend(true)
            } else {
                val e = task.exception
                if(e is FirebaseFunctionsException){
                    Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.naver_login_failed), Toast.LENGTH_LONG).show()
                }
                Log.wtf("Firebase Auth Failed with Naver", task.exception.toString())
                trySend(false)
            }
            close()
        }
        awaitClose()
    }

    private fun getNaverFirebaseJwt(accessToken: String): Task<String> {
        val source = TaskCompletionSource<String>()

        val data = hashMapOf(
            "token" to accessToken
        )

        functions.getHttpsCallable("naverCustomAuth")
            .call(data).continueWith { task->
                if(task.isSuccessful) source.setResult(task.result.data as String)
                else task.exception?.let { exception -> source.setException(exception) }
            }

        return source.task
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

    fun getNoticeFlag() = callbackFlow {
        store.collection("users")
            .document(uid!!)
            .get().continueWith {
                if(it.isSuccessful){
                    trySend(it.result["noticeFlag"] as Boolean)
                } else {
                    Log.wtf("NoticeFlag", "Get Failed")
                }
                close()
            }
        awaitClose()
    }

    fun updateNoticeFlag(isChecked: Boolean) = callbackFlow {
        store.collection("users")
            .document(uid!!)
            .update("noticeFlag", isChecked)
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

    fun updateToken(newToken: String) {
        if (isSigned()) {
            val uref = store.collection("users").document(uid!!)

            store.runTransaction { transaction ->
                val snapshot = transaction.get(uref)

                val tokens = snapshot.get("tokens") as List<String>
                if (!tokens.contains(newToken)) {
                    transaction.update(uref, "tokens", FieldValue.arrayUnion(newToken))
                }
            }.addOnSuccessListener { Log.d(TAG, "Token registration success!") }
                .addOnFailureListener { e -> Log.w(TAG, "Token registration failure.", e) }
        }
    }

    fun isSigned() = auth.currentUser != null
}