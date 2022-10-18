package com.example.recipestorage.models

class User(
    val id: Long,
    val email: String,
    val token: String,
    val imageUrl: String
) {
    override fun toString(): String {
        return "User(id=$id, email='$email', token='$token', imageUrl='$imageUrl')"
    }
}