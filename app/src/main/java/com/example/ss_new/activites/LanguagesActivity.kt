package com.example.ss_new.activites

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ss_new.R
import com.example.ss_new.adapters.recycler_adapter.LanguageRecyclerViewAdapter
import com.example.ss_new.ads.NativeAdManager
import com.example.ss_new.app_utils.AllFilesUtils
import com.example.ss_new.databinding.ActivityLanguageBinding


class LanguagesActivity : AppCompatActivity(), LanguageRecyclerViewAdapter.OnItemsClickListener1 {
    lateinit var binding: ActivityLanguageBinding
    lateinit var adapter: LanguageRecyclerViewAdapter
    private var localeList: ArrayList<LanguageRecyclerViewAdapter.LocaleModel> = arrayListOf()
    var selectedLanguage = "en"
    lateinit var prefs: SharedPreferences
    lateinit var ed: Editor
    var isFromSplash = false
    var fromHomeFragment = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = getSharedPreferences("MyAppPreferences", MODE_PRIVATE)
        ed = prefs.edit()

        isFromSplash = intent.getBooleanExtra("isFromSplash", false)
        fromHomeFragment = intent.getBooleanExtra("fromHomeFragment", false)

        if (!AllFilesUtils.isSubscribed(this)) {
            NativeAdManager(this).loadNative(this, binding.adView)
        } else {
            binding.adView.visibility = View.GONE
        }

        val tempLocaleList = countriesArrayList()
//        val selectedLang = AppUtils.getPrefsString(this, AppUtils.languageKey, AppUtils.defaultLang)
        val selectedLang = prefs.getString("selected_language", selectedLanguage)
        for (temp in tempLocaleList) {
            if (temp.langCode == selectedLang) {
                localeList.add(
                    LanguageRecyclerViewAdapter.LocaleModel(
                        temp.img,
                        temp.language,
                        temp.langCode,
                        true
                    )
                )
            } else {
                localeList.add(
                    LanguageRecyclerViewAdapter.LocaleModel(
                        temp.img,
                        temp.language,
                        temp.langCode,
                        false
                    )
                )
            }
        }

        adapter = LanguageRecyclerViewAdapter(this, localeList, this)
        binding.recView.layoutManager = LinearLayoutManager(this)
        binding.recView.adapter = adapter

        binding.btnSetLanguage.setOnClickListener {

//            if (prefs.getBoolean("from_activity", true)) {
            if (prefs.getBoolean("from_activity", true)) {
                ed.putBoolean("from_activity", false).apply()
                ed.putString("selected_language", selectedLanguage).apply()
                setLocalLanguages(selectedLanguage)
                startActivity(DashboardActivity.getIntentForDashboard(this@LanguagesActivity))
//                startActivity(Intent(applicationContext, SubscriptionActivity::class.java))
                finish()
            } else {
                ed.putString("selected_language", selectedLanguage).apply()
                setLocalLanguages(selectedLanguage)
                startActivity(Intent(applicationContext, DashboardActivity::class.java))

                finish()
            }

        }

        binding.btnBack.setOnClickListener {
            finish()
        }


    }

    private fun countriesArrayList(): ArrayList<LanguageRecyclerViewAdapter.LocaleModel> {
        val tempData = arrayOf(
            Triple(R.drawable.english, "English", "en"),
            Triple(R.drawable.arabic, "Arabic", "ar"),
            Triple(R.drawable.china, "Chinese", "zh"),
            Triple(R.drawable.french, "French", "fr"),
            Triple(R.drawable.german, "German", "de"),
            Triple(R.drawable.hindi, "Hindi", "hi"),
            Triple(R.drawable.indonesian, "Indonesian", "id"),
            Triple(R.drawable.portuguese, "Portuguese", "pt"),
            Triple(R.drawable.spanish, "Spanish", "es"),
            Triple(R.drawable.urdu, "Urdu", "ur")
        )

        val tempList = ArrayList<LanguageRecyclerViewAdapter.LocaleModel>()

        for ((drawableRes, name, code) in tempData) {
            tempList.add(LanguageRecyclerViewAdapter.LocaleModel(drawableRes, name, code, false))
        }

        return tempList
    }

    fun setLocalLanguages(language: String) {
        val appLocale = LocaleListCompat.forLanguageTags(language)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    companion object {
        fun setLocale(context: Context) {
            val prefs = context.getSharedPreferences("MyAppPreferences", MODE_PRIVATE)
            val getSavedKey = prefs.getString("selected_language", "en")
            LanguagesActivity().setLocalLanguages(getSavedKey!!)
        }
    }

    override fun onItemClick(pos: Int) {
        selectedLanguage = localeList[pos].langCode
    }
}