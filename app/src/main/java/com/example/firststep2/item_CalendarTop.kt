package com.example.firststep2

import java.util.*

data class calendarInt(val Year : Int, val Month : Int, val Day : Int)

class item_CalendarTop  {
    // 캘린더의 아이템

    companion object {
        const val DAYS_OF_WEEK = 7
        const val LOW_OF_CALENDAR = 6
    } // 아이템 외부에서 사용할 숫자, 캘린더의 가로 세로 숫자를 적어두고 캘린더에서 사용한다

    val calendar = Calendar.getInstance()

    var prevMonthTailOffset = 0
    var nextMonthHeadOffset = 0
    var currentMonthMaxDate = 0
    // 이번달 날짜인지 이전달 혹은 다음날 날짜인지 구별하기위한 변수

    var data = arrayListOf<calendarInt>()

    init {
        calendar.time = Date() // 아이템 init 시에 현재 날짜 기준으로 맞춤
    }

    fun initBaseCalendar(refreshCallback: (Calendar) -> Unit) {
        // 리사이클러뷰 어댑터에서 캘린더 인스턴스를 받아서 makeMonthDate 메소드를 실행
        // init 시에 time 을 현재로 정했기 때문에 이번달 데이터로 맞춰짐
        makeMonthDate(refreshCallback)
    }

    fun changeToPrevMonth(refreshCallback: (Calendar) -> Unit) {
        // 이전달로 버튼 클릭시 데이터 변경 부분
        if(calendar.get(Calendar.MONTH) == 0){
            // 만약 이번달이 1월이면 연도를 낮추고 12월로 한다
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 1)
            calendar.set(Calendar.MONTH, Calendar.DECEMBER) // 12월로 설정
        }else {
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1)
            // 아니면 그냥 월만 낮춤
        }
        makeMonthDate(refreshCallback) // 이번달로 데이터 변경 메소드 실행
    }

    fun changeToNextMonth(refreshCallback: (Calendar) -> Unit) {
        // 다음달로 버튼 클릭시 데이터 변경 부분
        if(calendar.get(Calendar.MONTH) == Calendar.DECEMBER){
            // 만약 이번달이 12월이면 연도를 늘리고 1월로 설정
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1)
            calendar.set(Calendar.MONTH, 0)
        }else {
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1)
            // 아니면 그냥 월만 늘림
        }
        makeMonthDate(refreshCallback)
    }

    private fun makeMonthDate(refreshCallback: (Calendar) -> Unit) {
        // 리사이클러뷰어댑터에서 실행한 initBaseCalendar 또는 이전달이나 다음달을 클릭했을때 콜백을 받아서 실행하는 메소드
        // 이번달의 데이터를 data 배열에 담는다. 리사이클러뷰의 변경 처리는 어댑터에서, 제목의 변경 처리는 액티비티에서 한다

        data.clear()

        currentMonthMaxDate = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        prevMonthTailOffset = calendar.get(Calendar.DAY_OF_WEEK) - 1

        makePrevMonthTail(calendar.clone() as Calendar)

        makeCurrentMonth(calendar)

        nextMonthHeadOffset = LOW_OF_CALENDAR * DAYS_OF_WEEK - (prevMonthTailOffset + currentMonthMaxDate)

        makeNextMonthHead(calendar.clone() as Calendar)

        refreshCallback(calendar)
    }

    private fun makePrevMonthTail(calendar: Calendar) {
        // 이전달 마지막일(꼬리) 표시
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1) // 이전달 데이터를 가져옴
        val maxDate = calendar.getActualMaximum(Calendar.DATE) // 이전달의 전체 일수
        var maxOffsetDate = maxDate - prevMonthTailOffset // 표시되는 이번달 첫일(전체일수 - 이전달 표시 일수)


        for (i in 1..prevMonthTailOffset) {
            var tmp = calendarInt(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), ++maxOffsetDate)
            data.add(tmp)
        } // 데이터에 표시되는 이전달 첫 일부터 마지막일까지 입력한다
    }

    private fun makeCurrentMonth(calendar: Calendar) {
        // 이번달 일자 데이터를 입력한다
        for (i in 1..calendar.getActualMaximum(Calendar.DATE)){
            var tmp = calendarInt(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), i)
            data.add(tmp)
        }
    }

    private fun makeNextMonthHead(calendar: Calendar) {
        // 다음달 데이터를 입력하는 메소드
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1)

        for (i in 1..nextMonthHeadOffset) {
            var tmp = calendarInt(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), i)
            data.add(tmp) // 1일부터 표시되는 다음달 마지막일까지 데이터를 입력한다
        }
    }
}