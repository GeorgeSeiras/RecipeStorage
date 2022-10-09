package com.example.recipestorage

import android.os.Bundle
import android.graphics.Color.parseColor
import android.text.InputType.TYPE_CLASS_TEXT
import android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
import android.widget.*
import android.widget.TableRow.LayoutParams
import android.widget.TableRow.generateViewId
import androidx.appcompat.app.AppCompatActivity
import com.example.recipestorage.models.Ingredient


class NewRecipeActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_recipe)
        val db = DatabaseHandler(this)

        //ingredient dynamic table
        val ingredientTable: TableLayout = findViewById(R.id.ingredient_table)
        val ingredientButton = findViewById<Button>(R.id.button_ingredient)
        var rowIdIngredient = 1

        ingredientButton.setOnClickListener {
            val tr = TableRow(this)
            tr.id = generateViewId()
            tr.tag = "ingredient_table_row_$rowIdIngredient"
            tr.layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )

            val amount = EditText(this)
            amount.id = generateViewId()
            amount.tag = "amount_$rowIdIngredient"
            amount.hint = "Amount"
            amount.setHintTextColor(parseColor("#757575"))
            amount.inputType = TYPE_CLASS_TEXT
            amount.layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
            tr.addView(amount)

            val unit = EditText(this)
            unit.id = generateViewId()
            unit.tag = "amount${rowIdIngredient}"
            unit.hint = "Unit"
            unit.setHintTextColor(parseColor("#757575"))
            unit.inputType = TYPE_CLASS_TEXT
            unit.layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
            tr.addView(unit)
            val ingredient = EditText(this)
            ingredient.id = generateViewId()
            ingredient.tag = "amount${rowIdIngredient}"
            ingredient.hint = "Ingredient"
            ingredient.setHintTextColor(parseColor("#757575"))
            ingredient.inputType = TYPE_CLASS_TEXT
            ingredient.layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
            tr.addView(ingredient)

            ingredientTable.addView(
                tr, LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT
                )
            )

            rowIdIngredient++
        }

        //step dynamic table
        val stepTable: TableLayout = findViewById(R.id.step_table)
        val stepButton = findViewById<Button>(R.id.button_step)
        var rowIdStep = 1

        stepButton.setOnClickListener {
            val tr = TableRow(this)
            tr.id = generateViewId()
            tr.tag = "step_table_row_$rowIdIngredient"
            tr.layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )

            val step = EditText(this)
            step.id = generateViewId()
            step.tag = "step_$rowIdStep"
            step.hint = "Step"
            step.maxLines = 10
            step.minLines = 1
            step.setHintTextColor(parseColor("#757575"))
            step.isSingleLine = false
            step.layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )

            tr.addView(step)
            stepTable.addView(
                tr, LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT
                )
            )

            rowIdStep++
        }

    }

}