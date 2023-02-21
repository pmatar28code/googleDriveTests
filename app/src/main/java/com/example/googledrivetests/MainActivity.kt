package com.example.googledrivetests

//import com.google.android.gms.drive.DriveFolder
//import com.google.android.gms.drive.MetadataChangeSet
//import com.google.api.services.drive.Drive

//import java.io.FileInputStream
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.drive.DriveApi
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.FileInputStream
import java.io.File as xFile


class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val scopes = mutableListOf<String>("https://www.googleapis.com/auth/drive.file",
            "https://www.googleapis.com/auth/drive.appdata",
            "https://www.googleapis.com/auth/drive.install",
            "https://www.googleapis.com/auth/drive",
            "https://www.googleapis.com/auth/drive.activity"
        )

        val httpTransport: HttpTransport = com.google.api.client.http.javanet.NetHttpTransport()
        val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()

        val googleServiceAccountJsonFile = getFileFromAssets(
            this,
            "for-phil-tests-84356-83ccab46dc55.json"
        ).path

        var googleCredentials = GoogleCredentials.fromStream(FileInputStream(googleServiceAccountJsonFile))
        googleCredentials = googleCredentials.createScoped(scopes)
        val requestInitializer: HttpRequestInitializer = HttpCredentialsAdapter(googleCredentials)
        val storage: Drive = Drive.Builder(httpTransport, jsonFactory, requestInitializer)
            .setApplicationName("soundRecorder")
            .build()
        Log.e("Storage:","$storage")

        val file = xFile(this.cacheDir, "for-phil-tests-84356-83ccab46dc55.json")
        val filePath = file.path
        CoroutineScope(Dispatchers.IO).launch {
            runBlocking {
                val listOfDriveFiles = storage.Files().list()
                    .execute()
                Log.e(
                    "file path and list of drive files",
                    "$filePath drive files = $listOfDriveFiles  size = ${listOfDriveFiles.size}"
                )
            }
        }

//        val metaDataSet = MetadataChangeSet.Builder()
//            .setTitle("firstTestFolderRoot")
//            .setMimeType("application/vnd.google-apps.folder")
//            .build()

        val fileMetaData = File()
         fileMetaData.name = "firstTestFolderRoot"
            fileMetaData.mimeType = "application/vnd.google-apps.folder"

//        CoroutineScope(Dispatchers.IO).launch {
//
//            try {
//                val file: File = storage.files().create(fileMetaData)
//                    .setFields("id")
//                    .execute()
//                Log.e("Folder ID on try: ", " ${file.id}")
//
//            } catch (e: GoogleJsonResponseException) {
//
//                Log.e("Unable to create folder: ", "${e.details}")
//
//            }
//        }

        CoroutineScope(Dispatchers.IO).launch {
            runBlocking {
                val filesInFolder =
                    storage.files().get("1wwvZ8j20rPBqCK5JphMNt-HBcSHvS94Y")

                filesInFolder.forEach { k, v ->
                    Log.e("k - v", "k= $k  v= $v")
                }
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            runBlocking {
                val files = mutableListOf<File>()

                var pageToken: String? = null
                do {
                    val result: FileList = storage.files().list()
                        .setQ("fullText contains '\"hello world\"'")
                        .setSpaces("drive")
                        .setFields("nextPageToken, items(id, title)")
                        .setPageToken(pageToken)
                        .execute()
                    for (file in result.files) {
                        System.out.printf(
                            "Found file: %s (%s)\n",
                            file.name, file.id
                        )
                    }

                    files.addAll(result.files)
                    pageToken = result.nextPageToken
                } while (pageToken != null)

                Log.e("list result = ", "$files")
            }
        }


    }

    private fun getFileFromAssets(
        context: Context,
        fileName: String
    ): xFile = xFile(context.cacheDir, fileName)
        .also {
            if (!it.exists()) {
                it.outputStream().use { cache ->
                    context.assets.open(fileName).use { inputStream ->
                        inputStream.copyTo(cache)
                    }
                }
            }
        }
}