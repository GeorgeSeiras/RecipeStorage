package com.example.recipestorage.models

class Ingredient(
    var id: Int,
    var unit: String,
    var amount: String,
    var ingredient: String,
    var recipeId: Long

) {
    override fun toString(): String {
        return "Ingredient(id=$id, unit='$unit', amount='$amount', ingredient='$ingredient', recipeId=$recipeId)"
    }
}