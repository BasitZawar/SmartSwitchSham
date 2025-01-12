package com.example.ss_new.activites.sending_receiving

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.ss_new.activites.DashboardActivity
import com.example.ss_new.app_utils.AllFilesUtils
import com.example.ss_new.app_utils.data_classes.ss_models.TransferModel
import com.example.ss_new.connection.SocketHandler
import com.example.ss_new.database.DBHelper
import com.example.ss_new.database.FilesEntity
import com.example.ss_new.databinding.ActivityReceiverBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.DataInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.net.Socket
import java.net.SocketTimeoutException

class FilerDataReceivingActivity : AppCompatActivity() {

    private val TAG = javaClass.simpleName
    private lateinit var binding: ActivityReceiverBinding
    private var receivedFilesSize: Long = 0

    private var receiveVideoCount = 0
    private var receiveMusicCount = 0
    private var receiveImgCount = 0
    private var receiveDocCount = 0
    private var receiveDownCount = 0
    private var receiveAppCount = 0
    private var receiveApkCount = 0


    private var tImgItems: Int = 0
    private var tVideoItems: Int = 0
    private var tMusicItems: Int = 0
    private var tDocItems: Int = 0
    private var tDownloadItem: Int = 0
    private var tAppItem: Int = 0
    private var tApkItem: Int = 0

    var socket: Socket? = null

    private var toDayDate = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.WHITE
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        binding = ActivityReceiverBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.e("TESTTAG", "onCreate: Receiving activity")
        toDayDate = AllFilesUtils.getCurrentDate()


