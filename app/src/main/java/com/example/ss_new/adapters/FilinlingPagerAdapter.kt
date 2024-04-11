package com.example.ss_new.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.ss_new.fragments.files_fragment.*

class FilinlingPagerAdapter(fragmentManager: FragmentManager, var behavior:Int, var img: ImageFragment, var music: MusicFragment,
                            var video: VideosFragment, var doc: DocsFragment, var down: DownloadsFragment,
                            var app: AppsFragment, var apk: ApkFragment) : FragmentPagerAdapter(fragmentManager) {


    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> img
            1 -> music
            2 -> video
            3 -> doc
            4 -> down
            5 -> app
            6 -> apk
            else -> img
        }
    }

    override fun getCount(): Int {
        return behavior
    }

}