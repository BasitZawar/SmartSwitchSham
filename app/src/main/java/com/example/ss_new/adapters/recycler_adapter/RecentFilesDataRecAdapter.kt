package com.example.ss_new.adapters.recycler_adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.ss_new.R
import com.example.ss_new.app_utils.AllFilesUtils
import com.example.ss_new.app_utils.AllFilesUtils.convertTime
import com.example.ss_new.app_utils.FileSelectionListener
import timber.log.Timber
import java.io.File

class RecentFilesDataRecAdapter(var list: ArrayList<AllFilesUtils.RecentFileModel>, var context: Context, private var listener: FileSelectionListener) : RecyclerView.Adapter<RecentFilesDataRecAdapter.RecentViewHolder>() {


    class RecentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val date = itemView.findViewById<TextView>(R.id.tvRecentFileDate)
        val size = itemView.findViewById<TextView>(R.id.tvRecentFileSize)
        val title = itemView.findViewById<TextView>(R.id.tvRecentFileTitle)
        val img  = itemView.findViewById<ImageView>(R.id.imgRecentThumb)
        val type  = itemView.findViewById<TextView>(R.id.tvRecentFileType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentViewHolder {
        return RecentViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_recent_files, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setData(list: ArrayList<AllFilesUtils.RecentFileModel>){
        this.list = list
        notifyDataSetChanged()
    }
    override fun onBindViewHolder(holder: RecentViewHolder, position: Int) {
        holder.title.text = list[position].title
        holder.type.text = list[position].date
        holder.date.text = convertTime(File(list[position].path).lastModified())
        holder.size.text = formatBytes(File(list[position].path).length().toFloat())
        Timber.e("${list[position].date} date")
        when (AllFilesUtils.getFileType(File(list[position].path))) {
            AllFilesUtils.video, AllFilesUtils.image -> {
                Glide.with(context).load(list[position].path)
                    .transform(CenterCrop(), RoundedCorners(25))
                    .placeholder(R.drawable.baseline_error_24)
                    .into(holder.img)
            }
            AllFilesUtils.docs -> {
                holder.img.setImageResource(R.drawable.icon_docs_list)
            }
            AllFilesUtils.audio -> {
                holder.img.setImageResource(R.drawable.icon_music_list)
            }
            AllFilesUtils.apk -> {
                holder.img.setImageResource(R.drawable.icon_apk_list)
            }
        }

        holder.itemView.setOnClickListener {
            listener.clicked(list[position].path)
        }

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