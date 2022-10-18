package com.example.recipestorage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.recipestorage.models.Recipe

class HomePageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val db = DatabaseHandler(this)

        val extras: Bundle? = intent.extras
        if (extras != null) {
            if (extras.containsKey("message")) {
                Toast.makeText(this, extras.get("message").toString(), Toast.LENGTH_LONG).show()
            }
        }
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
        val courseEditText = findViewById<EditText>(R.id.et_search_course)
        val originEditText = findViewById<EditText>(R.id.et_search_origin)
        val transaction = db.readableDatabase
        renderRecipes(
            db.getRecipesQuery(
                titleEditText.text.toString(),
                courseEditText.text.toString(),
                originEditText.text.toString(),
                transaction
            )
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

            val recipeLayout: View = inflater.inflate(R.layout.recipe_home_page_layout, null, true)
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
            recipeLayout.setOnClickListener {
                val intent = Intent(this, RecipeViewActivity::class.java)
                intent.putExtra("recipeId", (recipe.id))
                startActivity(intent)
            }
            row.setPadding(0, 0, 0, 10)
            row.addView(recipeLayout)
            recipeTable.addView(
                row, TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT,

                    )
            )
        }
    }
}