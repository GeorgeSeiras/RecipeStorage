package com.example.recipestorage

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
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

class EditRecipeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_recipe)
        val db = DatabaseHandler(this)
        val transaction = db.readableDatabase

        //ingredient dynamic table
        val ingredientButton = findViewById<Button>(R.id.button_ingredient)
        var rowIdIngredient = 1
        ingredientButton.setOnClickListener {
            ingredientButtonListener(this, rowIdIngredient)
            rowIdIngredient++
        }

        //step dynamic table
        val stepButton = findViewById<Button>(R.id.button_step)
        var rowIdStep = 1
        stepButton.setOnClickListener {
            stepButtonListener(this, rowIdStep)
            rowIdStep++
        }

        val editButton = findViewById<Button>(R.id.button_edit_recipe)
        editButton.setOnClickListener {
            editRecipeListener(db)
        }


        val extras: Bundle? = intent.extras
        if (extras != null) {
            val recipe: Recipe = db.getRecipeById(extras.getInt("recipeId").toLong(), transaction)
            db.closeDatabase(transaction)
            findViewById<EditText>(R.id.title_edit).setText(recipe.title)
            findViewById<EditText>(R.id.course).setText(recipe.course)
            findViewById<EditText>(R.id.origin).setText(recipe.origin)
            val prepTime: HashMap<String, Int> = recipe.getPrepTime()
            findViewById<EditText>(R.id.prep_time_h).setText(prepTime["h"].toString())
            findViewById<EditText>(R.id.prep_time_m).setText(prepTime["m"].toString())
            val cookTime: HashMap<String, Int> = recipe.getCookTime()
            findViewById<EditText>(R.id.cook_time_h).setText(cookTime["h"].toString())
            findViewById<EditText>(R.id.cook_time_m).setText(cookTime["m"].toString())
            renderIngredients(recipe.ingredients)
            renderSteps(recipe.steps)

        }
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
            val amountView = EditText(this)
            amountView.setText(ingredient.amount)
            amountView.setTextColor(Color.parseColor("#000000"))
            amountView.layoutParams = TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                0.15f,
            )
            row.addView(amountView)

            val unitView = EditText(this)
            unitView.setText(ingredient.unit)
            unitView.setTextColor(Color.parseColor("#000000"))
            unitView.layoutParams = TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                0.15f
            )
            row.addView(unitView)

            val ingredientView = EditText(this)
            ingredientView.setText(ingredient.ingredient)
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
            val stepView = EditText(this)
            stepView.background = border
            stepView.setTextColor(Color.parseColor("#000000"))
            stepView.setText(step.step)
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

    private fun ingredientButtonListener(context: Context, rowIdIngredient: Int) {
        val ingredientTable = findViewById<TableLayout>(R.id.ingredient_table)

        val tr = TableRow(context)
        tr.id = TableRow.generateViewId()
        tr.tag = "ingredient_table_row_$rowIdIngredient"
        tr.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )

        val amount = EditText(context)
        amount.id = TableRow.generateViewId()
        amount.tag = "amount_$rowIdIngredient"
        amount.hint = "Amount"
        amount.setHintTextColor(Color.parseColor("#757575"))
        amount.inputType = InputType.TYPE_CLASS_TEXT
        amount.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        tr.addView(amount)

        val unit = EditText(context)
        unit.id = TableRow.generateViewId()
        unit.tag = "amount${rowIdIngredient}"
        unit.hint = "Unit"
        unit.setHintTextColor(Color.parseColor("#757575"))
        unit.inputType = InputType.TYPE_CLASS_TEXT
        unit.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        tr.addView(unit)
        val ingredient = EditText(context)
        ingredient.id = TableRow.generateViewId()
        ingredient.tag = "amount${rowIdIngredient}"
        ingredient.hint = "Ingredient"
        ingredient.setHintTextColor(Color.parseColor("#757575"))
        ingredient.inputType = InputType.TYPE_CLASS_TEXT
        ingredient.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        tr.addView(ingredient)

        ingredientTable.addView(
            tr, TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun stepButtonListener(context: Context, rowIdStep: Int) {
        val stepTable = findViewById<TableLayout>(R.id.step_table)

        val tr = TableRow(context)
        tr.id = TableRow.generateViewId()
        tr.tag = "step_table_row_$rowIdStep"
        tr.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )

        val step = EditText(context)
        step.id = TableRow.generateViewId()
        step.tag = "step_$rowIdStep"
        step.hint = "Step"
        step.maxLines = 10
        step.minLines = 1
        step.setHintTextColor(Color.parseColor("#757575"))
        step.isSingleLine = false
        step.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )

        tr.addView(step)
        stepTable.addView(
            tr, TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun editRecipeListener(db: DatabaseHandler) {
        val transaction = db.writableDatabase
        val title = findViewById<EditText>(R.id.title_edit)
        val course = findViewById<EditText>(R.id.course)
        val origin = findViewById<EditText>(R.id.origin)
        val prepTimeH = findViewById<EditText>(R.id.prep_time_h)
        val prepTimeM = findViewById<EditText>(R.id.prep_time_m)
        val cookTimeH = findViewById<EditText>(R.id.cook_time_h)
        val cookTimeM = findViewById<EditText>(R.id.cook_time_m)
        val ingredientTable = findViewById<TableLayout>(R.id.ingredient_table)
        val stepTable = findViewById<TableLayout>(R.id.step_table)

        if (fieldValidation()) {
            val transaction = db.writableDatabase
            db.beginTransaction(transaction)
            var prepTime = 0
            var cookTime = 0
            if (prepTimeH.length() != 0) {
                prepTime += Integer.parseInt(prepTimeH.text.toString()) * 60 * 60
            }
            if (prepTimeM.length() != 0) {
                prepTime += Integer.parseInt(prepTimeM.text.toString()) * 60
            }
            if (cookTimeH.length() != 0) {
                cookTime += Integer.parseInt(cookTimeH.text.toString()) * 60 * 60
            }
            if (cookTimeM.length() != 0) {
                cookTime += Integer.parseInt(cookTimeM.text.toString()) * 60
            }
            val recipeId: Long = db.addRecipe(
                title = title.text.toString(),
                course = course.text.toString(),
                origin = origin.text.toString(),
                prepTime = prepTime,
                cookTime = cookTime,
                transaction
            )

            for (i in 0 until ingredientTable.childCount) {
                val row = ingredientTable.getChildAt(i) as TableRow

                db.addIngredient(
                    amount = (row.getChildAt(0) as EditText).text.toString(),
                    unit = (row.getChildAt(1) as EditText).text.toString(),
                    ingredient = (row.getChildAt(2) as EditText).text.toString(),
                    recipeId = recipeId,
                    db = transaction
                )
            }

            for (i in 0 until stepTable.childCount) {
                val row = stepTable.getChildAt(i) as TableRow
                db.addStep(
                    step = (row.getChildAt(0) as EditText).text.toString(),
                    recipeId = recipeId,
                    db = transaction
                )
            }

            db.commitTransaction(transaction)
            db.endTransaction(transaction)
            db.closeDatabase(transaction)
//            startActivity(Intent(this, MainActivity::class.java))

        }
    }

    private fun fieldValidation(): Boolean {
        val title = findViewById<EditText>(R.id.title_edit)
        val course = findViewById<EditText>(R.id.course)
        val origin = findViewById<EditText>(R.id.origin)
        val ingredientTable = findViewById<TableLayout>(R.id.ingredient_table)
        val stepTable = findViewById<TableLayout>(R.id.step_table)
        val prepTimeH = findViewById<EditText>(R.id.prep_time_h)
        val prepTimeM = findViewById<EditText>(R.id.prep_time_m)
        val cookTimeH = findViewById<EditText>(R.id.cook_time_h)
        val cookTimeM = findViewById<EditText>(R.id.cook_time_m)
        var success = true

        if (title.length() == 0) {
            title.error = "Title is required"
            success = false
        }
        if (course.length() == 0) {
            course.error = "Course is required"
            success = false
        }
        if (origin.length() == 0) {
            origin.error = "Origin is required"
            success = false
        }
        if (prepTimeH.length() == 0 && prepTimeM.length() == 0) {
            prepTimeH.error = "Prep Time is required"
            prepTimeM.error = "Prep Time is required"
        }
        if (cookTimeH.length() == 0 && cookTimeM.length() == 0) {
            cookTimeH.error = "Cook Time is required"
            cookTimeM.error = "Cook Time is required"
        }
        for (i in 0 until ingredientTable.childCount) {
            val row: TableRow = ingredientTable.getChildAt(i) as TableRow
            val ingredient: EditText = row.getChildAt(2) as EditText
            if (ingredient.length() == 0) {
                ingredient.error = "Ingredient is required"
                success = false
            }
        }
        for (i in 0 until stepTable.childCount) {
            val row: TableRow = stepTable.getChildAt(i) as TableRow
            val step: EditText = row.getChildAt(0) as EditText
            if (step.length() == 0) {
                step.error = "Step is required"
                success = false
            }
        }
        return success
    }
}