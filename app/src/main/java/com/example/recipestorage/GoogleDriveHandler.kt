package com.example.recipestorage

import android.content.Context
import android.widget.Toast
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.FileContent
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*


class GoogleDriveHandler(token: JSONObject) {
    private val APPLICATION_NAME = "RecipeAPP"
    private val JSON_FACTORY = GsonFactory.getDefaultInstance()
    private val TOKENS_DIRECTORY_PATH = "tokens"
    private val SCOPES = Collections.singletonList((DriveScopes.DRIVE_METADATA_READONLY))
    private val CREDENTIALS_FILE_PATH = "credentials.json"
    private var client = OkHttpClient()
    private var credentials: GoogleCredentials = init(token)

    private fun init(token: JSONObject): GoogleCredentials {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, token.get("expires_in") as Int)
        val accessToken = AccessToken(token.get("access_token") as String, calendar.time)
        return GoogleCredentials(accessToken)
            .createScoped(listOf(DriveScopes.DRIVE_FILE))
    }

    //if db has been modified upload to drive
    @Throws(IOException::class)
    fun uploadBasic(): Int {

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
                    file = service.files().update(files.files[0].id, null, mediaContent).execute()
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

    fun downloadDb(context: Context): Int {
        val requestInitializer: HttpRequestInitializer = HttpCredentialsAdapter(
            credentials
        )
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
                    Toast.makeText(context, "No backups found on Google Drive", Toast.LENGTH_LONG)
                        .show()
                } else {
                    folderDrive = folders.files[0]
                    val filePath =
                        java.io.File("/data/data/com.example.recipestorage/databases/RecipeDatabase")
                    val mediaContent = FileContent("file/sqlite", filePath)
                    val files = service.files().list()
                        .setFields("files(id,name,modifiedTime)")
                        .setQ("name='RecipeDatabase.sqlite' AND '${folderDrive.id}' in parents")
                        .execute()
                    //file does not exist
                    if (files.files.size == 0) {
                        Toast.makeText(
                            context,
                            "No backups found on Google Drive",
                            Toast.LENGTH_LONG
                        ).show()
                        //file exists
                    } else {
                        val outputStream: OutputStream = FileOutputStream("RecipeDatabase.sqlite")
                        service.files().get(files.files[0].id)
                            .executeMediaAndDownloadTo(outputStream)
                        Toast.makeText(context, "Backup downloaded", Toast.LENGTH_LONG).show()
                    }
                }

            } catch (e: GoogleJsonResponseException) {
                System.err.println("Unable to download file: " + e.details)
                throw e
            }
        }
        thread.start()
        thread.join()
        return 1
    }
}


