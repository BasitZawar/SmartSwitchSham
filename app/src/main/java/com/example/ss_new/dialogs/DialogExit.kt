package com.example.ss_new.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ss_new.R
import com.example.ss_new.ads.AppOpenAdManager
import com.example.ss_new.databinding.DialogExitBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DialogExit : BottomSheetDialogFragment() {

    val binding by lazy {
        DialogExitBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.btnExit.setOnClickListener {
            AppOpenAdManager().isOverTimed = false
            activity?.finishAffinity()
        }

        binding.btnNo.setOnClickListener {
            dismiss()
        }
    }

}