package com.example.recipestorage.models

class Token(
    val id: Long,
    val token: String,
    val refresh: String,
    val expiry: Int
) {
    override fun toString(): String {
        return "Token(id=$id, token='$token', refresh='$refresh', expiry=$expiry)"
    }
}