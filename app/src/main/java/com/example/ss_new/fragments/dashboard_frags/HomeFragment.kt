package com.example.ss_new.fragments.dashboard_frags

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ss_new.R
import com.example.ss_new.activites.FileToSelectActivity
import com.example.ss_new.adapters.recycler_adapter.RecentFilesDataRecAdapter
import com.example.ss_new.databinding.FragmentHomeBinding
import com.example.ss_new.app_utils.AllFilesUtils
import com.example.ss_new.activites.LanguagesActivity
import com.example.ss_new.activites.Splash
import com.example.ss_new.activites.SubscriptionActivity
import com.example.ss_new.activites.sending_receiving.WifiOrHotSpotSelectionActivity
import com.example.ss_new.ads.BannerAdManager
import com.example.ss_new.ads.InterstitialHelper
import com.example.ss_new.app_utils.FileSelectionListener
import com.example.ss_new.subscription.DataStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class HomeFragment : Fragment(), FileSelectionListener {
    lateinit var binding: FragmentHomeBinding
    lateinit var adapter: RecentFilesDataRecAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        setUsedStorage()


        if (!AllFilesUtils.isSubscribed(requireActivity())) {
            BannerAdManager().loadAdmobBanner(requireActivity(), binding.adView)
        }

        adapter = RecentFilesDataRecAdapter(
            arrayListOf(),
            requireActivity(),
            object : FileSelectionListener {
                override fun clicked(path: String) {
                    when (AllFilesUtils.getFileType(File(path))) {
                        AllFilesUtils.video -> {
                            startActivity(
                                FileToSelectActivity.getIntentFileExplorerActivity(
                                    requireActivity(),
                                    AllFilesUtils.video
                                )
                            )
                        }

                        AllFilesUtils.image -> {
                            startActivity(
                                FileToSelectActivity.getIntentFileExplorerActivity(
                                    requireActivity(),
                                    AllFilesUtils.image
                                )
                            )
                        }

                        AllFilesUtils.audio -> {
                            startActivity(
                                FileToSelectActivity.getIntentFileExplorerActivity(
                                    requireActivity(),
                                    AllFilesUtils.audio
                                )
                            )
                        }

                        AllFilesUtils.docs -> {
                            startActivity(
                                FileToSelectActivity.getIntentFileExplorerActivity(
                                    requireActivity(),
                                    AllFilesUtils.docs
                                )
                            )
                        }

                        else -> {
                            startActivity(
                                FileToSelectActivity.getIntentFileExplorerActivity(
                                    requireActivity(),
                                    AllFilesUtils.image
                                )
                            )
                        }
                    }
                }
            })
        binding.recRecentFiles.layoutManager = LinearLayoutManager(requireActivity())
        binding.recRecentFiles.adapter = adapter


        if (Splash.Companion.appHavePermissions(requireActivity())) {
            binding.pgBar.visibility = View.VISIBLE
            binding.recRecentFiles.visibility = View.VISIBLE

            binding.goToSettings.visibility = View.GONE
            binding.tvNoPermission.visibility = View.GONE
            CoroutineScope(Dispatchers.IO).launch {
                val list = AllFilesUtils.getRecentlyAddedMedia(requireActivity())
                withContext(Dispatchers.Main) {
                    adapter.setData(list as ArrayList<AllFilesUtils.RecentFileModel>)
                    binding.pgBar.visibility = View.GONE
                }
            }
        } else {
            binding.goToSettings.visibility = View.VISIBLE
            binding.tvNoPermission.visibility = View.VISIBLE

            binding.pgBar.visibility = View.GONE
            binding.recRecentFiles.visibility = View.GONE

        }
        binding.goToSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package", requireActivity().packageName, null)
            intent.data = uri

            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(intent)
            }
        }

        binding.btnLang.setOnClickListener {
            startActivity(Intent(requireActivity(), LanguagesActivity::class.java))
        }
        binding.btnPro.setOnClickListener {
            startActivity(
                Intent(requireActivity(), SubscriptionActivity::class.java).putExtra(
                    "fromHomeFragment",
                    true
                )
            )
        }

        binding.btnSeeAll.setOnClickListener {
            startActivity(
                FileToSelectActivity.getIntentFileExplorerActivity(
                    requireActivity(),
                    AllFilesUtils.image
                )
            )
        }

        binding.btnReceive.setOnClickListener {
            if (Splash.Companion.appHavePermissions(requireActivity())) {
                InterstitialHelper.showSplashInterstitial(requireActivity(), object :
                    InterstitialHelper.InterstitialListener {
                    override fun onAdDismiss() {
                        Toast.makeText(
                            requireActivity(),
                            getString(R.string.transferTxt),
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(
                            WifiOrHotSpotSelectionActivity.getIntentForWifiOrHotSpotActivity(
                                requireActivity(),
                                "new"
                            )
                        )
                    }
                })
            } else {
                Toast.makeText(
                    requireActivity(),
                    "Please grant permissions to use this feature",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.btnSend.setOnClickListener {
            if (Splash.Companion.appHavePermissions(requireActivity())) {
                InterstitialHelper.showSplashInterstitial(requireActivity(), object :
                    InterstitialHelper.InterstitialListener {
                    override fun onAdDismiss() {
                        startActivity(
                            FileToSelectActivity.getIntentFileExplorerActivity(
                                requireActivity(),
                                AllFilesUtils.image
                            )
                        )
                    }
                })
            } else {
                Toast.makeText(
                    requireActivity(),
                    "Please grant permissions to use this feature",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        return binding.root
    }

    private fun setUsedStorage() {
        val externalDir = Environment.getExternalStorageDirectory()
        val statFs = StatFs(externalDir.path)
        val blockSize = statFs.blockSizeLong
        val totalBlocks = statFs.blockCountLong
        val total = totalBlocks * blockSize

        val availableBlocks = statFs.availableBlocksLong
        val consumed = (total - availableBlocks * blockSize)
        val per = ((consumed.toFloat() / total.toFloat()) * 100)
        val remaining = total - consumed
        val rem = ((remaining.toFloat() / total.toFloat()) * 100)


        binding.tvStorageRem.text =
            "${formatBytes(remaining.toFloat())}/ ${formatBytes(total.toFloat())}"
        binding.tvStorageUsed.text =
            "${formatBytes(consumed.toFloat())}/ ${formatBytes(total.toFloat())}"
        binding.progressStorageUsed.progress = per.toInt()
        binding.progressStorageRem.progress = rem.toInt()
    }

    private fun formatBytes(bytes: Float): String {
        val kilobyte = 1024.0
        val megabyte = kilobyte * 1024.0
        val gigabyte = megabyte * 1024.0
        val terabyte = gigabyte * 1024.0

        return when {
            bytes >= terabyte -> String.format("%.0f TB", bytes / terabyte)
            bytes >= gigabyte -> String.format("%.0f GB", bytes / gigabyte)
            bytes >= megabyte -> String.format("%.0f MB", bytes / megabyte)
            bytes >= kilobyte -> String.format("%.0f KB", bytes / kilobyte)
            else -> String.format("%.0f B", bytes)
        }
    }
}