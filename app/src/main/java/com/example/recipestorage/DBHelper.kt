package com.example.recipestorage

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.recipestorage.models.Ingredient
import com.example.recipestorage.models.Recipe
import com.example.recipestorage.models.Step

class DatabaseHandler(context: Context) :
    SQLiteOpenHelper(context, databaseName, null, databaseVersion) {
    companion object {
        private val databaseVersion = 1
        private val databaseName = "EmployeeDatabase"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(recipeCreateQuery)
        db.execSQL(stepCreateQuery)
        db.execSQL(ingredientCreateQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $recipeTableName")
        db.execSQL("DROP TABLE IF EXISTS $stepTableName")
        db.execSQL("DROP TABLE IF EXISTS $ingredientTableName")
        onCreate(db)
    }


    // RECIPE TABLE
    private val recipeTableName = "recipe"
    private val recipeId = "id"
    private val recipeTitle = "title"
    private val recipePrepTime = "prep_time"
    private val recipeCookTime = "cook_time"
    private val recipeCourse = "course"
    private val recipeOrigin = "origin"

    private val recipeCreateQuery = ("create table if not exists " + recipeTableName + " ("
            + recipeId + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + recipeTitle + " TEXT NOT NULL,"
            + recipePrepTime + " INT NOT NULL,"
            + recipeCookTime + " INT NOT NULL,"
            + recipeCourse + " STRING NOT NULL,"
            + recipeOrigin + " STRING NOT NULL"
            + ");")

    fun addRecipe(
        title: String,
        course: String,
        origin: String,
        prepTime: Int,
        cookTime: Int,
        db: SQLiteDatabase
    ): Long {
        return try {
            val contentValues = ContentValues()

            contentValues.put(recipeTitle, title)
            contentValues.put(recipePrepTime, prepTime)
            contentValues.put(recipeCookTime, cookTime)
            contentValues.put(recipeCourse, course)
            contentValues.put(recipeOrigin, origin)

            db.insert(recipeTableName, null, contentValues)
        } catch (e: Exception) {
            -1
        }
    }

    fun updateRecipe(
        title: String,
        course: String,
        origin: String,
        prepTime: Int,
        cookTime: Int,
        originalRecipe: Recipe,
        db: SQLiteDatabase
    ): Int {
        return try {
            val contentValues = ContentValues()

            contentValues.put(recipeTitle, title)
            contentValues.put(recipePrepTime, prepTime)
            contentValues.put(recipeCookTime, cookTime)
            contentValues.put(recipeCourse, course)
            contentValues.put(recipeOrigin, origin)

            val oldValues = arrayOf(
                originalRecipe.title,
                originalRecipe.prepTime.toString(),
                originalRecipe.cookTime.toString(),
                originalRecipe.course,
                originalRecipe.origin,
            )

            val table: String = recipeTableName
            val selectionArgs =
                "$recipeTitle=? and $recipePrepTime=? and $recipeCookTime=? and $recipeCourse=? and $recipeOrigin=?"
            db.update(table, contentValues, selectionArgs, oldValues)
        } catch (e: Exception) {
            -1
        }
    }

    private fun populateRecipe(
        cursor: Cursor,
        db: SQLiteDatabase
    ): Recipe {

        return Recipe(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(recipeId)),
            title = cursor.getString(cursor.getColumnIndexOrThrow(recipeTitle)),
            prepTime = cursor.getInt(cursor.getColumnIndexOrThrow(recipePrepTime)),
            cookTime = cursor.getInt(cursor.getColumnIndexOrThrow(recipeCookTime)),
            course = cursor.getString(cursor.getColumnIndexOrThrow(recipeCourse)),
            origin = cursor.getString(cursor.getColumnIndexOrThrow(recipeOrigin)),
            steps = getRecipeSteps(cursor.getLong(cursor.getColumnIndexOrThrow(recipeId)), db),
            ingredients = getRecipeIngredients(
                cursor.getLong(cursor.getColumnIndexOrThrow(recipeId)),
                db
            )
        )
    }

    fun getRecipes(db: SQLiteDatabase): ArrayList<Recipe> {
        val recipes: ArrayList<Recipe> = arrayListOf()
        val table = recipeTableName
        val columns = null
        val selection = null
        val selectionArgs: Array<String> = arrayOf()
        val groupBy: String? = null
        val having: String? = null
        val orderBy = "$recipeId DESC"
        val limit = null
        val cursor: Cursor =
            db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
        if (cursor.moveToFirst()) {
            do {
                recipes.add(populateRecipe(cursor, db))
            } while (cursor.moveToNext())
        }
        return recipes
    }

    fun getRecipesQuery(
        title: String,
        course: String,
        origin: String,
        db: SQLiteDatabase
    ): ArrayList<Recipe> {
        val table = recipeTableName
        val columns = null
        val selection = "$recipeTitle LIKE ? AND $recipeCourse LIKE ? AND $recipeOrigin LIKE ?"
        val selectionArgs: Array<String> = arrayOf("%$title%", "%$course%", "%$origin%")
        val groupBy: String? = null
        val having: String? = null
        val orderBy = "$recipeId DESC"
        val limit = null
        val cursor: Cursor =
            db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
        val recipes: ArrayList<Recipe> = arrayListOf()
        if (cursor.moveToFirst()) {
            do {
                recipes.add(populateRecipe(cursor, db))
            } while (cursor.moveToNext())
        }
        return recipes
    }

    fun getRecipeById(id: Long, db: SQLiteDatabase): Recipe {
        val table = recipeTableName
        val columns = null
        val selection = "$recipeId = ?"
        val selectionArgs: Array<String> = arrayOf("$id")
        val groupBy: String? = null
        val having: String? = null
        val orderBy = null
        val limit = null
        val cursor: Cursor =
            db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
        if (cursor.moveToFirst()) {
            return populateRecipe(cursor, db)
        }
        throw Exception("Recipe not found")
    }

    // STEP TABLE
    private val stepTableName = "step"
    private val stepId = "id"
    private val stepStep = "step"
    private val stepRecipeId = "recipe_id"
    private val stepCreateQuery = ("create table if not exists " + stepTableName + " ("
            + stepId + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + stepStep + " TEXT NOT NULL,"
            + stepRecipeId + " INTEGER NOT NULL,"
            + " foreign key($stepRecipeId) references recipe($recipeId)"
            + ");")

    private fun populateStep(cursor: Cursor): Step {
        try {
            val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow(stepId))
            val step: String = cursor.getString(cursor.getColumnIndexOrThrow(stepStep))
            val recipeId: Long = cursor.getLong(cursor.getColumnIndexOrThrow(stepRecipeId))
            return Step(id = id, step = step, recipeId = recipeId)
        } catch (e: Exception) {
            Log.e("ERROR", e.toString())
            throw Exception("Error while retrieving origins")
        }
    }

    fun addStep(
        step: String,
        recipeId: Long,
        db: SQLiteDatabase
    ): Long {
        val contentValues = ContentValues()

        contentValues.put(stepStep, step)
        contentValues.put(stepRecipeId, recipeId)

        return db.insertOrThrow(stepTableName, null, contentValues)
    }

    fun addSteps(steps: ArrayList<Step>, db: SQLiteDatabase): ArrayList<Long> {
        val results: ArrayList<Long> = arrayListOf()
        for (step in steps) {
            val contentValues = ContentValues()
            contentValues.put(stepStep, step.step)
            contentValues.put(stepRecipeId, step.recipeId)
            results.add(db.insertOrThrow(stepTableName, null, contentValues))
        }
        return results
    }

    fun removeStep(id: Long, db: SQLiteDatabase): Int {
        return db.delete(stepTableName, "$stepId=?", arrayOf("$id"))
    }

    private fun getRecipeSteps(id: Long, db: SQLiteDatabase): ArrayList<Step> {
        val table = stepTableName
        val columns = null
        val selection = "$stepRecipeId = ?"
        val selectionArgs: Array<String> = arrayOf("$id")
        val groupBy: String? = null
        val having: String? = null
        val orderBy = null
        val limit = null
        val cursor: Cursor =
            db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
        if (cursor.moveToFirst()) {
            val steps: ArrayList<Step> = arrayListOf()
            do {
                steps.add(populateStep(cursor))
            } while (cursor.moveToNext())
            return steps
        }
        throw Exception("Error while retrieving steps")
    }

    //INGREDIENT TABLE
    private val ingredientTableName = "ingredient"
    private val ingredientId = "id"
    private val ingredientAmount = "amount"
    private val ingredientUnit = "unit"
    private val ingredientIngredient = "ingredient"
    private val ingredientRecipeId = "recipe_id"
    private val ingredientCreateQuery = ("create table if not exists " + ingredientTableName + " ("
            + ingredientId + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + ingredientAmount + " TEXT,"
            + ingredientUnit + " TEXT,"
            + ingredientIngredient + " TEXT NOT NULL,"
            + ingredientRecipeId + " INTEGER NOT NULL,"
            + " foreign key($ingredientRecipeId) references recipe($recipeId)"
            + ");")

    fun addIngredient(
        unit: String,
        amount: String,
        ingredient: String,
        recipeId: Long,
        db: SQLiteDatabase
    ): Long {
        val contentValues = ContentValues()

        contentValues.put(ingredientAmount, amount)
        contentValues.put(ingredientUnit, unit)
        contentValues.put(ingredientIngredient, ingredient)
        contentValues.put(ingredientRecipeId, recipeId)

        return db.insert(ingredientTableName, null, contentValues)
    }

    fun addIngredients(ingredients: ArrayList<Ingredient>, db: SQLiteDatabase): ArrayList<Long> {
        val results: ArrayList<Long> = arrayListOf()
        for (ingredient in ingredients) {
            val contentValues = ContentValues()
            contentValues.put(ingredientAmount, ingredient.amount)
            contentValues.put(ingredientUnit, ingredient.unit)
            contentValues.put(ingredientIngredient, ingredient.ingredient)
            contentValues.put(ingredientRecipeId, ingredient.recipeId)
            results.add(db.insertOrThrow(ingredientTableName, null, contentValues))
        }
        return results
    }

    private fun getRecipeIngredients(id: Long, db: SQLiteDatabase): ArrayList<Ingredient> {
        val table = ingredientTableName
        val columns = null
        val selection = "$ingredientRecipeId =?"
        val selectionArgs: Array<String> = arrayOf("$id")
        val groupBy: String? = null
        val having: String? = null
        val orderBy = null
        val limit = null
        val cursor: Cursor =
            db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
        val ingredients: ArrayList<Ingredient> = arrayListOf()
        if (cursor.moveToFirst()) {
            do {
                ingredients.add(populateIngredient(cursor))
            } while (cursor.moveToNext())
            return ingredients
        }
        throw Exception("Error while populating recipe")
    }

    private fun populateIngredient(cursor: Cursor): Ingredient {
        try {
            val id: Int = cursor.getInt(cursor.getColumnIndexOrThrow(stepId))
            val amount: String = cursor.getString(cursor.getColumnIndexOrThrow(ingredientAmount))
            val unit: String = cursor.getString(cursor.getColumnIndexOrThrow(ingredientUnit))
            val ingredient: String =
                cursor.getString(cursor.getColumnIndexOrThrow(ingredientIngredient))
            val recipeId: Long = cursor.getLong(cursor.getColumnIndexOrThrow(stepRecipeId))
            return Ingredient(
                id = id,
                amount = amount,
                unit = unit,
                ingredient = ingredient,
                recipeId = recipeId
            )
        } catch (e: Exception) {
            throw Exception("Error while retrieving origins")
        }
    }

    fun beginTransaction(db: SQLiteDatabase) {
        db.beginTransaction()
    }

    fun commitTransaction(db: SQLiteDatabase) {
        db.setTransactionSuccessful()
    }

    fun endTransaction(db: SQLiteDatabase) {
        db.endTransaction()
    }

    fun closeDatabase(db: SQLiteDatabase) {
        db.close()
    }

}