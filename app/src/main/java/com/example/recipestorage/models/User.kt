package com.example.recipestorage.models

class User(
    val id: Long,
    val email: String,
    val gid: String
) {
    override fun toString(): String {
        return "User(id=$id, email='$email', gid='$gid)"
    }
}