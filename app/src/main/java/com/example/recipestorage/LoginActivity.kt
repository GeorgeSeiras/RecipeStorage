package com.example.recipestorage

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        if (GoogleSignIn.getLastSignedInAccount(this) != null) {
            startActivity(Intent(this, HomePageActivity::class.java))
        } else {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_login)
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Scope(Scopes.DRIVE_APPFOLDER), Scope(Scopes.DRIVE_FILE))
                .requestServerAuthCode("475398467852-v5oujdpk4p7t43e619ecr7pjbm632s81.apps.googleusercontent.com")
                .requestIdToken("475398467852-v5oujdpk4p7t43e619ecr7pjbm632s81.apps.googleusercontent.com")
                .requestEmail()
                .build()
            val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

            val googleLoginButton = findViewById<Button>(R.id.google_login_btn)
            googleLoginButton.setOnClickListener {
                signIn(mGoogleSignInClient)
            }
        }
    }

    private fun signIn(mGoogleSignInClient: GoogleSignInClient) {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(
            signInIntent, RC_SIGN_IN
        )


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount = completedTask.getResult(
                ApiException::class.java
            )

            val client = OkHttpClient()
            val requestBody: RequestBody = FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add(
                    "client_id",
                    "475398467852-v5oujdpk4p7t43e619ecr7pjbm632s81.apps.googleusercontent.com"
                )
                .add("client_secret", "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")
                .add("redirect_uri", "")
                .add("scope", "${Scopes.DRIVE_FILE} ${Scopes.DRIVE_APPFOLDER}")
                .add("code", "${account.serverAuthCode}")
                .add("id_token", account.idToken)
                .build()
            val request: Request = Request.Builder()
                .url("https://www.googleapis.com/oauth2/v4/token")
                .post(requestBody)
                .build()
            val thread = Thread {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) Log.e("ERROR", "$response")
                    try {
                        val token = JSONObject(response.body()?.string() ?: "")
                        val driveHandler = GoogleDriveHandler(token)
                        driveHandler.uploadBasic()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
            thread.start()
            thread.join()

            startActivity(Intent(this, HomePageActivity::class.java))
        } catch (e: ApiException) {
            // Sign in was unsuccessful
            Log.e(
                "failed code=", e.statusCode.toString()
            )
        }
    }

    companion object {
        const val RC_SIGN_IN = 9001
    }

    private fun signOut(mGoogleSignInClient: GoogleSignInClient) {
        mGoogleSignInClient.signOut()
            .addOnCompleteListener(this) {
                // Update your UI here
            }
    }
}