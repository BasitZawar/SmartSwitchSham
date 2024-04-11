package com.example.ss_new.activites

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.example.ss_new.subscription.BillingViewModel
import com.example.ss_new.R
import com.example.ss_new.app_utils.AllFilesUtils
import com.example.ss_new.databinding.ActivitySubscriptionBinding
import com.example.ss_new.subscription.Subscriptions

class SubscriptionActivity : AppCompatActivity() {
    lateinit var binding: ActivitySubscriptionBinding
    private lateinit var viewModel: BillingViewModel
    lateinit var prefs: SharedPreferences
    lateinit var ed: SharedPreferences.Editor
    var isFromSplash = false
    var fromHomeFragment =false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscriptionBinding.inflate(layoutInflater)
        window.statusBarColor = getColor(R.color.color_blue)
        setContentView(binding.root)
        prefs = getSharedPreferences("MyAppPreferences", MODE_PRIVATE)
        ed = prefs.edit()
        viewModel = ViewModelProvider(
            this, ViewModelProvider.AndroidViewModelFactory(application)
        ).get(BillingViewModel::class.java)

         isFromSplash = intent.getBooleanExtra("isFromSplash", false)
         fromHomeFragment = intent.getBooleanExtra("fromHomeFragment", false)

        if (isFromSplash) {
            binding.tvSkip.visibility = View.VISIBLE
            binding.btnBack.visibility = View.INVISIBLE
        } else {
            binding.tvSkip.visibility = View.INVISIBLE
            binding.btnBack.visibility = View.VISIBLE
        }
        if (fromHomeFragment) {
            binding.btnBack.visibility = View.VISIBLE
            binding.tvSkip.visibility = View.INVISIBLE
        }


        if (AllFilesUtils.isSubscribed(this)) {
            binding.tv3.visibility = View.GONE
        }

        binding.btnBack.setOnClickListener { onBackPressed() }
        initRadioButtons()
        iniHyperlink()
        initClickListener()
        binding.tvDescMonth.text = AllFilesUtils.monthly_price
        binding.tvDesc6Month.text = AllFilesUtils.six_month_price
        binding.tvDescYearly.text = AllFilesUtils.yearly_price


    }

    private fun initRadioButtons() {
        binding.rbMonthly.isChecked = prefs.getBoolean("rb_monthly", true)
        binding.rb6Month.isChecked = prefs.getBoolean("rb_6month", false)
        binding.rbYearly.isChecked = prefs.getBoolean("rb_yearly", false)
    }

    private fun initClickListener() {
        binding.conMonthly.setOnClickListener {
            binding.rbMonthly.isChecked = true
            binding.rb6Month.isChecked = false
            binding.rbYearly.isChecked = false
            ed.putBoolean("rb_monthly", true).apply()
            ed.putBoolean("rb_6month", false).apply()
            ed.putBoolean("rb_yearly", false).apply()
        }
        binding.con6Month.setOnClickListener {
            binding.rbMonthly.isChecked = false
            binding.rb6Month.isChecked = true
            binding.rbYearly.isChecked = false
            ed.putBoolean("rb_monthly", false).apply()
            ed.putBoolean("rb_6month", true).apply()
            ed.putBoolean("rb_yearly", false).apply()
        }
        binding.conYearly.setOnClickListener {
            binding.rbMonthly.isChecked = false
            binding.rb6Month.isChecked = false
            binding.rbYearly.isChecked = true
            ed.putBoolean("rb_monthly", false).apply()
            ed.putBoolean("rb_6month", false).apply()
            ed.putBoolean("rb_yearly", true).apply()
        }
        binding.tvSubscribe.setOnClickListener {
            if (prefs.getBoolean("rb_monthly", true)) {
                viewModel.billing.launchSubscribeFlow(this, Subscriptions.Monthly)
            } else if (prefs.getBoolean("rb_6month", false)) {
                viewModel.billing.launchSubscribeFlow(this, Subscriptions.SixMonth)
            } else if (prefs.getBoolean("rb_yearly", false)) {
                viewModel.billing.launchSubscribeFlow(this, Subscriptions.Annual)
            }

        }
        binding.tvContinueAds.setOnClickListener {
//            if (prefs.getBoolean("from_activity", true)) {
            if (isFromSplash) {
                startActivity(Intent(applicationContext, LanguagesActivity::class.java))
                finish()
            }
//            else if (AllFilesUtils.isFromActv) {
//                finish()
//            }
            else {
                AllFilesUtils.isFromActv = true
                startActivity(DashboardActivity.getIntentForDashboard(this@SubscriptionActivity))
                finish()
            }


        }
        binding.tvSkip.setOnClickListener {
//            startActivity(DashboardActivity.getIntentForDashboard(this@SubscriptionActivity))
//            if (prefs.getBoolean("from_activity", true)) {
            if (isFromSplash) {
                startActivity(Intent(applicationContext, LanguagesActivity::class.java))
                finish()
            }
//            else if (AllFilesUtils.isFromActv) {
//                finish()
//            }
            else {
                AllFilesUtils.isFromActv = true
                startActivity(DashboardActivity.getIntentForDashboard(this@SubscriptionActivity))
                finish()
            }

        }
    }

    private fun iniHyperlink() {
        binding.tvHeadlines.setClickable(true);

        //Handlle click event
        binding.tvHeadlines.setMovementMethod(LinkMovementMethod.getInstance());


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            binding.tvHeadlines.setText(
                Html.fromHtml(
                    "By containing, you agree to our\n" + " <a href='https://sites.google.com/view/privacypolicysmartswitchapp/home'>Privacy Policy</a> & <a href='https://payments.google.com/payments/apis-secure/u/0/get_legal_document?ldl=en_GB&ldo=0&ldt=buyertos'>Terms of Services</a>",
                    Html.FROM_HTML_MODE_LEGACY
                )
            );
        } else {
            binding.tvHeadlines.setText(
                Html.fromHtml(
                    "By containing, you agree to our\n" + " <a href='https://sites.google.com/view/privacypolicysmartswitchapp/home'>Privacy Policy</a> & <a href='https://payments.google.com/payments/apis-secure/u/0/get_legal_document?ldl=en_GB&ldo=0&ldt=buyertos'>Terms of Services</a>"
                )
            );
        }

    }

    private fun makeLinksClickable(spannable: CharSequence): Spannable {
        val spannableString = SpannableString.valueOf(spannable)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                // Handle the click action, e.g., open the URL in a browser
                val uri = Uri.parse(view.tag.toString())
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        }

        // Set the ClickableSpan to the entire text
        spannableString.setSpan(
            clickableSpan, 0, spannableString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Set the link color
        spannableString.setSpan(
            android.text.style.ForegroundColorSpan(resources.getColor(R.color.color_blue_launcher)),
            0,
            spannableString.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spannableString
    }

    override fun onResume() {
        super.onResume()
        if (AllFilesUtils.isSubscribed(this)) {
            binding.tv3.visibility = View.GONE
        }
    }

}