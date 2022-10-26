package com.example.recipestorage

import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.OnCompleteListener


class Logout() {

    fun logoutPopup(logoutButton: View, context: Context) {
        val inflater = context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(R.layout.popup_logout_confirmation, null)

        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val focusable = true

        val popupWindow = PopupWindow(popupView, width, height, focusable)
//                val parent = findViewById<ScrollView>(R.id.sv_recipe_view)
        popupWindow.showAtLocation(logoutButton, Gravity.CENTER, 0, 0)

        val cancelButton: Button = popupView.findViewById(R.id.bt_cancel)
        cancelButton.setOnClickListener {
            popupWindow.dismiss()
        }
        val deleteButton: Button = popupView.findViewById(R.id.bt_confirm)
        deleteButton.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Scope(Scopes.DRIVE_APPFOLDER), Scope(Scopes.DRIVE_FILE))
                .requestServerAuthCode(BuildConfig.CLIENT_ID)
                .requestIdToken(BuildConfig.CLIENT_ID)
                .requestEmail()
                .build()
            val mGoogleSignInClient = GoogleSignIn.getClient(context, gso)
            mGoogleSignInClient.signOut().addOnCompleteListener {
                context.startActivity(Intent(context, LoginActivity::class.java))
            }
        }

    }
}