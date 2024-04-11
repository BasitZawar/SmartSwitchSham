package com.example.ss_new.activites.sending_receiving

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.ss_new.activites.DashboardActivity
import com.example.ss_new.database.DBHelper
import com.example.ss_new.database.FilesEntity
import com.example.ss_new.databinding.ActivityReceiverBinding
import com.example.ss_new.app_utils.data_classes.connection.Sockets.getSocket
import com.example.ss_new.app_utils.data_classes.ss_models.TransferModel
import com.example.ss_new.app_utils.AllFilesUtils
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.DataInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectInputStream

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


    private var toDayDate = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiverBinding.inflate(layoutInflater)
        setContentView(binding.root)


        toDayDate = AllFilesUtils.getCurrentDate()


        MainScope().launch {
            try {
               receiveData()
            } catch (e: Exception) {
                Timber.e( "$TAG Error: $e")
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
    @SuppressLint("MissingPermission")
    private fun disconnectWifiDirect() {
        val p2pManager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        val p2pChannel = p2pManager.initialize(this, Looper.getMainLooper(), null)
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
        val socket = getSocket()
        if (socket != null) {
            try {
                val ois = ObjectInputStream(socket.getInputStream())
                val dis = DataInputStream(ois)
                val filesCount = dis.readInt()
                val string = dis.readUTF()
                Timber.e("$TAG: string received: $string")
                extractSizesString(string)
                withContext(Dispatchers.Main) {
                    setPercentages()
                }
                for (i in 0 until filesCount) {
                    var bytes: ByteArray?
                    var fos: FileOutputStream? = null
                    try {
                        val fileToReceive = ois.readObject() as TransferModel
                        if (fileToReceive.type == "Con") {
                            //
                        } else {
                            bytes = ois.readObject() as ByteArray

                            val folder = File(Environment.getExternalStorageDirectory(), "/MySmartSwitchData")

                            if (!folder.exists()) {
                                folder.mkdirs()
                            }
                            val fileName = if (fileToReceive.type == "Apps") {
                                fileToReceive.name + ".apk"
                            } else {
                                fileToReceive.name
                            }
                            val file = File(
                                folder, "/$fileName"
                            )

                            when (fileToReceive.type) {
                                AllFilesUtils.audio -> {
                                    receiveMusicCount += 1
                                }
                                AllFilesUtils.video -> {
                                    receiveVideoCount += 1
                                }
                                AllFilesUtils.docs -> {
                                    receiveDocCount += 1
                                }
                                AllFilesUtils.image -> {
                                    receiveImgCount += 1
                                }
                                AllFilesUtils.apk -> {
                                    receiveApkCount += 1
                                }
                                AllFilesUtils.app -> {
                                    receiveAppCount += 1
                                }
                                AllFilesUtils.download -> {
                                    receiveDownCount += 1
                                }
                            }

                            sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))

                            fos = FileOutputStream(file)
                            fos.write(bytes)
                            receivedFilesSize += file.length()

                            DBHelper.getDB(this@FilerDataReceivingActivity).sSwitchDao()?.insertAllFiles(
                                FilesEntity(0,file.path,"",getFileType(file.path),
                                    isSelected = false,
                                    isSent = false,
                                    isReceived = true,toDayDate)
                            )

                            launch(Dispatchers.Main) {
                                updatePercentages()
                            }
                        }
                    } catch (e: Exception) {
                        Timber.e( "$TAG run Exception: $e")
                    } finally {
                        fos?.flush()
                        fos?.close()
                    }
                }
                launch(Dispatchers.Main) {
                    binding.btnDone.visibility = View.VISIBLE
                }
            } catch (ex: Exception) {
                Timber.e("$ex")
            } finally {
                socket.close()
            }
        } else {
            return@withContext 0
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

    private fun updatePercentages(){

        binding.progressVideo.progress = receiveVideoCount
        binding.progressMusic.progress = receiveMusicCount
        binding.progressImage.progress = receiveImgCount
        binding.progressDoc.progress = receiveDocCount
        binding.progressDownload.progress = receiveDownCount
        binding.progressApk.progress = receiveApkCount
        binding.progressApp.progress = receiveAppCount

    }

    private fun extractSizesString(inputString: String){
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