        MainScope().launch {
            Log.e("TAG", "onCreate: MainScore")
            try {
                Log.e("TAG", "onCreate: MainScore try")

                receiveData()
            } catch (e: Exception) {
                Log.e("TAG", "onCreate: MainScore catch")

                Timber.e("$TAG Error: $e")
            }
        }
        binding.btnDone.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finishAffinity()
        }
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectWifiDirect()
    }

    private fun disconnectWifiDirect() {
        val p2pManager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        val p2pChannel = p2pManager.initialize(this, mainLooper, null)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        p2pManager.requestGroupInfo(p2pChannel) { group ->
            if (group != null) {
                p2pManager.removeGroup(
                    p2pChannel,
                    object : WifiP2pManager.ActionListener {
                        override fun onSuccess() {
                            Timber.e(TAG, "removeGroup onSuccess -")
                        }

                        override fun onFailure(reason: Int) {
                            Timber.e("$TAG removeGroup onFailure -$reason")
                        }
                    })
            }
        }
    }

    private suspend fun receiveData(): Int = withContext(Dispatchers.IO) {
//        val socket = getSocket()
        socket = SocketHandler.getSocket()
        Log.e("TESTTAG", "receiveData: socket let")
        socket?.let {
            Log.e("TESTTAG", "receiveData: after socket let")

            try {
                Log.e("TESTTAG", "receiveData: inside try")

                val ois = ObjectInputStream(it.getInputStream())
                val dis = DataInputStream(ois)
                val filesCount = dis.readInt()
                val string = dis.readUTF()
                Log.e("TESTTAG", "receiveData: receiving dis " + dis)
                Log.e("TESTTAG", "receiveData: receiving array " + filesCount)
                extractSizesString(string)
                withContext(Dispatchers.Main) {
                    setPercentages()
                }

                for (i in 0 until filesCount) {
                    Log.e("TESTTAG", "receiveData:  for loop $i")
                    var bytes: ByteArray?
                    var fos: FileOutputStream? = null
                    try {
                        Log.e("TESTTAG", "receiveData: inside try for loop")
                        val fileToReceive = ois.readObject() as TransferModel
                        if (fileToReceive.type != "Con") {
                            bytes = ois.readObject() as ByteArray

                            val folder = File(
                                Environment.getExternalStorageDirectory(),
                                "/MySmartSwitchData"
                            )
//                            val file = File("/storage/emulated/0/MySmartSwitchData/IMG_20231104_144138170_HDR (6).jpg")

                            if (!folder.exists()) {
                                folder.mkdirs()
                                Log.e("TESTTAG", "receiveData: folder made")
                            }
                            val fileName = if (fileToReceive.type == "Apps") {
                                fileToReceive.name + ".apk"
                                Log.e("TESTTAG", "receiveData: apk ${fileToReceive.name}")
                            } else {
                                Log.e("TESTTAG", "receiveData: file name ${fileToReceive.name}")
                                fileToReceive.name
                            }
                            val file =
                                File(folder, "/storage/emulated/0/MySmartSwitchData/$fileName")
                            Log.e("TESTTAG", "receiveData: file $file")
                            when (fileToReceive.type) {
                                AllFilesUtils.audio -> receiveMusicCount += 1
                                AllFilesUtils.video -> receiveVideoCount += 1
                                AllFilesUtils.docs -> receiveDocCount += 1
                                AllFilesUtils.image -> receiveImgCount += 1
                                AllFilesUtils.apk -> receiveApkCount += 1
                                AllFilesUtils.app -> receiveAppCount += 1
                                AllFilesUtils.download -> receiveDownCount += 1
                            }
                            Log.e("TESTTAG", "receiveData: after when statement")
                            try {
                                Log.e("TESTTAG", "receiveData: inside the try")
                                sendBroadcast(
                                    Intent(
                                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                        Uri.fromFile(file)
                                    )
                                )
                                Log.e("TESTTAG", "receiveData: inside and after the sendBroadcast")
                            } catch (e: Exception) {
                                Log.e(
                                    "TESTTAG",
                                    "receiveData: inside the catch ${e.localizedMessage}"
                                )

                            }

                            Log.e("TESTTAG", "receiveData: after the try catch")
                            try {
                                fos = FileOutputStream(file)
                                fos.write(bytes)
                                receivedFilesSize += file.length()
                            } catch (e: Exception) {
                            }

                            Log.e("TESTTAG", "receiveData: before history method")
                            DBHelper.getDB(this@FilerDataReceivingActivity).sSwitchDao()
                                ?.insertAllFiles(
                                    FilesEntity(
                                        0, file.path, "", getFileType(file.path),
                                        isSelected = false,
                                        isSent = false,
                                        isReceived = true, toDayDate
                                    )
                                )

                            Log.e("TESTTAG", "receiveData: before UI operation")
                            launch(Dispatchers.Main) {
                                Log.e("TESTTAG", "receiveData: inside ")
                                updatePercentages()
                            }
                        }
                    } catch (e: SocketTimeoutException) {
                        Timber.e("TESTTAG", "Timeout while receiving data: ${e.message}")
                        // Retry logic can be implemented here
                    } catch (e: Exception) {
                        Timber.e("TESTTAG", "run Exception: ${e.message}")
                    } finally {
                        fos?.flush()
                        fos?.close()
                        Log.e("TESTTAG", "receiveData fos closed")

                    }
                }
                launch(Dispatchers.Main) {
                    binding.btnDone.visibility = View.VISIBLE
                }
            } catch (ex: SocketTimeoutException) {
                Timber.e("TESTTAG", "Timeout Exception: ${ex.message}")
                Log.e("TESTTAG", "receiveData Exception 1 ${ex.message}: ")
                // Implement retry logic if needed
            } catch (ex: IOException) {
                Timber.e("TESTTAG", "IOException: ${ex.message}")
                Log.e("TESTTAG", "receiveData Exception 2 ${ex.message}: ")

            } catch (ex: Exception) {
                Timber.e("TESTTAG", "General exception: ${ex.message}")
                Log.e("TESTTAG", "receiveData Exception 3 ${ex.message}: ")

            } finally {
                it.close()
                Log.e("TESTTAG", "finally socket close")

            }
        }

        return@withContext 0
    }


    private fun setPercentages() {

        binding.progressVideo.max = tVideoItems
        binding.progressMusic.max = tMusicItems
        binding.progressDoc.max = tDocItems
        binding.progressImage.max = tImgItems
        binding.progressDownload.max = tDownloadItem
        binding.progressApk.max = tApkItem
        binding.progressApp.max = tAppItem



        binding.tvTotalVideoSending.text = "$tVideoItems items"
        binding.tvTotalMusicSending.text = "$tMusicItems items "
        binding.tvTotalDocSending.text = "$tDocItems items "
        binding.tvTotalImagesSending.text = "$tImgItems items "
        binding.tvTotalDownloadSending.text = "$tDownloadItem items "
        binding.tvTotalApkSending.text = "$tApkItem items "
        binding.tvTotalAppSending.text = "$tAppItem items "
    }

    private fun updatePercentages() {
        Log.e("TESTTAG", "updatePercentages receiveVideoCount: $receiveVideoCount")
        Log.e("TESTTAG", "updatePercentages receiveMusicCount: $receiveMusicCount")
        Log.e("TESTTAG", "updatePercentages receiveImgCount: $receiveImgCount")
        Log.e("TESTTAG", "updatePercentages receiveDocCount: $receiveDocCount")
        Log.e("TESTTAG", "updatePercentages receiveApkCount: $receiveApkCount")
        Log.e("TESTTAG", "updatePercentages receiveAppCount: $receiveAppCount")
        binding.progressVideo.progress = receiveVideoCount
        binding.progressMusic.progress = receiveMusicCount
        binding.progressImage.progress = receiveImgCount
        binding.progressDoc.progress = receiveDocCount
        binding.progressDownload.progress = receiveDownCount
        binding.progressApk.progress = receiveApkCount
        binding.progressApp.progress = receiveAppCount

    }

    private fun extractSizesString(inputString: String) {
        val regex = Regex("""(video|img|audio|doc|apps|apk|download):(\d+)""")
        val matches = regex.findAll(inputString)

        val sizes = mutableMapOf<String, Int>()

        for (match in matches) {
            val type = match.groups[1]?.value
            val size = match.groups[2]?.value?.toIntOrNull()

            if (type != null && size != null) {
                sizes[type] = size
            }
        }

        tVideoItems = sizes["video"] ?: 0
        tImgItems = sizes["img"] ?: 0
        tMusicItems = sizes["audio"] ?: 0
        tDocItems = sizes["doc"] ?: 0
        tApkItem = sizes["apk"] ?: 0
        tAppItem = sizes["apps"] ?: 0
        tDownloadItem = sizes["download"] ?: 0

    }

    private fun getFileType(filePath: String): String {
        return when (filePath.substringAfterLast(".").lowercase()) {
            in setOf("mp3", "wav", "ogg", "aac", "flac") -> "Audio"
            in setOf("mp4", "avi", "mkv", "mov", "flv") -> "Video"
            in setOf("pdf", "doc", "docx", "txt", "xls", "xlsx") -> "Doc"
            in setOf("jpg", "jpeg", "png", "gif", "bmp") -> "Image"
            else -> ""
        }
    }
}