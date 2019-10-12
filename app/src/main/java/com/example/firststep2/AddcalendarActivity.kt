package com.example.firststep2

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_addcalendar.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddcalendarActivity : AppCompatActivity() {

    var startCal = GregorianCalendar.getInstance()
    var endCal = GregorianCalendar.getInstance()
    // 시작일과 종료일을 미리 불러온다

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addcalendar)

        startCal.set(Calendar.SECOND, 0)
        startCal.set(Calendar.MINUTE, 0)
        startCal.set(Calendar.HOUR, startCal.get(Calendar.HOUR) + 10)
        endCal.set(Calendar.SECOND, 0)
        endCal.set(Calendar.MINUTE, 0)
        endCal.set(Calendar.HOUR, endCal.get(Calendar.HOUR) + 11)
        // 시작일과 종료일을 초기화 해준다

        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.KOREAN)
        tv_startdate.text = sdf.format(startCal.time)
        tv_starttime.text = SimpleDateFormat("HH:mm", Locale.KOREAN).format(startCal.time)
        tv_enddate.text = sdf.format(endCal.time)
        tv_endtime.text = SimpleDateFormat("HH:mm", Locale.KOREAN).format(endCal.time)
        // 시작일과 종료일을 미리 텍스트뷰에 뿌려준다

    }

    fun back(view: View) {
        onBackPressed()
        // 취소버튼이나 상단 X버튼 눌렀을때 호출되는 메소드. 뒤로간다
    }


    fun correct(view: View) {
        // 확인버튼 눌렀을때 호출되는 메소드. 데이터를 룸과 서버에 입력한다

        val id = "121212" // todo 아이디를 쉐어드프리퍼런스에서 받아와서 넣는다
        var datanum = 0
        var year = startCal.get(Calendar.YEAR)
        var month = startCal.get(Calendar.MONTH)
        var day = startCal.get(Calendar.DATE)
        var Time = ""
        var Title = et_title.text.toString()
        var important = 0
        var oneDay = false

        var db = Room.databaseBuilder(
            applicationContext, AppDatabase::class.java, "Database"
        ).allowMainThreadQueries().build()

        // 시와 분은 미리 가공한다. 00분이 0분으로 나오는 문제 해결을 위해서.
        var startMin = ""
        if (startCal.get(Calendar.MINUTE) == 0) {
        } else {
            startMin = " " + startCal.get(Calendar.MINUTE).toString() + "분"
        }

        var endMin = ""
        if (endCal.get(Calendar.MINUTE) == 0) {
        } else {
            endMin = " " + endCal.get(Calendar.MINUTE).toString() + "분"
        }

        var startHour = ""
        if (startCal.get(Calendar.HOUR) < 10) {

            startHour = "0" + startCal.get(Calendar.HOUR).toString() + "시 "
        } else {
            startHour = "" + startCal.get(Calendar.HOUR).toString() + "시 "
        }

        var endHour = ""
        if (endCal.get(Calendar.HOUR) < 10) {
            endHour = "0" + endCal.get(Calendar.HOUR).toString() + "시"
        } else {
            endHour = "" + endCal.get(Calendar.HOUR).toString() + "시"
        }


        // 현재 유저의 가장 큰 datanum을 불러와서 +1을 저장한다
        CoroutineScope(Dispatchers.Main).launch {
            datanum =
                if (db.RoomCalendarDao().gethigh(id).isNotEmpty()) db.RoomCalendarDao().gethigh(id).get(
                    0
                ).datanum + 1
                else 0
        }

        if (startCal.get(Calendar.DAY_OF_YEAR) == endCal.get(Calendar.DAY_OF_YEAR)) {
            // 시작일과 종료일이 같을때 -> 일자만 표시함

            oneDay = true

            Time = "오늘 " + startHour + startMin + " ~ " + endHour + endMin

            important = 0

        } else if (startCal.get(Calendar.MONTH) == endCal.get(Calendar.MONTH)) {
            // 같은 월일때 -> 일과 시까지 표시함
            Time =
                "" + startCal.get(Calendar.DATE) + "일 " + startHour + "~ " + endCal.get(
                    Calendar.DATE
                ) + "일 " + endHour
            important = 1
        } else if (startCal.get(Calendar.YEAR) == endCal.get(Calendar.YEAR)) {
            // 같은 년도일때 -> 년월일만 표시함
            Time =
                "" + startCal.get(Calendar.MONTH) + "월 " + startCal.get(Calendar.DATE) + "일" + " ~ " + endCal.get(
                    Calendar.MONTH
                ) + "월 " + endCal.get(Calendar.DATE) + "일"

            important = 2
        } else {
            // 그외에 -> 년월일을 표시함
            Time =
                "" + startCal.get(Calendar.YEAR) + "년 " + startCal.get(Calendar.MONTH) + "월 " + startCal.get(
                    Calendar.DATE
                ) + "일" + " ~ " + endCal.get(Calendar.YEAR) + "년 " + endCal.get(Calendar.MONTH) + "월 " + endCal.get(
                    Calendar.DATE
                ) + "일"

            important = 3
        }

        var diffSec = (startCal.getTimeInMillis() - endCal.getTimeInMillis()) / 1000
        var diffDays = Math.abs(diffSec / (24 * 60 * 60)) // 날짜의 갭을 계산한다.

        CoroutineScope(Dispatchers.Main).launch {

            if (oneDay) { // 만약 시작일과 종료일이 같다면, 하루에 바로 입력하고 끝낸다
                db.RoomCalendarDao()
                    .insert(
                        RoomCalendar(
                            null,
                            id,
                            datanum,
                            year,
                            month,
                            day,
                            Time,
                            Title,
                            important
                        )
                    )

            } else { // 시작일과 종료일이 다르다면, 위에서 계산한 일자만큼 추가로 입력한다.

                for (i in 0..diffDays) {
                    db.RoomCalendarDao()
                        .insert(
                            RoomCalendar(
                                null,
                                id,
                                datanum,
                                year,
                                month,
                                day,
                                Time,
                                Title,
                                important
                            )
                        )
                    startCal.set(Calendar.DATE, startCal.get(Calendar.DATE) + 1)
                    day = startCal.get(Calendar.DATE)
                }

            }



            finish() // 액티비티 종료
        }

    }


    fun startCalUpdate(view: View) {
        // 시작일을 눌렀을때 호출되는 메소드. 일자를 입력받는다
        val startDateSetListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                startCal.set(Calendar.YEAR, year)
                startCal.set(Calendar.MONTH, monthOfYear)
                startCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val myFormat = "yyyy/MM/dd" // mention the format you need
                val sdf = SimpleDateFormat(myFormat, Locale.US)
                tv_startdate.text = sdf.format(startCal.time)

            }

        val startTimeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
            startCal.set(Calendar.HOUR_OF_DAY, hour)
            startCal.set(Calendar.MINUTE, minute)
            tv_starttime.text = SimpleDateFormat("HH:mm").format(startCal.time)
        }

        TimePickerDialog(
            this, startTimeSetListener,
            startCal.get(Calendar.HOUR_OF_DAY),
            startCal.get(Calendar.MINUTE),
            true
        ).show()

        DatePickerDialog(
            this, startDateSetListener,
            startCal.get(Calendar.YEAR),
            startCal.get(Calendar.MONTH),
            startCal.get(Calendar.DAY_OF_MONTH)
        ).show()

    }

    fun endCalUpdate(view: View) {
        // 종료일을 눌렀을때 호출되는 메소드. 일자를 입력받는다
        val endDateSetListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                endCal.set(Calendar.YEAR, year)
                endCal.set(Calendar.MONTH, monthOfYear)
                endCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val myFormat = "yyyy/MM/dd" // mention the format you need
                val sdf = SimpleDateFormat(myFormat, Locale.US)
                tv_enddate.text = sdf.format(endCal.time)

            }

        val endTimeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
            endCal.set(Calendar.HOUR_OF_DAY, hour)
            endCal.set(Calendar.MINUTE, minute)
            tv_endtime.text = SimpleDateFormat("HH:mm").format(endCal.time)
        }

        TimePickerDialog(
            this, endTimeSetListener,
            endCal.get(Calendar.HOUR_OF_DAY),
            endCal.get(Calendar.MINUTE),
            true
        ).show()

        DatePickerDialog(
            this, endDateSetListener,
            endCal.get(Calendar.YEAR),
            endCal.get(Calendar.MONTH),
            endCal.get(Calendar.DAY_OF_MONTH)
        ).show()

    }

}
