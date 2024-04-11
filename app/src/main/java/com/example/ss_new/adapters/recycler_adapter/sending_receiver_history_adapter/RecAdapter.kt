package com.example.ss_new.adapters.recycler_adapter.sending_receiver_history_adapter

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
import com.example.ss_new.database.FilesEntity
import java.io.File

class RecAdapter(private var items: ArrayList<FilesEntity>,var context: Context) : RecyclerView.Adapter<RecAdapter.FileViewHolder>() {


    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        fun bind(position:Int){
            itemView.findViewById<TextView>(R.id.tvFileNameHistory).text = File(items[position].path).name.toString()
            itemView.findViewById<TextView>(R.id.tvCatNameHis).text = items[position].fileType
            itemView.findViewById<TextView>(R.id.tvDateHis).text = items[position].date
            val img   = itemView.findViewById<ImageView>(R.id.imgHistory)
            when(items[position].fileType){
                "Video" ->{
                    Glide.with(context).load(items[position].path).transform(
                        CenterCrop(),
                        RoundedCorners(15)
                    ).placeholder(R.drawable.icon_video_list).into(img)
                }
                "Audio" ->{
                    img.setImageResource(R.drawable.icon_music_list)
                }
                "Image" ->{
                    Glide.with(context).load(items[position].path)
                        .transform(CenterCrop(),RoundedCorners(15)).placeholder(R.drawable.icon_img_list).into(img)
                }
                "Doc" ->{
                    img.setImageResource(R.drawable.icon_docs_list)
                }
                "Apk" ->{
                    img.setImageResource(R.drawable.icon_apk_list)
                }
                "App" -> {
                    img.setImageResource(R.drawable.icon_apps_list)
                }
                "download" -> {
                    img.setImageResource(R.drawable.icon_download_list)
                }


            }


        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return  FileViewHolder(
                inflater.inflate(
                    R.layout.item_history,
                    parent,
                    false
                ))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(position)
    }



    fun setData(it: ArrayList<FilesEntity>) {
        items = it
        notifyDataSetChanged()
    }

}