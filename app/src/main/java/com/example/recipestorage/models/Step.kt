package com.example.recipestorage.models

class Step(
    var id: Int? = null,
    var step: String,
    var recipeId: Long


) {
    override fun toString(): String {
        return "Step(id=$id, step='$step', recipeId=$recipeId)"
    }
}
