package com.example.recipestorage

import android.Manifest.permission
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.recipestorage.models.Recipe
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import java.security.AccessController.getContext


class EditRecipeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_recipe)
        val db = DatabaseHandler(this)
        val transaction = db.readableDatabase
        val logoutButton = findViewById<Button>(R.id.btn_logout)
        logoutButton.setOnClickListener {
            Logout().logoutPopup(logoutButton, this)
        }
        val extras: Bundle? = intent.extras
        if (extras != null) {
            val recipe: Recipe = db.getRecipeById(extras.getLong("recipeId"))
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
            recipe.ingredients.forEachIndexed { index, ingredient ->
                addIngredient(
                    unit = ingredient.unit,
                    amount = ingredient.amount,
                    index = index
                )
            }
            recipe.steps.forEachIndexed { index, step ->
                addStep(step = step.step, index = index)
            }


            //ingredient dynamic table
            val ingredientButton = findViewById<Button>(R.id.button_ingredient)
            val ingredientTable = findViewById<TableLayout>(R.id.ingredient_table)
            ingredientButton.setOnClickListener {
                addIngredient(index = ingredientTable.childCount)
            }

            //step dynamic table
            val stepButton = findViewById<Button>(R.id.button_step)
            val stepTable = findViewById<TableLayout>(R.id.step_table)
            stepButton.setOnClickListener {
                addStep(index = stepTable.childCount)
            }

            val editButton = findViewById<Button>(R.id.button_edit_recipe)
            editButton.setOnClickListener {
                editRecipeListener(recipe, db, this, GoogleSignIn.getLastSignedInAccount(this))
            }
        }
    }


    private fun addIngredient(
        unit: String? = null,
        amount: String? = null,
        ingredient: String? = null,
        index: Int
    ) {
        val ingredientTable = findViewById<TableLayout>(R.id.ingredient_table)

        val tr = TableRow(this)
        tr.id = TableRow.generateViewId()
        tr.weightSum = 1f
        tr.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )

        val amountText = EditText(this)
        amountText.id = TableRow.generateViewId()
        amountText.hint = "Amount"
        if (amount != null) {
            amountText.setText(" $amount")
        }
        amountText.setHintTextColor(Color.parseColor("#757575"))
        amountText.setTextColor(Color.parseColor("#000000"))
        amountText.background.setTint(Color.parseColor("#000000"))
        amountText.inputType = InputType.TYPE_CLASS_TEXT
        amountText.layoutParams = TableRow.LayoutParams(
            0,
            TableLayout.LayoutParams.WRAP_CONTENT,
            0.2f
        )
        tr.addView(amountText)

        val unitText = EditText(this)
        unitText.id = TableRow.generateViewId()
        unitText.hint = "Unit"
        if (unit != null) {
            unitText.setText(" $unit")
        }
        unitText.setHintTextColor(Color.parseColor("#757575"))
        unitText.setTextColor(Color.parseColor("#000000"))
        unitText.background.setTint(Color.parseColor("#000000"))
        unitText.inputType = InputType.TYPE_CLASS_TEXT
        unitText.layoutParams = TableRow.LayoutParams(
            0,
            TableRow.LayoutParams.WRAP_CONTENT,
            0.1f
        )
        tr.addView(unitText)

        val ingredientText = EditText(this)
        ingredientText.id = TableRow.generateViewId()
        ingredientText.hint = "Ingredient"
        if (unit != null) {
            ingredientText.setText(" $ingredient")
        }
        ingredientText.setHintTextColor(Color.parseColor("#757575"))
        ingredientText.setTextColor(Color.parseColor("#000000"))
        ingredientText.background.setTint(Color.parseColor("#000000"))
        ingredientText.inputType = InputType.TYPE_CLASS_TEXT
        ingredientText.layoutParams = TableRow.LayoutParams(
            0,
            TableRow.LayoutParams.WRAP_CONTENT,
            0.5f
        )
        tr.addView(ingredientText)

        if (index > 0) {
            val btn = Button(this)
            btn.text = "X"
            btn.background.setTint(Color.parseColor("#C62828"))
            btn.layoutParams = TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.MATCH_PARENT,
                0.1f
            )
            btn.setOnClickListener {
                ingredientTable.removeView(tr)
            }
            tr.addView(btn)
        }
        ingredientTable.addView(
            tr, TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun addStep(step: String? = null, index: Int) {
        val stepTable = findViewById<TableLayout>(R.id.step_table)

        val tr = TableRow(this)
        tr.id = TableRow.generateViewId()
        tr.weightSum = 1f
        tr.layoutParams = TableRow.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            TableLayout.LayoutParams.WRAP_CONTENT
        )

        val stepText = EditText(this)
        stepText.id = TableRow.generateViewId()
        stepText.hint = "Step"
        stepText.maxLines = 10
        stepText.minLines = 1
        stepText.setHintTextColor(Color.parseColor("#757575"))
        if (step != null) {
            stepText.setText(" $step")
        }
        stepText.setTextColor(Color.parseColor("#000000"))
        stepText.background.setTint(Color.parseColor("#000000"))
        stepText.isSingleLine = false
        stepText.layoutParams = TableRow.LayoutParams(
            0,
            TableRow.LayoutParams.WRAP_CONTENT,
            0.8f
        )
        tr.addView(stepText)
        if (index > 0) {
            val btn = Button(this)
            btn.text = "X"
            btn.background.setTint(Color.parseColor("#C62828"))
            btn.layoutParams = TableRow.LayoutParams(
                0,
                TableLayout.LayoutParams.MATCH_PARENT,
                0.1f
            )
            btn.setOnClickListener {
                stepTable.removeView(tr)
            }
            tr.addView(btn)
        }
        stepTable.addView(
            tr, TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun editRecipeListener(
        original: Recipe, db: DatabaseHandler, context: Context,
        account: GoogleSignInAccount?
    ) {
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
            db.updateRecipe(
                title = title.text.toString(),
                course = course.text.toString(),
                origin = origin.text.toString(),
                prepTime = prepTime,
                cookTime = cookTime,
                originalRecipe = original,
                transaction
            )
            db.deleteRecipeIngredients(original.id, transaction)
            for (i in 0 until ingredientTable.childCount) {
                val row = ingredientTable.getChildAt(i) as TableRow
                db.addIngredient(
                    amount = (row.getChildAt(0) as EditText).text.toString(),
                    unit = (row.getChildAt(1) as EditText).text.toString(),
                    ingredient = (row.getChildAt(2) as EditText).text.toString(),
                    recipeId = original.id,
                    db = transaction
                )
            }

            db.deleteRecipeSteps(original.id, transaction)
            for (i in 0 until stepTable.childCount) {
                val row = stepTable.getChildAt(i) as TableRow
                db.addStep(
                    step = (row.getChildAt(0) as EditText).text.toString(),
                    recipeId = original.id,
                    db = transaction
                )
            }

            db.commitTransaction(transaction)
            db.endTransaction(transaction)

//          sync db with drive
            val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            var isConnected: Boolean = false
            isConnected = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cm.getActiveNetwork() != null && cm.getNetworkCapabilities(cm.getActiveNetwork()) != null;
            } else {
                cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo()!!
                    .isConnectedOrConnecting();
            }
            if (isConnected) {
                val driveHandler = GoogleDriveHandler(
                    context,
                    account
                )
                driveHandler.syncDb(context)
            }

            db.closeDatabase(transaction)

            //return to home page with success toast
            val intent = Intent(this, RecipeViewActivity::class.java)
            intent.putExtra("message", "Recipe Successfully Updated")
            intent.putExtra("recipeId", original.id)
            startActivity(intent)
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