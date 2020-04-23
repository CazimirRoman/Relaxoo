package com.cazimir.relaxoo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.model.MoreApp
import com.squareup.picasso.Picasso

class MoreAppsAdapter(val context: Context, private val list: List<MoreApp>) : RecyclerView.Adapter<MoreAppsAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val rowView = LayoutInflater.from(parent.context).inflate(R.layout.item_more_apps, parent, false)
        return CustomViewHolder(rowView)
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item = list.get(position)
        holder.picasso.load(item.logoUrl).into(holder.logo)
        holder.description.text = item.description
    }

    class CustomViewHolder(val rowView: View, var item: MoreApp? = null) : RecyclerView.ViewHolder(rowView) {

        var logo: ImageView
        var description: AppCompatTextView
        val picasso = Picasso.get()

        init {

            logo = rowView.findViewById(R.id.appLogo)
            description = rowView.findViewById(R.id.appDescription)

            rowView.setOnClickListener {
                // println(item)
            }
        }
    }
}
