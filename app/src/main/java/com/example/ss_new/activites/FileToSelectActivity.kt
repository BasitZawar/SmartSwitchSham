package com.example.ss_new.activites

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.example.ss_new.R
import com.example.ss_new.activites.sending_receiving.ActivityWifiConnection
import com.example.ss_new.adapters.FilinlingPagerAdapter
import com.example.ss_new.databinding.ActivityFileSelectionBinding
import com.example.ss_new.activites.sending_receiving.WifiOrHotSpotSelectionActivity
import com.example.ss_new.adapters.recycler_adapter.SelectedFilesRecyclerAdapter
import com.example.ss_new.ads.BannerAdManager
import com.example.ss_new.ads.InterstitialHelper
import com.example.ss_new.app_utils.AllFilesUtils
import com.example.ss_new.app_utils.FileSelectionListener
import com.example.ss_new.database.FilesEntity
import com.example.ss_new.fragments.files_fragment.*

class FileToSelectActivity : AppCompatActivity() {
    companion object {
        val selectedFileList = ArrayList<FilesEntity>()


        const val contentKey = "showContent"
        fun getIntentFileExplorerActivity(context: Context, showContent: String): Intent {
            return Intent(context, FileToSelectActivity::class.java).putExtra(
                contentKey,
                showContent
            )
        }
    }

