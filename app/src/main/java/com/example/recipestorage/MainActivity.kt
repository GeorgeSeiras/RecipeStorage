package com.example.recipestorage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
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
        val db = DatabaseHandler(this)

        val newRecipeButton: FrameLayout = findViewById(R.id.fl_new_recipe_redirect)
        newRecipeButton.setOnClickListener {
            startActivity(Intent(this, NewRecipeActivity::class.java))
        }

        val searchTitleButton = findViewById<Button>(R.id.bt_search_recipes)
        searchTitleButton.setOnClickListener {
            searchByTitle(db)
        }

        val transaction = db.readableDatabase
        renderRecipes(
            db.getRecipes(transaction)
        )
        db.closeDatabase(transaction)
    }

    private fun searchByTitle(db: DatabaseHandler) {
        val titleEditText = findViewById<EditText>(R.id.et_search_title)
        val transaction = db.readableDatabase
        renderRecipes(
            db.getRecipeByTitle(titleEditText.text.toString(), transaction)
        )
        db.closeDatabase(transaction)
    }

    //remove previous recipes and re-render the new set of recipes
    private fun renderRecipes(recipes: ArrayList<Recipe>) {
        val recipeTable = findViewById<LinearLayout>(R.id.tl_recipes)
        recipeTable.removeAllViews()
        val inflater: LayoutInflater =
            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        for (recipe in recipes) {
            val row = TableRow(this)
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