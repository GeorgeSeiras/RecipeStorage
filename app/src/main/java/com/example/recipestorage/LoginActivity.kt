package com.example.recipestorage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import org.json.JSONException
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        signOut(mGoogleSignInClient)
        if (GoogleSignIn.getLastSignedInAccount(this) != null) {
            val handler = GoogleDriveHandler(this, GoogleSignIn.getLastSignedInAccount(this))
            handler.syncDb(this)
            startActivity(Intent(this, HomePageActivity::class.java))
        } else {
            setContentView(R.layout.activity_login)
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Scope(Scopes.DRIVE_APPFOLDER), Scope(Scopes.DRIVE_FILE))
                .requestServerAuthCode(BuildConfig.CLIENT_ID)
                .requestIdToken(BuildConfig.CLIENT_ID)
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
            val thread = Thread {
                val flow = GoogleAuthorizationCodeFlow.Builder(
                    NetHttpTransport(), GsonFactory.getDefaultInstance(),
                    BuildConfig.CLIENT_ID,
                    BuildConfig.CLIENT_SECRET,
                    Collections.singleton("${Scopes.DRIVE_FILE} ${Scopes.DRIVE_APPFOLDER}")
                )
                    .setAccessType("offline")
                    .setApprovalPrompt("force")
                    .build()

                val response = flow.newTokenRequest(account.serverAuthCode).execute()

                val credentials = GoogleCredential.Builder()
                    .setJsonFactory(GsonFactory.getDefaultInstance())
                    .setTransport(NetHttpTransport())
                    .setClientSecrets(
                        BuildConfig.CLIENT_ID,
                        BuildConfig.CLIENT_SECRET
                    )
                    .build()
                    .setFromTokenResponse(response)

                try {
                    val db = DatabaseHandler(this)
                    var user = db.getUserByGId(account.id!!)
                    if (user == null) {
                        user = db.addUser(account.email!!, account.id!!)
                    } else {
                        if (user.email != account.email) {
                            user = db.updateUser(account.email!!, account.id!!)
                        }
                    }
                    var driveHandler: GoogleDriveHandler
                    if (credentials.refreshToken != null) {
                        db.addToken(
                            credentials.accessToken,
                            credentials.refreshToken,
                            credentials.expiresInSeconds.toInt(),
                            user!!.id
                        )
                        db.close()
                        driveHandler = GoogleDriveHandler(
                            this,
                            account
                        )
                        driveHandler.syncDb(this)
                    } else {
                        val token = db.getTokenOfUser(user!!.id)
                        if (token == null) {
                            downloadBackup(credentials)
                            db.close()
                        } else {
                            if (token!!.refresh != null && token!!.token != null) {
                                db.updateToken(
                                    credentials.accessToken,
                                    null,
                                    credentials.expiresInSeconds.toInt(),
                                    token.id
                                )
                            }
                            db.close()
                            driveHandler = GoogleDriveHandler(
                                this,
                                account
                            )
                            driveHandler.syncDb(this)
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
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

    fun downloadBackup(glCredential: GoogleCredential) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.SECOND, glCredential.expiresInSeconds.toInt() - 50)
        val accessToken =
            AccessToken(glCredential.accessToken, null)
        val requestInitializer: HttpRequestInitializer = HttpCredentialsAdapter(
            GoogleCredentials(accessToken)
                .createScoped(listOf(DriveScopes.DRIVE_FILE))
        )
        val service = Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            requestInitializer
        )
            .setApplicationName("RecipeStorage")
            .build()

        val folders = service.files().list()
            .setQ("name='RecipesStorage' AND mimeType='application/vnd.google-apps.folder'")
            .execute()
        var file = File()
        var folderDrive: File
        //folder does not exist
        if (folders.files.size == 0) {
            val folder = File()
            folder.name = "RecipesStorage"
            folder.mimeType = "application/vnd.google-apps.folder"
            folderDrive = service.files().create(folder).execute()
            //folder exists
        } else {
            folderDrive = folders.files[0]
        }

        val files = service.files().list()
            .setFields("files(id,name,modifiedTime)")
            .setQ("name='RecipeDatabase.sqlite' AND '${folderDrive.id}' in parents")
            .execute()
        //file does not exist
        if (files.files.size == 0) {
            Log.v("TEST", "here")

            //TODO remove drive permissions?
        }
        val outputStream: OutputStream =
            FileOutputStream("/data/data/com.example.recipestorage/databases/RecipeDatabase")
        service.files().get(files.files[0].id)
            .executeMediaAndDownloadTo(outputStream)
    }
}