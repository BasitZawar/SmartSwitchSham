package com.example.ss_new.app_utils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.database.Cursor
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.ContactsContract
import android.provider.MediaStore
import android.telephony.TelephonyManager
import com.example.ss_new.database.DBHelper
import com.example.ss_new.database.FilesEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


object AllFilesUtils {

    val allFiles = ArrayList<FileInfo>()
    val allVideosFromDevice = ArrayList<FilesEntity>()
    val allImagesFromDevice = ArrayList<FilesEntity>()
    val allAudioFromDevice = ArrayList<FilesEntity>()
    val allDocFromDevice = ArrayList<FilesEntity>()
    val allApkFromDevice = ArrayList<FilesEntity>()
    val allDownloadsFromDevice = ArrayList<FilesEntity>()
    val allAppsFromDevice = ArrayList<FilesEntity>()
    private val TAG = javaClass.simpleName

    const val image: String = "Image"
    const val audio: String = "Audio"
    const val video: String = "Video"
    const val docs: String = "Doc"
    const val download: String = "Download"
    const val other: String = "Other"
    const val app: String = "App"
    const val apk: String = "Apk"
    var monthly_price: String = ""
    var six_month_price: String = ""
    var yearly_price: String = ""
    var isFromActv = true

    var appRegions = arrayListOf(
        "US", "FR", "CA", "DE", "UK", "IN"
    )

    data class FileInfo(val path: String, val fileType: String)

    @SuppressLint("SuspiciousIndentation")
    fun retrieveAllFiles(context: Context, fileType: String) {
        val externalFilesDir = Environment.getExternalStorageDirectory()
        traverseDirectory(externalFilesDir, allFiles, context, fileType)
    }

    fun setSubEnabled(context: Context, value: Boolean) {
        val pref = context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        pref?.edit()?.putBoolean("Remove_Ads", value)?.apply()
    }

    fun isSubscribed(context: Context): Boolean {
        val pref = context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        return pref?.getBoolean("Remove_Ads", false) ?: false
    }

