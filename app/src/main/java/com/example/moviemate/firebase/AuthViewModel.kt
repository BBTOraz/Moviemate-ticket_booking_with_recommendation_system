package com.example.moviemate.firebase

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    val user = MutableLiveData<FirebaseUser?>()

    fun registerUser(name: String, city: String, email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    val userData = mapOf(
                        "name" to name,
                        "city" to city,
                        "email" to email,
                        "password" to password,
                        "ticket_purchase" to emptyList<Map<String, Any>>()
                    )
                    firestore.collection("users").document(firebaseUser!!.uid).set(userData)
                        .addOnSuccessListener {
                            onSuccess()
                        }
                        .addOnFailureListener {
                            onError(it.message ?: "Unknown error")
                        }
                } else {
                    onError(task.exception?.message ?: "Unknown error")
                }
            }
    }

    fun loginUser(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user.value = auth.currentUser
                    onSuccess()
                } else {
                    onError(task.exception?.message ?: "Unknown error")
                }
            }
    }

    fun logout() {
        auth.signOut()
        user.value = null
    }
}