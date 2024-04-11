package com.example.ss_new.fragments.dashboard_frags

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.ss_new.activites.FileToSelectActivity.Companion.getIntentFileExplorerActivity
import com.example.ss_new.activites.Splash
import com.example.ss_new.ads.InterstitialHelper
import com.example.ss_new.ads.NativeAdManager
import com.example.ss_new.databinding.FragmentFilesBinding
import com.example.ss_new.app_utils.AllFilesUtils

class FilesFragment : Fragment() {

    lateinit var binding: FragmentFilesBinding
    var count = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFilesBinding.inflate(layoutInflater)
        if (!AllFilesUtils.isSubscribed(requireActivity())) {
            NativeAdManager(requireActivity()).loadNative(requireActivity(), binding.adView)
        } else {
            binding.adView.visibility = View.GONE
        }
        binding.btnVideos.setOnClickListener {
            if (Splash.Companion.appHavePermissions(requireActivity())) {
                if (count % 2 == 0) {
                    InterstitialHelper.showSplashInterstitial(requireActivity(), object :
                        InterstitialHelper.InterstitialListener {
                        override fun onAdDismiss() {
                            startActivity(
                                getIntentFileExplorerActivity(
                                    requireActivity(),
                                    AllFilesUtils.video
                                )
                            )
                            count++
                        }
                    })
                } else {
                    startActivity(
                        getIntentFileExplorerActivity(
                            requireActivity(),
                            AllFilesUtils.video
                        )
                    )
                    count++
                }

            } else {
                Toast.makeText(
                    requireActivity(), "Please grant permissions to use this feature",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.btnMusic.setOnClickListener {
            if (Splash.Companion.appHavePermissions(requireContext())) {
//                if (count % 2 == 0) {
//                    InterstitialHelper.showSplashInterstitial(requireActivity(), object :
//                        InterstitialHelper.InterstitialListener {
//                        override fun onAdDismiss() {
//                            startActivity(
//                                getIntentFileExplorerActivity(
//                                    requireActivity(),
//                                    AllFilesUtils.audio
//                                )
//                            )
//                        }
//                    })
//                    count++
//                } else {
                    startActivity(
                        getIntentFileExplorerActivity(
                            requireActivity(),
                            AllFilesUtils.audio
                        )
                    )
                    count++
//                }

            } else {
                Toast.makeText(
                    requireActivity(),
                    "Please grant permissions to use this feature",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.btnDocs.setOnClickListener {
            if (Splash.Companion.appHavePermissions(requireActivity())) {
                if (count % 2 == 0) {
                    InterstitialHelper.showSplashInterstitial(requireActivity(), object :
                        InterstitialHelper.InterstitialListener {
                        override fun onAdDismiss() {
                            startActivity(
                                getIntentFileExplorerActivity(
                                    requireActivity(),
                                    AllFilesUtils.docs
                                )
                            )
                        }
                    })
                    count++
                } else {
                    startActivity(
                        getIntentFileExplorerActivity(
                            requireActivity(),
                            AllFilesUtils.docs
                        )
                    )
                    count++
                }

            } else {
                Toast.makeText(
                    requireActivity(),
                    "Please grant permissions to use this feature",
                    Toast.LENGTH_SHORT
                ).show()

            }
        }
        binding.btnImages.setOnClickListener {
            if (Splash.Companion.appHavePermissions(requireActivity())) {
                if (count % 2 == 0) {
                    InterstitialHelper.showSplashInterstitial(requireActivity(), object :
                        InterstitialHelper.InterstitialListener {
                        override fun onAdDismiss() {
                            startActivity(
                                getIntentFileExplorerActivity(
                                    requireActivity(),
                                    AllFilesUtils.image
                                )
                            )
                        }
                    })
                    count++
                } else {
                    startActivity(
                        getIntentFileExplorerActivity(
                            requireActivity(),
                            AllFilesUtils.image
                        )
                    )
                    count++
                }

            } else {
                Toast.makeText(
                    requireActivity(),
                    "Please grant permissions to use this feature",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.btnDownloads.setOnClickListener {
            if (Splash.Companion.appHavePermissions(requireActivity())) {
//                if (count % 2 == 0) {
//                    InterstitialHelper.showSplashInterstitial(requireActivity(), object :
//                        InterstitialHelper.InterstitialListener {
//                        override fun onAdDismiss() {
//                            startActivity(
//                                getIntentFileExplorerActivity(
//                                    requireActivity(),
//                                    AllFilesUtils.download
//                                )
//                            )
//
//                        }
//                    })
//                    count++
//                } else {
                    startActivity(
                        getIntentFileExplorerActivity(
                            requireActivity(),
                            AllFilesUtils.download
                        )
                    )
                    count++
//                }

            } else {
                Toast.makeText(
                    requireActivity(),
                    "Please grant permissions to use this feature",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.btnApps.setOnClickListener {
            if (Splash.Companion.appHavePermissions(requireActivity())) {
                if (count % 2 == 0) {
                    InterstitialHelper.showSplashInterstitial(requireActivity(), object :
                        InterstitialHelper.InterstitialListener {
                        override fun onAdDismiss() {
                            startActivity(
                                getIntentFileExplorerActivity(
                                    requireActivity(),
                                    AllFilesUtils.app
                                )
                            )
                        }
                    })
                    count++
                } else {
                    startActivity(
                        getIntentFileExplorerActivity(
                            requireActivity(),
                            AllFilesUtils.app
                        )
                    )
                    count++
                }

            } else {
                Toast.makeText(
                    requireActivity(),
                    "Please grant permissions to use this feature",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.btnApk.setOnClickListener {
            if (Splash.Companion.appHavePermissions(requireActivity())) {
//                if (count % 2 == 0) {
//                    InterstitialHelper.showSplashInterstitial(requireActivity(), object :
//                        InterstitialHelper.InterstitialListener {
//                        override fun onAdDismiss() {
//                            startActivity(
//                                getIntentFileExplorerActivity(
//                                    requireActivity(),
//                                    AllFilesUtils.apk
//                                )
//                            )
//                        }
//                    })
//                    count++
//                } else {
                    startActivity(
                        getIntentFileExplorerActivity(
                            requireActivity(),
                            AllFilesUtils.apk
                        )
                    )
                    count++
//                }
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
}
