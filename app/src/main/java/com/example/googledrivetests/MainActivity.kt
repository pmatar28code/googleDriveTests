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
import com.google.api.client.http.FileContent
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.google.api.services.drive.model.Permission
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

//        val file = xFile(this.cacheDir, "for-phil-tests-84356-83ccab46dc55.json")
//        val filePath = file.path
        CoroutineScope(Dispatchers.IO).launch {
            runBlocking {
                val listOfDriveFiles = storage.Files().list()
                    .execute()
                Log.e(
                    "file path and list of drive files",
                    "xxpathHere drive files = $listOfDriveFiles  size = ${listOfDriveFiles.size}"
                )
            }
        }

        //TODO create a folder with specific name

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
        val folder_id = "1wwvZ8j20rPBqCK5JphMNt-HBcSHvS94Y"
        CoroutineScope(Dispatchers.IO).launch {
            runBlocking {
                val files = mutableListOf<File>()

                val folderName = "firstTestFolderRoot"  //TODO this is the name we are searching fore , could be a user, trainee etc..
                var pageToken: String? = null
                do {
                //TODO searching by folder name (which can be the trainee name or id and that way we get the actual folder id on the result)
                    val result: FileList = storage.files().list()
                        .setQ("mimeType='application/vnd.google-apps.folder' and name='$folderName' and trashed=false")//.setQ("'$folder_id' in parents and mimeType='application/vnd.google-apps.folder' and trashed=false")
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id,name,mimeType)")
                        .setPageToken(pageToken)
                        .execute()
                    for (f in result.files) {
                        files.add(f)
                    }
                    Log.e("list result = ", "$files")
                    pageToken = result.nextPageToken
                    Log.e("page token = ","$pageToken")
                } while (pageToken != null)

                //TODO get files in specific folder
                val q = "'$folder_id' in parents and trashed = false"
                val filesInSpecificFolderByID = storage.files().list()
                    .setQ(q)
                    .execute().files
                Log.e("Files in specific folder by ID","$filesInSpecificFolderByID")

                //TODO upload file to specific folder using the folder id
                val fileMetadata = File()
                fileMetadata.name = "forPhilJson.json"

                //TODO Set the parents parameter to the ID of the folder you want to upload the image to
//                val parents = mutableListOf<String>()
//                parents.add(folder_id)
//                fileMetadata.parents = parents
//
//                val actualFile = xFile(this@MainActivity.cacheDir, "for-phil-tests-84356-83ccab46dc55.json")
//                val filePath = actualFile.path
//                val fileToUpload = xFile(filePath)
//                val mediaContent = FileContent("application/json",fileToUpload)
//
//                val file = storage.files().create(fileMetadata, mediaContent)
//                    .setFields("id, name, parents")
//                    .execute()
            }
            //TODO delete files or folders
            //storage.files().delete("1wwvZ8j20rPBqCK5JphMNt-HBcSHvS94Y").execute()

            val emailAddressToShareTo = "lionprodev@gmail.com" //TODO share folder so that they have a way to see the folders and contents
                                                                 //TODO created by the app throughout use on a regular google drive account with GUI
//            val permission = Permission()
//                .setType("user")
//                .setRole("writer")
//                .setEmailAddress(emailAddressToShareTo)
//
//            storage.permissions().create(folder_id,permission).execute()

            //TODO delete permission to share specific folder (by id ) to an specific email address
            // Get the list of permissions for the folder
//            val permissions = storage.permissions().list(folder_id).execute().permissions
//
//            var permissionId: String? = null
//            for (permission in permissions) {
//                Log.e("Permission email in for loop","$permission")
//                if (permission.emailAddress == emailAddressToShareTo) {
//                    permissionId = permission.id
//                    break
//                }
//            }
//
//            permissionId = "12341993584222948980"
//            if (permissionId != null) {
//                storage.permissions().delete(folder_id, permissionId).execute()
//                Log.e("Permission for $emailAddressToShareTo", "removed from folder $folder_id")
//
//            }else{
//                Log.e("permision ID is NULL","$permissionId")
//            }

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