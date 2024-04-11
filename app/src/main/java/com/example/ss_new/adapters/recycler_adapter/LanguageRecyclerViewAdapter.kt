package com.example.ss_new.adapters.recycler_adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.ss_new.R


class LanguageRecyclerViewAdapter (val context: Context, private val localeList: ArrayList<LocaleModel>, var listener: OnItemsClickListener1):
    Adapter<LanguageRecyclerViewAdapter.ViewHolder>() {

    private var selectedItemPosition: Int = RecyclerView.NO_POSITION

    interface OnItemsClickListener1 {
        fun onItemClick(pos: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_locale_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.country.text = localeList[position].language
        Glide.with(context).load(localeList[position].img).transform(
            CenterCrop(),
            RoundedCorners(15)
        ).into(holder.flags)
        if (localeList[position].isSelected || position == selectedItemPosition) {

            holder.itemView.background = ContextCompat.getDrawable(context,R.drawable.shape_round_8_languages)

        } else {
            holder.itemView.background = ContextCompat.getDrawable(context,R.drawable.shape_round_8)

        }
        holder.itemView.setOnClickListener{

            selectedItemPosition = position

            for (item in localeList) {
                item.isSelected = false
            }
            localeList[position].isSelected = true
            listener.onItemClick(position)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return localeList.size
    }
    class ViewHolder(ItemView: View): RecyclerView.ViewHolder(ItemView){
        val country: TextView = itemView.findViewById(R.id.tvCountries)
        val flags: ImageView = itemView.findViewById(R.id.imgCountries)
    }

    data class LocaleModel(var img : Int, var language: String, var langCode:String, var isSelected : Boolean)
}