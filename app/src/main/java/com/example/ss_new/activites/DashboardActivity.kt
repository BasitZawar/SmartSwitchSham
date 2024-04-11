package com.example.ss_new.activites

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.example.ss_new.R
import com.example.ss_new.adapters.MainPagerAdapter
import com.example.ss_new.databinding.ActivityDashboardBinding
import com.example.ss_new.dialogs.DialogExit
import com.example.ss_new.fragments.dashboard_frags.FilesFragment
import com.example.ss_new.fragments.dashboard_frags.HistoryFragment
import com.example.ss_new.fragments.dashboard_frags.HomeFragment
import com.example.ss_new.subscription.DataStoreManager

class DashboardActivity : AppCompatActivity(){
    lateinit var binding: ActivityDashboardBinding

    private lateinit var loadingDialog : Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        window.statusBarColor = getColor(R.color.color_blue)
        setContentView(binding.root)

        loadingDialog = Dialog(this)

        setTabLayout()

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if(binding.dasViewPager.currentItem == 1){
                    DialogExit().show(supportFragmentManager,"Exit")
                }else{
                    binding.dasViewPager.currentItem = 1
                }
            }

        })
    }


    private fun setTabLayout(){
        val homeFrag = HomeFragment()
        val fileFrag = FilesFragment()
        val historyFrag = HistoryFragment()
        binding.dasViewPager.offscreenPageLimit= 2
        val newAdapter = MainPagerAdapter(
            supportFragmentManager,
            3,
            homeFrag,
            fileFrag,
            historyFrag
        )
        binding.dasViewPager.adapter = newAdapter
        binding.dasViewPager.currentItem = 1
        binding.dasViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        clickFiles()
                    }
                    1 -> {
                        clickHome()
                    }
                    2 -> {
                        clickHistory()
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        binding.btnHome.setOnClickListener {
            clickHome()
            binding.dasViewPager.currentItem = 1
        }

        binding.btnFiles.setOnClickListener {
            clickFiles()
            binding.dasViewPager.currentItem = 0

        }

        binding.btnHistory.setOnClickListener {
            clickHistory()
            binding.dasViewPager.currentItem = 2

        }

    }

    private fun clickHome(){

        window.statusBarColor = getColor(R.color.color_blue)
        window.decorView.systemUiVisibility = 0

        binding.imgHome.setColorFilter(getColor(R.color.color_blue), PorterDuff.Mode.SRC_IN)
        binding.tvHome.setTextColor(getColor(R.color.color_blue))

        binding.imgBtnFiles.setColorFilter(getColor(R.color.tab_un), PorterDuff.Mode.SRC_IN)
        binding.tvBtnFiles.setTextColor(getColor(R.color.tab_un))

        binding.imgBtnHis.setColorFilter(getColor(R.color.tab_un), PorterDuff.Mode.SRC_IN)
        binding.tvBtnHis.setTextColor(getColor(R.color.tab_un))
    }

    private fun clickFiles(){
        window.statusBarColor = Color.WHITE
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        binding.imgHome.setColorFilter(getColor(R.color.tab_un), PorterDuff.Mode.SRC_IN)
        binding.tvHome.setTextColor(getColor(R.color.tab_un))

        binding.imgBtnFiles.setColorFilter(getColor(R.color.tab_selected), PorterDuff.Mode.SRC_IN)
        binding.tvBtnFiles.setTextColor(getColor(R.color.tab_selected))

        binding.imgBtnHis.setColorFilter(getColor(R.color.tab_un), PorterDuff.Mode.SRC_IN)
        binding.tvBtnHis.setTextColor(getColor(R.color.tab_un))
    }
    private fun clickHistory(){

        window.statusBarColor = Color.WHITE
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        binding.imgHome.setColorFilter(getColor(R.color.tab_un), PorterDuff.Mode.SRC_IN)
        binding.tvHome.setTextColor(getColor(R.color.tab_un))

        binding.imgBtnFiles.setColorFilter(getColor(R.color.tab_un), PorterDuff.Mode.SRC_IN)
        binding.tvBtnFiles.setTextColor(getColor(R.color.tab_un))

        binding.imgBtnHis.setColorFilter(getColor(R.color.tab_selected), PorterDuff.Mode.SRC_IN)
        binding.tvBtnHis.setTextColor(getColor(R.color.tab_selected))
    }

    companion object {
        fun getIntentForDashboard(context: Context): Intent {
            return  Intent(context,DashboardActivity::class.java)
        }
    }

}