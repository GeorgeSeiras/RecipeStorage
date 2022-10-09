package com.example.recipestorage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val db = DatabaseHandler(this)

        val newRecipeButton: FloatingActionButton = findViewById(R.id.new_recipe_button)
        newRecipeButton.setOnClickListener {
            startActivity(Intent(this, NewRecipeActivity::class.java))
        }
    }


}