package com.example.nurture_nest.data

import com.example.nurture_nest.model.Child
import com.example.nurture_nest.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()

    // Get all users by role (parent or teacher)
    suspend fun getUsersByRole(role: String): List<User> {
        val snapshot = db.collection("users")
            .whereEqualTo("role", role)
            .get()
            .await()
        return snapshot.toObjects(User::class.java)
    }

    // Add a new user
    suspend fun addUser(user: User): String {
        val docRef = db.collection("users").add(user).await()
        return docRef.id
    }

    // Add a new child
    suspend fun addChild(child: Child): String {
        val docRef = db.collection("children").add(child).await()
        return docRef.id
    }

    // Assign child to a user (parent or teacher)
    suspend fun assignChildToUser(userId: String, childId: String) {
        val userRef = db.collection("users").document(userId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val assigned = snapshot.get("assignedChildren") as? List<String> ?: emptyList()
            val updated = assigned.toMutableList()
            if (!updated.contains(childId)) updated.add(childId)
            transaction.update(userRef, "assignedChildren", updated)
        }.await()
    }
}
