package com.example.firststep2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_calendarbottom.view.*

class adapter_CalendarBottom(val Activity: CalendarActivity) :
    RecyclerView.Adapter<ViewHolderHelper>() {
    // 캘린더 액티비티의 하단에 입력되는 리사이클러뷰의 어댑터 클래스. 유저의 일정을 뿌려준다

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
