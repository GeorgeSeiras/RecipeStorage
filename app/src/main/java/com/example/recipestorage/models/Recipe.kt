package com.example.recipestorage.models

class Recipe(
    var id: Int? = null,
    var title: String,
    var prepTime: Int,
    var cookTime: Int,
    var course: String,
    var origin: String,
    var steps: ArrayList<Step?> = arrayListOf(null),
    var ingredients: ArrayList<Ingredient?> = arrayListOf(null),

    ) {

    override fun toString(): String {
        val stepsList: ArrayList<String> = arrayListOf()
        val ingredientsList: ArrayList<String> = arrayListOf()
        for (step in steps) {
            stepsList.add(step.toString())
        }
        for (ingredient in ingredients) {
            ingredientsList.add(ingredient.toString())
        }
        return "Recipe(id=$id, title='$title', prepTime=$prepTime, cookTime=$cookTime, course='$course', origin='$origin', steps=$stepsList, ingredients=$ingredientsList)"
    }
}