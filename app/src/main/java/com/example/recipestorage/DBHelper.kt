package com.example.recipestorage

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.recipestorage.models.*
import java.util.*

class DatabaseHandler(context: Context) :
    SQLiteOpenHelper(context, databaseName, null, databaseVersion) {
    companion object {
        private val databaseVersion = 1
        private val databaseName = "RecipeDatabase"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(userCreateQuery)
        db.execSQL(tokenCreateQuery)
        db.execSQL(recipeCreateQuery)
        db.execSQL(stepCreateQuery)
        db.execSQL(ingredientCreateQuery)
        db.execSQL(lModifiedCreateQuery)

        val cursor: Cursor =
            db.query(lModifiedTableName, null, null, arrayOf(), null, null, null, null)
        if (!cursor.moveToFirst()) {
            val contentValues = ContentValues()
            contentValues.put(lModifiedLastModified, 0)
            db.insert(lModifiedTableName, null, contentValues)
            cursor.close()
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $recipeTableName")
        db.execSQL("DROP TABLE IF EXISTS $stepTableName")
        db.execSQL("DROP TABLE IF EXISTS $ingredientTableName")
        db.execSQL("DROP TABLE IF EXISTS $lModifiedTableName")
        db.execSQL("DROP TABLE IF EXISTS $tokenTableName")
        db.execSQL("DROP TABLE IF EXISTS $userTableName")
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
        try {
            val contentValues = ContentValues()

            contentValues.put(recipeTitle, title)
            contentValues.put(recipePrepTime, prepTime)
            contentValues.put(recipeCookTime, cookTime)
            contentValues.put(recipeCourse, course)
            contentValues.put(recipeOrigin, origin)

            val res = db.insert(recipeTableName, null, contentValues)
            updateLModified()
            return res
        } catch (e: Exception) {
            return -1
        }
    }

    fun removeRecipe(
        id: Long,
        db: SQLiteDatabase
    ): Int {
        val tableName = recipeTableName
        val whereArgs = "$recipeId = ?"
        val whereClause = arrayOf("$id")
        return db.delete(tableName, whereArgs, whereClause)
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
        try {
            val contentValues = ContentValues()

            contentValues.put(recipeTitle, title)
            contentValues.put(recipePrepTime, prepTime)
            contentValues.put(recipeCookTime, cookTime)
            contentValues.put(recipeCourse, course)
            contentValues.put(recipeOrigin, origin)

            val table: String = recipeTableName
            val res = db.update(
                table,
                contentValues,
                "$recipeId = ?",
                arrayOf(originalRecipe.id.toString())
            )
            updateLModified()
            return res
        } catch (e: Exception) {
            return -1
        }
    }

    private fun populateRecipe(
        cursor: Cursor,
        db: SQLiteDatabase
    ): Recipe {

        return Recipe(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(recipeId)),
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
            + " foreign key($stepRecipeId) references recipe($recipeId) ON DELETE CASCADE"
            + ");")

    private fun populateStep(cursor: Cursor): Step {
        try {
            val id: Long = cursor.getLong(cursor.getColumnIndexOrThrow(stepId))
            val step: String = cursor.getString(cursor.getColumnIndexOrThrow(stepStep))
            val recipeId: Long = cursor.getLong(cursor.getColumnIndexOrThrow(stepRecipeId))
            return Step(id = id, step = step, recipeId = recipeId)
        } catch (e: Exception) {
            Log.e("ERROR", e.toString())
            throw Exception("Error while retrieving steps")
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

        val res = db.insertOrThrow(stepTableName, null, contentValues)
        return res
    }

    fun deleteRecipeSteps(
        id: Long,
        db: SQLiteDatabase
    ): Int {
        return db.delete(stepTableName, "$stepRecipeId=?", arrayOf("$id"))
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
            + " foreign key($ingredientRecipeId) references recipe($recipeId) ON DELETE CASCADE"
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

        val res = db.insert(ingredientTableName, null, contentValues)
        return res
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
        updateLModified()
        return results
    }

    fun deleteRecipeIngredients(
        id: Long,
        db: SQLiteDatabase
    ): Int {
        val res = db.delete(ingredientTableName, "$ingredientRecipeId=?", arrayOf("$id"))
        return res
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
            val id: Long = cursor.getLong(cursor.getColumnIndexOrThrow(stepId))
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
            throw Exception("Error while retrieving ingredients")
        }
    }

    //LastModified Table
    private val lModifiedTableName = "lastmodified"
    private val lModifiedId = "id"
    private val lModifiedLastModified = "last_modified"
    private val lModifiedCreateQuery = ("create table if not exists " + lModifiedTableName + " ("
            + lModifiedId + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + lModifiedLastModified + " INTEGER"
            + ");")

    fun getLastModified(): Long {
        val db = readableDatabase
        val table = lModifiedTableName
        val columns = null
        val selection = null
        val selectionArgs: Array<String> = arrayOf()
        val groupBy: String? = null
        val having: String? = null
        val orderBy = null
        val limit = null
        val cursor: Cursor =
            db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
        if (cursor.moveToFirst()) {
            val res = cursor.getLong(cursor.getColumnIndexOrThrow(lModifiedLastModified))
            return res
        }
        throw Exception("Last modified entry not found")
    }

    fun updateLModified(): Int {
        try {
            val lModified: Long = System.currentTimeMillis() / 1000L;
            val db = writableDatabase
            val table = lModifiedTableName
            val columns = null
            val selection = null
            val selectionArgs: Array<String> = arrayOf()
            val groupBy: String? = null
            val having: String? = null
            val orderBy = null
            val limit = null
            val cursor: Cursor =
                db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
            var id: Long = -1
            if (cursor.moveToFirst()) {
                id = cursor.getLong(cursor.getColumnIndexOrThrow(lModifiedId))
            }
            if (id == -1L) {
                cursor.close()
                throw Exception("Last modified entry not found")
            }
            cursor.close()
            val contentValues = ContentValues()
            contentValues.put(lModifiedLastModified, lModified)
            return db.update(
                table,
                contentValues,
                "$lModifiedId = ?",
                arrayOf(id.toString())
            )
        } catch (e: Exception) {
            return -1
        }
    }

    //User Table
    private val userTableName = "user"
    private val userId = "id"
    private val userEmail = "email"
    private val userGId = "gid"
    private val userCreateQuery = ("create table if not exists " + userTableName + " ("
            + userId + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + userEmail + " TEXT NOT NULL UNIQUE,"
            + userGId + " TEXT NOT NULL UNIQUE"
            + ");")

    fun addUser(email: String, gid: String): User {
        val db = writableDatabase
        val contentValues = ContentValues()
        contentValues.put(userEmail, email)
        contentValues.put(userGId, gid)
        val id = db.insertOrThrow(
            userTableName,
            null,
            contentValues
        )
        val cursor =
            db.query(userTableName, null, "$userId = ?", arrayOf(id.toString()), null, null, null)
        if (cursor.moveToFirst()) {
            db.close()
            return populateUser(cursor)
            db.close()
        }
        throw Exception("Something went wrong while creating the user")
    }

    fun updateUser(email: String, gid: String): User? {
        val db = writableDatabase
        val contentValues = ContentValues()
        contentValues.put(userEmail, email)
        db.update(userTableName, contentValues, "$userGId = ?", arrayOf(gid))
        db.close()
        return getUserByGId(gid)
    }

    private fun populateUser(cursor: Cursor): User {
        return User(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(userId)),
            gid = cursor.getString(cursor.getColumnIndexOrThrow(userGId)),
            email = cursor.getString(cursor.getColumnIndexOrThrow(userEmail)),
        )
    }

    fun getUserByGId(gid: String): User? {
        val db = readableDatabase
        val cursor: Cursor =
            db.query(userTableName, null, "$userGId = ?", arrayOf(gid), null, null, null, null)
        if (cursor.moveToFirst()) {
            db.close()
            return populateUser(cursor)
        }
        db.close()
        return null
    }

    fun getUserByEmail(email: String): User? {
        val db = readableDatabase
        val cursor: Cursor =
            db.query(userTableName, null, "$userEmail = ?", arrayOf(email), null, null, null, null)
        if (cursor.moveToFirst()) {
            return populateUser(cursor)
        }
        return null
    }


    //AccessToken Table
    private val tokenTableName = "token"
    private val tokenId = "id"
    private val tokenRefresh = "refresh"
    private val tokenAccess = "token"
    private val tokenExpiry = "expiry"
    private val tokenUserId = "user_id"
    private val tokenCreateQuery = ("create table if not exists " + tokenTableName + " ("
            + tokenId + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + tokenRefresh + " TEXT NOT NULL,"
            + tokenAccess + " TEXT NOT NULL,"
            + tokenExpiry + " TEXT NOT NULL,"
            + tokenUserId + " INTEGER NOT NULL,"
            + " foreign key($tokenUserId) references $userTableName($userId)"
            + ");")

    fun addToken(
        token: String,
        refresh: String,
        expiresIn: Int,
        userId: Long
    ): Long {
        val db = writableDatabase
        val contentValues = ContentValues()
        contentValues.put(tokenAccess, token)
        contentValues.put(tokenRefresh, refresh)
        contentValues.put(tokenUserId, userId)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.SECOND, expiresIn)
        contentValues.put(tokenExpiry, "${calendar.timeInMillis / 1000}")
        return db.insertOrThrow(
            tokenTableName,
            null,
            contentValues
        )
        db.close()
    }

    fun updateToken(token: String, refresh: String?, expiresIn: Int, tokenId: Long): Int {
        val db = writableDatabase
        val contentValues = ContentValues()
        contentValues.put(tokenAccess, token)
        if (refresh != null) {
            contentValues.put(tokenRefresh, refresh)
        }
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.SECOND, expiresIn)
        contentValues.put(tokenExpiry, calendar.timeInMillis / 1000)
        return db.update(
            tokenTableName,
            contentValues,
            "id = ?",
            arrayOf(tokenId.toString())
        )
    }

    fun getTokenOfUser(id: Long): Token? {
        val db = readableDatabase
        val query =
            "SELECT * FROM $tokenTableName " +
                    "INNER JOIN $userTableName " +
                    "ON $tokenTableName.${tokenUserId}=${userTableName}.${userId} WHERE ${userTableName}.${userId}=?"
        val cursor: Cursor = db.rawQuery(query, arrayOf(id.toString()));
        return if (cursor.moveToFirst()) {
            populateToken(cursor)
        } else {
            null
        }
    }

    private fun populateToken(cursor: Cursor): Token {
        return Token(
            cursor.getLong(cursor.getColumnIndexOrThrow(tokenId)),
            cursor.getString(cursor.getColumnIndexOrThrow(tokenAccess)),
            cursor.getString(cursor.getColumnIndexOrThrow(tokenRefresh)),
            cursor.getInt(cursor.getColumnIndexOrThrow(tokenExpiry))
        )
    }

    fun getToken(id: Long): Token {
        val db = readableDatabase
        val cursor: Cursor =
            db.query(
                tokenTableName,
                null,
                "$tokenId = ?",
                arrayOf(id.toString()),
                null,
                null,
                null,
                null
            )
        if (cursor.moveToFirst()) {
            return populateToken(cursor)
        }
        throw Exception("Error while retrieving token")
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