package com.example.recipestorage

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.recipestorage.models.Ingredient
import com.example.recipestorage.models.Recipe
import com.example.recipestorage.models.Step

class RecipeViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_view)

        val editButton = findViewById<Button>(R.id.bt_edit)
        editButton.setOnClickListener {
            editButtonListener()
        }

        val deleteButton = findViewById<Button>(R.id.bt_delete)
        deleteButton.setOnClickListener {
            deleteButtonListener()
        }

        val db = DatabaseHandler(this)
        val transaction = db.readableDatabase
        val extras: Bundle? = intent.extras
        if (extras != null) {
            val recipe: Recipe = db.getRecipeById(extras.getInt("recipeId").toLong(), transaction)
            db.closeDatabase(transaction)

            findViewById<TextView>(R.id.title).text = recipe.title
            findViewById<TextView>(R.id.Course).text = recipe.course
            findViewById<TextView>(R.id.Origin).text = recipe.origin
            val prepTime: Map<String, Int> = recipe.getPrepTime()
            findViewById<TextView>(R.id.hours_prep).text = prepTime["h"].toString()
            findViewById<TextView>(R.id.minutes_prep).text = prepTime["m"].toString()
            val cookTime: Map<String, Int> = recipe.getCookTime()
            findViewById<TextView>(R.id.hours_cook).text = cookTime["h"].toString()
            findViewById<TextView>(R.id.minutes_cook).text = cookTime["m"].toString()
            renderIngredients(recipe.ingredients)
            renderSteps(recipe.steps)
        }

    }

    private fun editButtonListener() {

    }

    private fun deleteButtonListener() {

    }

    private fun renderIngredients(ingredients: ArrayList<Ingredient>) {
        val ingredientTable = findViewById<TableLayout>(R.id.ingredient_table)
        val border = ResourcesCompat.getDrawable(resources, R.drawable.border, null)
        for (ingredient in ingredients) {
            val row = TableRow(this)
            row.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT,
            )
            val amountView = TextView(this)

            amountView.text = " ${ingredient.amount}"
            amountView.setTextColor(Color.parseColor("#000000"))
            amountView.background = border
            amountView.layoutParams = TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                0.15f,
            )
            row.addView(amountView)
            val unitView = TextView(this)
            unitView.text = " ${ingredient.unit}"
            unitView.setTextColor(Color.parseColor("#000000"))
            unitView.background = border
            unitView.layoutParams = TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                0.15f
            )
            row.addView(unitView)
            val ingredientView = TextView(this)
            ingredientView.text = " ${ingredient.ingredient}"
            ingredientView.setTextColor(Color.parseColor("#000000"))
            ingredientView.background = border
            ingredientView.layoutParams = TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                0.7f
            )
            row.addView(ingredientView)

            ingredientTable.addView(
                row, TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )
            )
        }
    }

    private fun renderSteps(steps: ArrayList<Step>) {
        val stepTable = findViewById<TableLayout>(R.id.step_table)
        val border = ResourcesCompat.getDrawable(resources, R.drawable.border, null)
        for ((i, step) in steps.withIndex()) {
            val row = TableRow(this)
            row.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
            val indexView = TextView(this)
            indexView.text = " ${i.toString()}"
            indexView.setTextColor(Color.parseColor("#000000"))
            indexView.background = border
            indexView.gravity = Gravity.CENTER_HORIZONTAL
            indexView.layoutParams = TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                10f
            )
            row.addView(indexView)
            val stepView = TextView(this)
            stepView.background = border
            stepView.setTextColor(Color.parseColor("#000000"))
            stepView.text = " ${step.step}"
            stepView.maxLines = 10
            stepView.minLines = 1
            stepView.isSingleLine = false
            stepView.layoutParams = TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                90f
            )
            row.addView(stepView)

            stepTable.addView(
                row, TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )
            )
        }
    }
}