    private fun traverseDirectory(
        directory: File,
        allFiles: ArrayList<FileInfo>,
        context: Context,
        fileType: String
    ) {
        if (directory.isDirectory) {
            val files = directory.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isDirectory) {
                        traverseDirectory(file, allFiles, context, fileType)
                    } else {
                        if (file.length() < 314572800) {
                            if (file.exists() && file.canRead() && getFileType(file) != other && getFileType(
                                    file
                                ) == fileType
                            ) {
                                if (file.name.lastIndexOf('.') != -1) {
                                    allFiles.add(FileInfo(file.path, getFileType(file)))
                                    when (fileType) {
                                        video -> {
                                            allVideosFromDevice.add(
                                                FilesEntity(
                                                    0,
                                                    file.path,
                                                    "",
                                                    getFileType(file),
                                                    isSelected = false,
                                                    isSent = false,
                                                    isReceived = false,
                                                    ""
                                                )
                                            )
                                        }

                                        image -> {
                                            allImagesFromDevice.add(
                                                FilesEntity(
                                                    0,
                                                    file.path,
                                                    "",
                                                    getFileType(file),
                                                    isSelected = false,
                                                    isSent = false,
                                                    isReceived = false,
                                                    ""
                                                )
                                            )
                                        }

                                        audio -> {
                                            allAudioFromDevice.add(
                                                FilesEntity(
                                                    0,
                                                    file.path,
                                                    "",
                                                    getFileType(file),
                                                    isSelected = false,
                                                    isSent = false,
                                                    isReceived = false,
                                                    ""
                                                )
                                            )
                                        }

                                        docs -> {
                                            allDocFromDevice.add(
                                                FilesEntity(
                                                    0,
                                                    file.path,
                                                    "",
                                                    getFileType(file),
                                                    isSelected = false,
                                                    isSent = false,
                                                    isReceived = false,
                                                    ""
                                                )
                                            )
                                        }

                                        apk -> {
                                            allApkFromDevice.add(
                                                FilesEntity(
                                                    0,
                                                    file.path,
                                                    "",
                                                    getFileType(file),
                                                    isSelected = false,
                                                    isSent = false,
                                                    isReceived = false,
                                                    ""
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun getFileType(file: File): String {
        return when (file.extension.lowercase()) {
            "jpg", "jpeg", "png", "gif" -> image
            "mp4", "avi", "mkv", "mov" -> video
            "pdf", "docx" -> docs
            "apk" -> apk
            "mp3", "wav", "ogg", "aac" -> audio
            else -> other
        }
    }

    fun changeSizeToFormat(size: Long): String {
        if (size <= 0) return "0B"

        val kiloByte = 1024
        val megaByte = kiloByte * kiloByte
        val gigaByte = megaByte * kiloByte

        return when {
            size >= gigaByte -> String.format("%.2f Gb", size.toDouble() / gigaByte)
            size >= megaByte -> String.format("%.2f Mb", size.toDouble() / megaByte)
            size >= kiloByte -> String.format("%.2f Kb", size.toDouble() / kiloByte)
            else -> String.format("%d B", size)
        }
    }

    fun getInstalledApps(context: Context) {
        var appSize: Long = 0
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val packageManager = context.packageManager
        val resolveInfoList = packageManager.queryIntentActivities(intent, 0)
        for (resolveInfo in resolveInfoList) {
            appSize = 0
            val packageName = resolveInfo.activityInfo.packageName
            if (!isSystemPackage(resolveInfo)) {
                val appName = resolveInfo.loadLabel(packageManager).toString()

                val packageInfo = packageManager.getPackageInfo(packageName, 0)
                val sourceDir = File(packageInfo.applicationInfo.sourceDir)
                if (sourceDir.exists()) {
                    appSize = sourceDir.length()
                }

                allAppsFromDevice.add(
                    FilesEntity(
                        0, packageName, "a",
                        "App", isSelected = false, isSent = false, isReceived = false, ""
                    )
                )

            }
        }
    }

    private fun isSystemPackage(resolveInfo: ResolveInfo): Boolean {
        val applicationInfo = resolveInfo.activityInfo.applicationInfo
        return (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
    }

    fun getAppDetails(context: Context, packageName: String): AppDetails? {
        val packageManager = context.packageManager
        val appInfo = try {
            packageManager.getApplicationInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return null
        }

        val appName = packageManager.getApplicationLabel(appInfo).toString()
        val sourceDir = appInfo.sourceDir
        val file = File(sourceDir)
        val appSize = changeSizeToFormat(file.length())
        val appIcon = packageManager.getApplicationIcon(appInfo)

        return AppDetails(appName, appSize, appIcon)
    }

    fun isWiFiConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

            return networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo

            return networkInfo != null && networkInfo.isConnected && networkInfo.type == ConnectivityManager.TYPE_WIFI
        }
    }

    fun getCurrentDate(): String {
        val currentDate = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy - hh:mm a", Locale.getDefault())
        return dateFormat.format(currentDate.time)
    }

    data class AppDetails(val name: String, val size: String, val icon: Drawable?)


    fun getAllFilesInDownloads() {
        val downloadsPath =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        getAllFilesRecursively(downloadsPath)
    }

    private fun getAllFilesRecursively(directory: File) {
        if (directory.exists() && directory.isDirectory) {
            val files = directory.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isFile) {
                        if (file.exists() && file.canRead() && getFileType(file) != other) {
                            if (file.name.lastIndexOf('.') != -1) {
                                allFiles.add(FileInfo(file.path, getFileType(file)))
                                allDownloadsFromDevice.add(
                                    FilesEntity(
                                        0,
                                        file.path,
                                        download,
                                        download,
                                        isSelected = false,
                                        isSent = false,
                                        isReceived = false,
                                        ""
                                    )
                                )
                            }
                        }
                    } else if (file.isDirectory) {
                        getAllFilesRecursively(file) // Recursive call
                    }
                }
            }
        }
    }


    fun getRecentlyAddedMedia(context: Context): List<RecentFileModel> {
        val recentMediaList = mutableListOf<RecentFileModel>()

        // Define the projection to retrieve relevant columns
        val projection = arrayOf(
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.TITLE,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.MEDIA_TYPE
        )

        // Specify the query parameters
        val selection =
            "${MediaStore.Files.FileColumns.MEDIA_TYPE} IN (?, ?, ?) AND ${MediaStore.Files.FileColumns.SIZE} > 0"
        val selectionArgs = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_DOCUMENT.toString()
        )

        // Sort the results by date modified in descending order
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"

        // Perform the query
        context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val dateModifiedColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val mediaTypeColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)

            while (cursor.moveToNext() && recentMediaList.size < 10) {
                val filePath = cursor.getString(dataColumn)
                val dateModifiedMillis = cursor.getLong(dateModifiedColumn)
                val title = cursor.getString(titleColumn)
                val size = cursor.getString(sizeColumn)
                val mediaType = cursor.getInt(mediaTypeColumn)

                if (File(filePath).exists() && File(filePath).length() > 100 && AllFilesUtils.getFileType(
                        File(filePath)
                    ) != AllFilesUtils.other
                ) {
//                    val dateModified = formatDate(dateModifiedMillis)
                    when (AllFilesUtils.getFileType(File(filePath))) {
                        AllFilesUtils.video, AllFilesUtils.image -> {
                            if (hasThumbnail(Uri.parse(filePath), context)) {
                                recentMediaList.add(
                                    RecentFileModel(
                                        File(filePath).name,
                                        filePath,
                                        "",
                                        AllFilesUtils.getFileType(File(filePath))
                                    )
                                )
                            }
                        }

                        else -> {
                            recentMediaList.add(
                                RecentFileModel(
                                    File(filePath).name,
                                    filePath,
                                    "",
                                    AllFilesUtils.getFileType(File(filePath))
                                )
                            )
                        }
                    }
                }
            }
        }

        return recentMediaList
    }

    private fun hasThumbnail(videoUri: Uri, context: Context): Boolean {
        val retriever = MediaMetadataRetriever()

        try {
            retriever.setDataSource(context, videoUri)
            val bitmap = retriever.getFrameAtTime(1)
            return bitmap != null
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            retriever.release()
        }
        return false
    }

    fun convertTime(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault())
        val date = Date(timestamp * 1000) // Convert seconds to milliseconds
        return dateFormat.format(date)
    }

    fun isAppRegion(ctx: Context): Boolean {
        val tm = ctx.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val countryCodeValue = tm.networkCountryIso.toUpperCase()
        return appRegions.contains(countryCodeValue)
    }

    data class RecentFileModel(
        var title: String,
        var path: String,
        var size: String,
        var date: String
    )

}