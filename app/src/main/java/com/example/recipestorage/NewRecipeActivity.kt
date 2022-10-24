package com.example.recipestorage

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Color.parseColor
import android.os.Bundle
import android.text.InputType.TYPE_CLASS_TEXT
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import android.widget.TableRow.LayoutParams
import android.widget.TableRow.generateViewId
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn


class NewRecipeActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_recipe)
        val db = DatabaseHandler(this)

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

        val createButton = findViewById<Button>(R.id.button_edit_recipe)
        createButton.setOnClickListener {
            createRecipeButtonListener(db)
        }
    }

    private fun ingredientButtonListener(context: Context, rowIdIngredient: Int) {
        val ingredientTable = findViewById<TableLayout>(R.id.ingredient_table)

        val tr = TableRow(context)
        tr.id = generateViewId()
        tr.tag = "ingredient_table_row_$rowIdIngredient"
        tr.weightSum = 1f
        tr.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )

        val amount = EditText(context)
        amount.id = generateViewId()
        amount.tag = "amount_$rowIdIngredient"
        amount.hint = "Amount"
        amount.setHintTextColor(parseColor("#757575"))
        amount.setTextColor(parseColor("#000000"))
        amount.background.setTint(parseColor("#000000"))
        amount.inputType = TYPE_CLASS_TEXT
        amount.layoutParams = LayoutParams(
            0,
            LayoutParams.WRAP_CONTENT,
            0.2f
        )
        tr.addView(amount)

        val unit = EditText(context)
        unit.id = generateViewId()
        unit.tag = "amount${rowIdIngredient}"
        unit.hint = "Unit"
        unit.setHintTextColor(parseColor("#757575"))
        unit.setTextColor(parseColor("#000000"))
        unit.background.setTint(parseColor("#000000"))
        unit.inputType = TYPE_CLASS_TEXT
        unit.layoutParams = LayoutParams(
            0,
            LayoutParams.WRAP_CONTENT,
            0.1f
        )
        tr.addView(unit)

        val ingredient = EditText(context)
        ingredient.id = generateViewId()
        ingredient.tag = "amount${rowIdIngredient}"
        ingredient.hint = "Ingredient"
        ingredient.setHintTextColor(parseColor("#757575"))
        ingredient.setTextColor(parseColor("#000000"))
        ingredient.background.setTint(parseColor("#000000"))
        ingredient.inputType = TYPE_CLASS_TEXT
        ingredient.layoutParams = LayoutParams(
            0,
            LayoutParams.MATCH_PARENT,
            0.5f
        )
        tr.addView(ingredient)

        val btn = Button(context)
        btn.text = "X"
        btn.background.setTint(parseColor("#C62828"))
        btn.layoutParams = LayoutParams(
            0,
            LayoutParams.MATCH_PARENT,
            0.1f
        )
        btn.setOnClickListener {
            ingredientTable.removeView(tr)
        }
        tr.addView(btn)

        ingredientTable.addView(
            tr, LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        )
    }

    private fun stepButtonListener(context: Context, rowIdStep: Int) {
        val stepTable = findViewById<TableLayout>(R.id.step_table)

        val tr = TableRow(context)
        tr.id = generateViewId()
        tr.tag = "step_table_row_$rowIdStep"
        tr.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )

        val step = EditText(context)
        step.id = generateViewId()
        step.tag = "step_$rowIdStep"
        step.hint = "Step"
        step.maxLines = 10
        step.minLines = 1
        step.setHintTextColor(parseColor("#757575"))
        step.setTextColor(parseColor("#000000"))
        step.background.setTint(parseColor("#000000"))
        step.isSingleLine = false
        step.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        val btn = Button(context)
        btn.text = "X"
        btn.background.setTint(parseColor("#C62828"))
        btn.layoutParams = LayoutParams(
            0,
            LayoutParams.MATCH_PARENT,
            0.1f
        )
        btn.setOnClickListener {
            stepTable.removeView(tr)
        }
        tr.addView(btn)

        tr.addView(step)
        stepTable.addView(
            tr, LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun createRecipeButtonListener(db: DatabaseHandler) {
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

            val driveHandler = GoogleDriveHandler(
                this,
                GoogleSignIn.getLastSignedInAccount(this)
            )
            driveHandler.syncDb(this)
            db.closeDatabase(transaction)
            val intent = Intent(this, HomePageActivity::class.java)
            intent.putExtra("message", "Recipe Successfully Created")
            startActivity(intent)

        }


    }

    private fun deleteRowPopup(row: TableRow, table: TableLayout): Boolean {
        Log.v("TEST", "LONG PRESS")
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(R.layout.popup_deletion_confirmation, null)

        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val focusable = true

        val popupWindow = PopupWindow(popupView, width, height, focusable)
        val parent = findViewById<ScrollView>(R.id.sv_recipe_view)
        popupWindow.showAtLocation(parent, Gravity.CENTER, 0, 0)

        val cancelButton: Button = popupView.findViewById(R.id.bt_cancel)
        cancelButton.setOnClickListener {
            popupWindow.dismiss()
        }
        val deleteButton: Button = popupView.findViewById(R.id.bt_delete_confirm)
        deleteButton.setOnClickListener {
            table.removeView(row)
            popupWindow.dismiss()
        }
        return true
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