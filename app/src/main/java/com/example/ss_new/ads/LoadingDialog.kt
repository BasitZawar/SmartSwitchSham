package com.example.ss_new.ads

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.example.ss_new.R


import java.lang.IllegalArgumentException

object LoadingDialog {

    var dialog: Dialog? = null

    fun showLoadingDialog(context: Activity?) {
        if (dialog == null) {
            dialog = context?.let { Dialog(it,android.R.style.Theme_Black_NoTitleBar_Fullscreen) }
            dialog?.setContentView(R.layout.dialog_loading)
            dialog?.setCancelable(false)
        }
        if (dialog?.isShowing == false)
            dialog?.show()


    }

    fun hideLoadingDialog() {
        try {
            dialog?.dismiss()
            dialog = null
        } catch (_: IllegalArgumentException) {
        }
    }
}