package com.example.firststep2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_calendarbottom.view.*

class adapter_CalendarBottom(val mainActivity: CalendarActivity) :
    RecyclerView.Adapter<ViewHolderHelper>() {
    var itemlist = arrayListOf<item_CalendarBottom>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderHelper {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_calendarbottom, parent, false)
        return ViewHolderHelper(view)
    }

    override fun getItemCount(): Int {
        return itemlist.size
    }

    override fun onBindViewHolder(holder: ViewHolderHelper, position: Int) {
        var currentItem = itemlist[position]

        holder.itemView.tv_title.text = currentItem.Title
        holder.itemView.tv_time.text = currentItem.Time
    }
}
