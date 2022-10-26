package com.example.recipestorage

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.FileContent
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.Data
import com.google.api.client.util.DateTime
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.OAuth2Credentials
import com.google.gson.JsonObject
import org.json.JSONObject
import java.io.*
import java.util.*


class GoogleDriveHandler(
    context: Context,
    account: GoogleSignInAccount?,
) {
    private var credentials: GoogleCredentials = init(account, context)

    private fun init(
        account: GoogleSignInAccount?,
        context: Context
    ): GoogleCredentials {
        val db = DatabaseHandler(context)
        val user =
            db.getUserByEmail(account!!.email!!) ?: throw Exception("Something went wrong...")
        var token = db.getTokenOfUser(user!!.id) ?: throw Exception("Something went wrong...")
        val calendar = Calendar.getInstance()

        if (calendar.timeInMillis / 1000L < token!!.expiry) {
            val accessToken = AccessToken(token.token, Date(token.expiry * 1000L))
            return GoogleCredentials(accessToken)
                .createScoped(listOf("${DriveScopes.DRIVE_FILE} ${DriveScopes.DRIVE_APPDATA}"))
        } else {
            val thread = Thread {
                val jsonCreds = JSONObject(
                    "{" +
                            "\"type\":\"authorized_user\"," +
                            "\"client_id\":\"${BuildConfig.CLIENT_ID}\"," +
                            "\"client_secret\":\"${BuildConfig.CLIENT_SECRET}\"," +
                            "\"refresh_token\":\"${token.refresh}\"" +
                            "}"
                )
                val creds = GoogleCredentials.fromStream(
                    jsonCreds.toString().byteInputStream()
                )
                val newToken = creds.refreshAccessToken()
                val cal = Calendar.getInstance()
//                cal.add(Calendar.SECOND, newToken.expirationTime)
                db.updateToken(
                    newToken.tokenValue,
                    null,
                    (newToken.expirationTime.time / 1000L).toInt(),
                    token.id
                )
//                val refreshCredentials = GoogleCredential
//                    .Builder()
//                    .setJsonFactory(GsonFactory.getDefaultInstance())
//                    .setTransport(NetHttpTransport())
//                    .setClientSecrets(
//                        BuildConfig.CLIENT_ID,
//                        BuildConfig.CLIENT_SECRET
//                    )
//                    .build()
//                    .setRefreshToken(token.refresh)
//                    .setAccessToken(token.token)
//                    .setExpiresInSeconds(token.expiry.toLong())
//                refreshCredentials.refreshToken()

//                val cal = Calendar.getInstance()
//                cal.add(Calendar.SECOND, refreshCredentials.expiresInSeconds.toInt())
//                db.updateToken(
//                    refreshCredentials.accessToken,
//                    null,
//                    refreshCredentials.expiresInSeconds.toInt(),
//                    token.id
//                )
            }
            thread.start()
            thread.join()
            val refreshedToken = db.getToken(token.id)
            val jsonCreds = JSONObject(
                "{" +
                        "\"type\":\"authorized_user\"," +
                        "\"client_id\":\"${BuildConfig.CLIENT_ID}\"," +
                        "\"client_secret\":\"${BuildConfig.CLIENT_SECRET}\"," +
                        "\"refresh_token\":\"${refreshedToken.refresh}\"" +
                        "}"
            )
            return GoogleCredentials.fromStream(
                jsonCreds.toString().byteInputStream()
            )
//            val accessToken =
//                AccessToken(refreshedToken.token, Date(refreshedToken.expiry.toLong()))
//            return GoogleCredentials.create(accessToken)
//                .createScoped(listOf("${DriveScopes.DRIVE_FILE} ${DriveScopes.DRIVE_APPDATA}"))

        }
        throw Exception("Some error")
    }

    //if db has been modified upload to drive
    @Throws(IOException::class)
    fun syncDb(context: Context): Int {
        val db = DatabaseHandler(context)

        val requestInitializer: HttpRequestInitializer = HttpCredentialsAdapter(
            credentials
        )
        var file = File()
        // Build a new authorized API client service.
        val thread = Thread {
            val service = Drive.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                requestInitializer
            )
                .setApplicationName("RecipeStorage")
                .build()

            try {
                val folders = service.files().list()
                    .setQ("name='RecipesStorage' AND mimeType='application/vnd.google-apps.folder'")
                    .execute()
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

                val filePath =
                    java.io.File("/data/data/com.example.recipestorage/databases/RecipeDatabase")
                val mediaContent = FileContent("file/sqlite", filePath)
                val files = service.files().list()
                    .setFields("files(id,name,modifiedTime)")
                    .setQ("name='RecipeDatabase.sqlite' AND '${folderDrive.id}' in parents")
                    .execute()
                //file does not exist
                if (files.files.size == 0) {
                    val fileMetadata = File()
                    fileMetadata.name = "RecipeDatabase.sqlite"
                    fileMetadata.parents = Collections.singletonList(folderDrive.id)
                    file = service.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute()
                    //file exists
                } else {
                    val lastModified = db.getLastModified()
                    val googleDate: DateTime = files.files[0]["modifiedTime"] as DateTime
                    if (lastModified > (googleDate.value / 1000L)) {
                        file =
                            service.files().update(files.files[0].id, null, mediaContent).execute()
                    } else {
                        val outputStream: OutputStream =
                            FileOutputStream("/data/data/com.example.recipestorage/databases/RecipeDatabase")
                        service.files().get(files.files[0].id)
                            .executeMediaAndDownloadTo(outputStream)
                    }
                }
            } catch (e: GoogleJsonResponseException) {
                System.err.println("Unable to upload file: " + e.details)
                throw e
            }
        }
        thread.start()
        thread.join()
        return 1
    }


}