    lateinit var binding: ActivityFileSelectionBinding
    private val img = ImageFragment()
    val music = MusicFragment()
    val video = VideosFragment()
    private val doc = DocsFragment()
    private val down = DownloadsFragment()
    val app = AppsFragment()
    val apk = ApkFragment()
    lateinit var adapter: SelectedFilesRecyclerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!AllFilesUtils.isSubscribed(this)) {
            BannerAdManager().loadAdmobBanner(this, binding.adView)
        }

        val showContent = intent.getStringExtra(contentKey)

        scrollToLayout(showContent)

        binding.btnSendFiles.setOnClickListener {
            if (selectedFileList.size > 0) {
                InterstitialHelper.showSplashInterstitial(this, object :
                    InterstitialHelper.InterstitialListener {
                    override fun onAdDismiss() {
                        if (AllFilesUtils.isWiFiConnected(this@FileToSelectActivity)) {
                            startActivity(
                                Intent(
                                    this@FileToSelectActivity,
                                    ActivityWifiConnection::class.java
                                ).putExtra("user", "sender")
                            )

                            finish()
                        } else {
                            Toast.makeText(
                                this@FileToSelectActivity,
                                getString(R.string.ensureWIFIOnTxt),
                                Toast.LENGTH_SHORT
                            ).show()
                        }


//                        startActivity(
//                            WifiOrHotSpotSelectionActivity.getIntentForWifiOrHotSpotActivity(
//                                this@FileToSelectActivity,
//                                "old"
//                            )
//                        )
                    }
                })
            } else {
                Toast.makeText(this, getString(R.string.selectFileTxt), Toast.LENGTH_SHORT).show()
            }
        }
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                selectedFileList.clear()
                isEnabled = false
            }
        })

        init()
        content(showContent)

        adapter =
            SelectedFilesRecyclerAdapter(selectedFileList, this, object : FileSelectionListener {
                override fun clicked(path: String) {
                    selectedFileList.removeAll { it.path == path }
                    updateSelected()
                }
            })
        binding.selectRecView.layoutManager = LinearLayoutManager(this)
        binding.selectRecView.adapter = adapter

        binding.upDownImg.setOnClickListener {
            if (binding.selectRecView.visibility == View.GONE) {
                binding.selectRecView.visibility = View.VISIBLE
                binding.upDownImg.setImageResource(R.drawable.arrow_up)
            } else {
                binding.selectRecView.visibility = View.GONE
                binding.upDownImg.setImageResource(R.drawable.arrow_down)
            }
        }
    }

    fun updateSelected() {
        binding.tvTFileSelected.text = selectedFileList.size.toString()
        adapter.updateItemList(selectedFileList)
    }

    private fun scrollToLayout(showContent: String?) {
        when (showContent) {
            AllFilesUtils.video -> {
                binding.btnVideos.parent.requestChildFocus(binding.btnVideos, binding.btnVideos)
            }

            AllFilesUtils.image -> {
                binding.btnImages.parent.requestChildFocus(binding.btnImages, binding.btnImages)
            }

            AllFilesUtils.audio -> {
                binding.btnMusic.parent.requestChildFocus(binding.btnMusic, binding.btnMusic)
            }

            AllFilesUtils.docs -> {
                binding.btnDocs.parent.requestChildFocus(binding.btnDocs, binding.btnDocs)
            }

            AllFilesUtils.download -> {
                binding.btnDownload.parent.requestChildFocus(
                    binding.btnDownload,
                    binding.btnDownload
                )
            }

            AllFilesUtils.app -> {
                binding.btnApps.parent.requestChildFocus(binding.btnApps, binding.btnApps)
            }

            AllFilesUtils.apk -> {
                binding.btnApk.parent.requestChildFocus(binding.btnApk, binding.btnApk)
            }
        }
    }

    private fun content(showContent: String?) {
        when (showContent) {
            AllFilesUtils.video -> {
                binding.btnVideos.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.tab_selected)
                binding.tvVid.setTextColor(getColor(R.color.white))
                binding.containerViewPager.currentItem = 2
            }

            AllFilesUtils.image -> {
                binding.btnImages.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.tab_selected)
                binding.tvImg.setTextColor(getColor(R.color.white))
                binding.containerViewPager.currentItem = 0
            }

            AllFilesUtils.audio -> {
                binding.btnMusic.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.tab_selected)
                binding.tvMus.setTextColor(getColor(R.color.white))
                binding.containerViewPager.currentItem = 1
            }

            AllFilesUtils.docs -> {
                binding.btnDocs.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.tab_selected)
                binding.tvDoc.setTextColor(getColor(R.color.white))
                binding.containerViewPager.currentItem = 3
            }

            AllFilesUtils.download -> {
                binding.btnDownload.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.tab_selected)
                binding.tvDwn.setTextColor(getColor(R.color.white))
                binding.containerViewPager.currentItem = 4
            }

            AllFilesUtils.app -> {
                binding.btnApps.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.tab_selected)
                binding.tvApp.setTextColor(getColor(R.color.white))
                binding.containerViewPager.currentItem = 5
            }

            AllFilesUtils.apk -> {
                binding.btnApk.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.tab_selected)
                binding.tvApk.setTextColor(getColor(R.color.white))
                binding.containerViewPager.currentItem = 6
            }
        }
    }

    private fun init() {

        binding.apply {
            btnBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }


            binding.containerViewPager.offscreenPageLimit = 6
            val newAdapter = FilinlingPagerAdapter(
                supportFragmentManager,
                7,
                img,
                music,
                video, doc, down, app, apk
            )
            binding.containerViewPager.adapter = newAdapter
            binding.containerViewPager.currentItem = 1
            binding.containerViewPager.addOnPageChangeListener(object :
                ViewPager.OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {

                }

                override fun onPageSelected(position: Int) {
                    when (position) {
                        0 -> {
                            clickImg()
                            scrollToLayout(AllFilesUtils.image)
                        }

                        1 -> {
                            scrollToLayout(AllFilesUtils.audio)
                            clickMusic()
                        }

                        2 -> {
                            scrollToLayout(AllFilesUtils.video)
                            clickVid()
                        }

                        3 -> {
                            scrollToLayout(AllFilesUtils.docs)
                            clickDoc()
                        }

                        4 -> {
                            scrollToLayout(AllFilesUtils.download)
                            clickDwn()
                        }

                        5 -> {
                            scrollToLayout(AllFilesUtils.app)
                            clickApp()
                        }

                        6 -> {
                            scrollToLayout(AllFilesUtils.apk)
                            clickApk()
                        }

                    }
                }

                override fun onPageScrollStateChanged(state: Int) {

                }
            })

            btnImages.setOnClickListener {
                clickImg()
                binding.containerViewPager.currentItem = 0
            }

            btnVideos.setOnClickListener {
                clickVid()
                binding.containerViewPager.currentItem = 2
            }

            btnApps.setOnClickListener {
                clickApp()
                binding.containerViewPager.currentItem = 5
            }

            btnApk.setOnClickListener {
                clickApk()
                binding.containerViewPager.currentItem = 6
            }

            btnMusic.setOnClickListener {
                clickMusic()
                binding.containerViewPager.currentItem = 1
            }

            btnDownload.setOnClickListener {
                clickDwn()
                binding.containerViewPager.currentItem = 4
            }

            btnDocs.setOnClickListener {
                clickDoc()
                binding.containerViewPager.currentItem = 3
            }
        }
    }


    fun disable() {
        binding.btnDocs.isEnabled = false
        binding.btnImages.isEnabled = false
        binding.btnVideos.isEnabled = false
        binding.btnApk.isEnabled = false
        binding.btnApps.isEnabled = false
        binding.btnMusic.isEnabled = false
        binding.btnDownload.isEnabled = false
    }

    fun enable() {
        binding.btnDocs.isEnabled = true
        binding.btnImages.isEnabled = true
        binding.btnVideos.isEnabled = true
        binding.btnApk.isEnabled = true
        binding.btnApps.isEnabled = true
        binding.btnMusic.isEnabled = true
        binding.btnDownload.isEnabled = true
    }

    private fun clickImg() {
        binding.btnImages.backgroundTintList =
            ContextCompat.getColorStateList(this, R.color.tab_selected)
        binding.btnVideos.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnDocs.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnApps.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnApk.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnMusic.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnDownload.backgroundTintList =
            ContextCompat.getColorStateList(this, R.color.white)


        binding.tvImg.setTextColor(getColor(R.color.white))
        binding.tvVid.setTextColor(getColor(R.color.btnTextColor))
        binding.tvDoc.setTextColor(getColor(R.color.btnTextColor))
        binding.tvApp.setTextColor(getColor(R.color.btnTextColor))
        binding.tvApk.setTextColor(getColor(R.color.btnTextColor))
        binding.tvMus.setTextColor(getColor(R.color.btnTextColor))
        binding.tvDwn.setTextColor(getColor(R.color.btnTextColor))


    }

    private fun clickVid() {
        binding.btnImages.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnVideos.backgroundTintList =
            ContextCompat.getColorStateList(this, R.color.tab_selected)
        binding.btnDocs.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnApps.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnApk.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnMusic.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnDownload.backgroundTintList =
            ContextCompat.getColorStateList(this, R.color.white)


        binding.tvImg.setTextColor(getColor(R.color.btnTextColor))
        binding.tvVid.setTextColor(getColor(R.color.white))
        binding.tvDoc.setTextColor(getColor(R.color.btnTextColor))
        binding.tvApp.setTextColor(getColor(R.color.btnTextColor))
        binding.tvApk.setTextColor(getColor(R.color.btnTextColor))
        binding.tvMus.setTextColor(getColor(R.color.btnTextColor))
        binding.tvDwn.setTextColor(getColor(R.color.btnTextColor))


    }

    private fun clickDoc() {
        binding.btnImages.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnVideos.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnDocs.backgroundTintList =
            ContextCompat.getColorStateList(this, R.color.tab_selected)
        binding.btnApps.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnApk.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnMusic.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnDownload.backgroundTintList =
            ContextCompat.getColorStateList(this, R.color.white)


        binding.tvImg.setTextColor(getColor(R.color.btnTextColor))
        binding.tvVid.setTextColor(getColor(R.color.btnTextColor))
        binding.tvDoc.setTextColor(getColor(R.color.white))
        binding.tvApp.setTextColor(getColor(R.color.btnTextColor))
        binding.tvApk.setTextColor(getColor(R.color.btnTextColor))
        binding.tvMus.setTextColor(getColor(R.color.btnTextColor))
        binding.tvDwn.setTextColor(getColor(R.color.btnTextColor))

    }

    private fun clickApp() {
        binding.btnImages.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnVideos.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnDocs.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnApps.backgroundTintList =
            ContextCompat.getColorStateList(this, R.color.tab_selected)
        binding.btnApk.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnMusic.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnDownload.backgroundTintList =
            ContextCompat.getColorStateList(this, R.color.white)


        binding.tvImg.setTextColor(getColor(R.color.btnTextColor))
        binding.tvVid.setTextColor(getColor(R.color.btnTextColor))
        binding.tvDoc.setTextColor(getColor(R.color.btnTextColor))
        binding.tvApp.setTextColor(getColor(R.color.white))
        binding.tvApk.setTextColor(getColor(R.color.btnTextColor))
        binding.tvMus.setTextColor(getColor(R.color.btnTextColor))
        binding.tvDwn.setTextColor(getColor(R.color.btnTextColor))

    }

    private fun clickApk() {
        binding.btnImages.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnVideos.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnDocs.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnApps.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnApk.backgroundTintList =
            ContextCompat.getColorStateList(this, R.color.tab_selected)
        binding.btnMusic.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnDownload.backgroundTintList =
            ContextCompat.getColorStateList(this, R.color.white)


        binding.tvImg.setTextColor(getColor(R.color.btnTextColor))
        binding.tvVid.setTextColor(getColor(R.color.btnTextColor))
        binding.tvDoc.setTextColor(getColor(R.color.btnTextColor))
        binding.tvApp.setTextColor(getColor(R.color.btnTextColor))
        binding.tvApk.setTextColor(getColor(R.color.white))
        binding.tvMus.setTextColor(getColor(R.color.btnTextColor))
        binding.tvDwn.setTextColor(getColor(R.color.btnTextColor))

    }

    private fun clickMusic() {
        binding.btnImages.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnVideos.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnDocs.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnApps.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnApk.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnMusic.backgroundTintList =
            ContextCompat.getColorStateList(this, R.color.tab_selected)
        binding.btnDownload.backgroundTintList =
            ContextCompat.getColorStateList(this, R.color.white)


        binding.tvImg.setTextColor(getColor(R.color.btnTextColor))
        binding.tvVid.setTextColor(getColor(R.color.btnTextColor))
        binding.tvDoc.setTextColor(getColor(R.color.btnTextColor))
        binding.tvApp.setTextColor(getColor(R.color.btnTextColor))
        binding.tvApk.setTextColor(getColor(R.color.btnTextColor))
        binding.tvMus.setTextColor(getColor(R.color.white))
        binding.tvDwn.setTextColor(getColor(R.color.btnTextColor))

    }

    private fun clickDwn() {
        binding.btnImages.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnVideos.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnDocs.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnApps.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnApk.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnMusic.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
        binding.btnDownload.backgroundTintList =
            ContextCompat.getColorStateList(this, R.color.tab_selected)


        binding.tvImg.setTextColor(getColor(R.color.btnTextColor))
        binding.tvVid.setTextColor(getColor(R.color.btnTextColor))
        binding.tvDoc.setTextColor(getColor(R.color.btnTextColor))
        binding.tvApp.setTextColor(getColor(R.color.btnTextColor))
        binding.tvApk.setTextColor(getColor(R.color.btnTextColor))
        binding.tvMus.setTextColor(getColor(R.color.btnTextColor))
        binding.tvDwn.setTextColor(getColor(R.color.white))

    }
}