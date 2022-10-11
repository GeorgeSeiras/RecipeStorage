package com.example.recipestorage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginEnd
import com.example.recipestorage.models.Recipe
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val newRecipeButton: FrameLayout = findViewById(R.id.fl_new_recipe_redirect)

        newRecipeButton.setOnClickListener {
            println("click")
            startActivity(Intent(this, NewRecipeActivity::class.java))
        }

        val db = DatabaseHandler(this)
        val transaction = db.readableDatabase
        val recipes: ArrayList<Recipe> = db.getRecipes(transaction)
        db.closeDatabase(transaction)

        val recipeTable = findViewById<LinearLayout>(R.id.tl_recipes)
        val inflater: LayoutInflater =
            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        for (recipe in recipes) {
            val row: TableRow = TableRow(this)
//            val tableRowParams = TableLayout.LayoutParams(
//                TableLayout.LayoutParams.WRAP_CONTENT,
//                TableLayout.LayoutParams.WRAP_CONTENT
//            )
//            tableRowParams.setMargins(10,0,10,0)
//            row.layoutParams = tableRowParams;
            val recipeLayout: View = inflater.inflate(R.layout.recipe_layout, null, false)
            recipeLayout.findViewById<TextView>(R.id.title).text = recipe.title
            recipeLayout.findViewById<TextView>(R.id.course).text = recipe.course
            recipeLayout.findViewById<TextView>(R.id.origin).text = recipe.origin
            val prepTimeMap = recipe.getPrepTime()
            recipeLayout.findViewById<TextView>(R.id.hours_prep).text = prepTimeMap["h"].toString()
            recipeLayout.findViewById<TextView>(R.id.minutes_prep).text =
                prepTimeMap["m"].toString()
            val cookTimeMap = recipe.getCookTime()
            recipeLayout.findViewById<TextView>(R.id.hours_cook).text = cookTimeMap["h"].toString()
            recipeLayout.findViewById<TextView>(R.id.minutes_cook).text =
                cookTimeMap["m"].toString()

            row.addView(recipeLayout)
            recipeTable.addView(
                row, TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT,

                    )
            )
        }
    }


}