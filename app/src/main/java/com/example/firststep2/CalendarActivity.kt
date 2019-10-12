package com.example.firststep2

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_calendar.*
import kotlinx.android.synthetic.main.activity_calendar.ib_calendar
import kotlinx.android.synthetic.main.activity_calendar.ib_chat
import kotlinx.android.synthetic.main.activity_calendar.ib_checklist
import kotlinx.android.synthetic.main.activity_calendar.ib_dog
import kotlinx.android.synthetic.main.activity_calendar.ib_setting
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {

    lateinit var scheduleRecyclerViewAdapter: adapter_CalendarTop
    lateinit var bottomRecyclerViewAdapter: adapter_CalendarBottom
    lateinit var db:AppDatabase
    lateinit var userid:String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        // 로그인한 유저 정보를 쉐어드 프리퍼런스에서 가져온다
        val prefs = applicationContext.getSharedPreferences("userdata", Context.MODE_PRIVATE)
        userid = prefs.getString("id", "")
        var nickname = prefs.getString("nickname", "")
        var profile = prefs.getString("profile", "")
        var gold = prefs.getString("gold", "")


        db = Room.databaseBuilder(
            applicationContext, AppDatabase::class.java, "Database"
        ).allowMainThreadQueries().build()
        // 하단 리사이클러뷰에서 사용할 룸 데이터베이스를 가지고온다


        // 바텀 네비게이션을 클릭하면 해당 액티비티로 이동한다
        ib_dog.setOnClickListener { Singleton.bottomMove(this, 0, this) }
        ib_calendar.setOnClickListener { Singleton.bottomMove(this, 1, this) }
        ib_checklist.setOnClickListener { Singleton.bottomMove(this, 2, this) }
        ib_chat.setOnClickListener { Singleton.bottomMove(this, 3, this) }
        ib_setting.setOnClickListener { Singleton.bottomMove(this, 4, this) }

        scrollview.setOnScrollChangeListener { NestedScrollView: NestedScrollView, scrollX: Int, scrollY: Int, oldx: Int, oldy: Int ->

            if (scrollY in 301..800) {
                // 스크롤 중일때 해당 방향으로 이동한다.
                // 이후에 플링으로 변경 예정.

                if (scrollY > oldy) {
//                scrollview.fullScroll(View.FOCUS_DOWN)
                    ObjectAnimator.ofInt(scrollview, "scrollY", scrollview.bottom)
                        .setDuration(0.1.toLong()).start()
                }
                if (scrollY < oldy) {
//                scrollview.fullScroll(View.FOCUS_DOWN)
                    ObjectAnimator.ofInt(scrollview, "scrollY", scrollview.top)
                        .setDuration(0.1.toLong()).start()
                }
            }

            if (!scrollview.canScrollVertically(-1)) {
                println("확인1 : scrollX = $scrollX, scrollY = $scrollY, oldx = $oldx, oldy = $oldy")
                topDefault.visibility = View.VISIBLE
                topViewBottom.visibility = View.GONE
                // 스크롤뷰가 상단에 닿았을때 체크
            }

            if (!scrollview.canScrollVertically(1)) {
                println("확인2 : scrollX = $scrollX, scrollY = $scrollY, oldx = $oldx, oldy = $oldy")
                topDefault.visibility = View.GONE
                topViewBottom.visibility = View.VISIBLE
                // 스크롤뷰가 하단에 닿았을때 체크
            }


        }


        initView() // 리사이클러뷰를 가져오는 메소드
    }

    fun initView() {

        scheduleRecyclerViewAdapter = adapter_CalendarTop(this)
        // 리사이클러뷰 어댑터를 붙여준다. 이 액티비티를 인자로 넣는 이유는 refreshCurrentMonth 메소드를 사용하기 위해서

        rv_schedule.layoutManager =
            GridLayoutManager(this, item_CalendarTop.DAYS_OF_WEEK) // 당연히 그리드 레이아웃. 넓이는 1주일
        rv_schedule.adapter = scheduleRecyclerViewAdapter // 위에서 만든 어댑터를 리사이클러뷰에 넣어준다
//        rv_schedule.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL))
        rv_schedule.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        // 가로 세로 구분선을 넣어서 캘린더 느낌을 낸다

        tv_prev_month.setOnClickListener {
            scheduleRecyclerViewAdapter.changeToPrevMonth()
        } // 이전달로 이동 클릭시 어댑터 내부 메소드를 사용하여 이동한다.

        tv_next_month.setOnClickListener {
            scheduleRecyclerViewAdapter.changeToNextMonth()
        } // 다음달로 이동 클릭시 어댑터 내부 메소드를 사용하여 이동한다

        tv_prev_date.setOnClickListener {
            changeToPrevDate()
        } // 이전달로 이동 클릭시 어댑터 내부 메소드를 사용하여 이동한다.

        tv_next_date.setOnClickListener {
            changeToNextDate()
        } // 다음달로 이동 클릭시 어댑터 내부 메소드를 사용하여 이동한다

        bottomRecyclerViewAdapter = adapter_CalendarBottom(this)
        rv_bottom.layoutManager = LinearLayoutManager(this)
        rv_bottom.adapter = bottomRecyclerViewAdapter
        rv_bottom.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        // 하단 리사이클러뷰를 가져온다

        refreshCurrentMonth(scheduleRecyclerViewAdapter.calendar)



    }


    fun changeToPrevDate() {
        // 이전일로 가기 버튼 클릭했을때 데이터 변경
        var calendar = scheduleRecyclerViewAdapter.calendar
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE)-1)
        scheduleRecyclerViewAdapter.nYear = calendar.get(Calendar.YEAR)
        scheduleRecyclerViewAdapter.nMonth = calendar.get(Calendar.MONTH)
        scheduleRecyclerViewAdapter.nDay = calendar.get(Calendar.DATE)
        refreshCurrentMonth(calendar)
        refreshBottomData(userid, calendar)
        rv_schedule.adapter!!.notifyDataSetChanged()
        rv_bottom.adapter!!.notifyDataSetChanged()
    }

    fun changeToNextDate() {
        // 다음일로 가기 버튼 클릭했을때 데이터 변경
        var calendar = scheduleRecyclerViewAdapter.calendar
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE)+1)
        scheduleRecyclerViewAdapter.nYear = calendar.get(Calendar.YEAR)
        scheduleRecyclerViewAdapter.nMonth = calendar.get(Calendar.MONTH)
        scheduleRecyclerViewAdapter.nDay = calendar.get(Calendar.DATE)
        refreshCurrentMonth(calendar)
        refreshBottomData(userid, calendar)
        rv_schedule.adapter!!.notifyDataSetChanged()
        rv_bottom.adapter!!.notifyDataSetChanged()
    }


    fun refreshCurrentMonth(calendar: Calendar) {
        // 이전달로 이동 혹은 다음달로 이동 클릭시 캘린더 상단의 이번달 표시도 바꿔줌

        val sdf = SimpleDateFormat("yyyy MM", Locale.KOREAN)
        tv_current_month.text = sdf.format(calendar.time)

        val sdf2 = SimpleDateFormat("yyyy MM dd", Locale.KOREAN)
        tv_current_date.text = sdf2.format(calendar.time)
//        refreshBottomData(userid, scheduleRecyclerViewAdapter.calendar)

    }

    fun refreshBottomData(userid: String, calendar: GregorianCalendar) {
        // 날짜가 바뀌었을때 아래화면 데이터 최신화
        /* 이건 나중에
           5. 서버에 데이터 정확성을 체크하고 다른점이 있다면
            5-1. 데이터의 변경 일자를 확인해서 어느 데이터가 최신인지 확인한다
            5-2. 서버의 데이터가 최신이라면 룸에 데이터를 반영한다
            5-3. 배열을 비우고 데이터를 다시 입력한다
            5-4. 반대로 룸의 데이터가 최신이라면 서버에 데이터를 반영한다.
         */

        bottomRecyclerViewAdapter.itemlist.clear() // 기존 배열을 비운다


        var mYear = calendar.get(Calendar.YEAR)
        var mMonth = calendar.get(Calendar.MONTH)
        var mDay = calendar.get(Calendar.DATE)
        // 정보를 가지고 올 일자를 지정한다.

        CoroutineScope(Dispatchers.Main).launch {

            var Data =
                db.RoomCalendarDao().getData(userid, mYear, mMonth, mDay) // 해당 일자의 데이터를 룸에서 가지고온다

            if (Data.size > 0) {

                for (i in Data.indices) {
                    var tmp = item_CalendarBottom(
                        Data[i].key!!,
                        Data[i].id,
                        Data[i].datanum,
                        Data[i].year,
                        Data[i].month,
                        Data[i].day,
                        Data[i].Time,
                        Data[i].Title
                    )

                    bottomRecyclerViewAdapter.itemlist.add(tmp) // 배열에 해당 일자의 데이터를 입력한다
                }
                rv_bottom.adapter!!.notifyDataSetChanged() // 바텀리사이클러뷰에 데이터 변경을 알린다
            }

        }


    }

    override fun onResume() {
        // 액티비티가 실행될때 데이터를 다시 불러온다
        super.onResume()
        refreshBottomData(userid, scheduleRecyclerViewAdapter.calendar)
    }

    fun addCalendar(view: View) {
        // 일정 추가 버튼 클릭시 해당 액티비티로 이동한다
        val intent = Intent(this, AddcalendarActivity::class.java)
        startActivity(intent)
    }

}
