package com.example.firststep2

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_schedule.view.*
import java.util.*

class adapter_CalendarTop(val Activity: CalendarActivity) :
    RecyclerView.Adapter<ViewHolderHelper>() {
    // 캘린더 액티비티의 캘린더를 표시하는 리사이클러뷰 어댑터. 인자로 액티비티를 받는 이유는 해당 액티비티의 메소드를 사용하기 위함
    val calendar = GregorianCalendar(Locale.KOREA)
    var nYear = calendar.get(Calendar.YEAR)
    var nMonth = calendar.get(Calendar.MONTH)
    var nDay = calendar.get(Calendar.DAY_OF_MONTH)
    //  현재 시간 입력받아서 표시하고, 이후에 데이터 변경시 변수값으로 컨트롤하기 위해 var로 지정


    val baseCalendar = item_CalendarTop()

    init {
        baseCalendar.initBaseCalendar {
            refreshView(it)
            // 어댑터 초기화시 이번달 데이터를 입력하고 리사이클러뷰와 액티비티에 알리는 refreshView 메소드 실행
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderHelper {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_schedule, parent, false)
        return ViewHolderHelper(view)
    }

    override fun getItemCount(): Int {
        // 데이터의 카운트 값은 캘린더의 가로 * 세로
        return item_CalendarTop.LOW_OF_CALENDAR * item_CalendarTop.DAYS_OF_WEEK
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolderHelper, position: Int) {
        if (position % item_CalendarTop.DAYS_OF_WEEK == 0) holder.itemView.tv_date.setTextColor(
            Color.parseColor(
                "#ff1200"
            )
        )
        // 일요일은 빨간색으로 표시
        else holder.itemView.tv_date.setTextColor(Color.parseColor("#676d6e"))
        // 다른날은 진회색으로 표시

        if (position < baseCalendar.prevMonthTailOffset || position >= baseCalendar.prevMonthTailOffset + baseCalendar.currentMonthMaxDate) {
            // 이전달 꼬리 혹은 다음달 머리 일 경우를 구분하기위해 레이아웃의 투명도를 낮춘다.
            holder.itemView.calendarlayout.alpha = 0.3f
        } else {
            holder.itemView.calendarlayout.alpha = 1f
        }

        holder.itemView.tv_date.text = baseCalendar.data[position].Day.toString()

        if (baseCalendar.data[position].Year == nYear && baseCalendar.data[position].Month == nMonth && baseCalendar.data[position].Day == nDay) {
            holder.itemView.calendarlayout.setBackgroundColor(0x40D3BBBB)
            // 최초 실행시 오늘 일자를 표시하고 캘린더 클릭시 해당 일자를 표시한다.
        } else {
            holder.itemView.calendarlayout.setBackgroundColor(android.R.color.white)
        }


        holder.itemView.setOnClickListener {
            nYear = baseCalendar.data[position].Year
            nMonth = baseCalendar.data[position].Month
            nDay = baseCalendar.data[position].Day
            calendar.set(nYear, nMonth, nDay)

            Activity.refreshBottomData(Activity.userid, calendar)

            if (position < baseCalendar.prevMonthTailOffset) {
                changeToPrevMonth()
                // 이전달 데이터 클릭시 해당 월로 이동
            } else if (position >= baseCalendar.prevMonthTailOffset + baseCalendar.currentMonthMaxDate) {
                changeToNextMonth()
                // 다음달 데이터 클릭시 해당 월로 이동
            } else {
                refreshView(calendar)
            }

        }


    }


    fun changeToPrevMonth() {
        // 이전달 버튼 클릭했을때 데이터 변경
        baseCalendar.changeToPrevMonth {
            refreshView(it)
        }
    }

    fun changeToNextMonth() {
        // 다음달 버튼 클릭했을때 데이터 변경
        baseCalendar.changeToNextMonth {
            refreshView(it)
        }
    }


    private fun refreshView(calendar: Calendar) {
        // 데이터 변경시 리사이클러뷰에 알려주고 액티비티의 리프레쉬 메소드 사용
        notifyDataSetChanged()
        Activity.refreshCurrentMonth(calendar)
    }
}