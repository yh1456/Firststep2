package com.example.firststep2

import java.util.*

data class calendarInt(val Year : Int, val Month : Int, val Day : Int)

class item_CalendarTop  {
    // 캘린더의 아이템

    companion object {
        const val DAYS_OF_WEEK = 7
        const val LOW_OF_CALENDAR = 6
    } // 아이템 외부에서 사용할 숫자, 캘린더의 가로 세로 숫자를 적어두고 캘린더에서 사용한다

    val calendar = Calendar.getInstance() // java.util 의 인스턴스를 가져와서 사용함

    var prevMonthTailOffset = 0
    var nextMonthHeadOffset = 0
    var currentMonthMaxDate = 0
    // 이번달 날짜인지 이전달 혹은 다음날 날짜인지 구별하기위한 변수

    var data = arrayListOf<calendarInt>()
    // 날짜를 담아두기 위한 변수
    // 전달 마지막일, 이번달 날짜, 다음달 초기일자가 담김

    init {
        calendar.time = Date() // 아이템 init 시에 현재 날짜 기준으로 맞춤
    }

    fun initBaseCalendar(refreshCallback: (Calendar) -> Unit) {
        // 리사이클러뷰 어댑터에서 캘린더 인스턴스를 받아서 makeMonthDate 메소드를 실행
        // 이때는 time 이 현재이기 때문에 이번달 데이터로 맞춰짐
        makeMonthDate(refreshCallback)
    }

    fun changeToPrevMonth(refreshCallback: (Calendar) -> Unit) {
        // 이전달로 버튼 클릭시 데이터 변경 부분
        if(calendar.get(Calendar.MONTH) == 0){ // 만약 이번달이 1월이면
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 1) // 연도를 1 낮추고
            calendar.set(Calendar.MONTH, Calendar.DECEMBER) // 12월로 설정
        }else {
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1) // 아니면 그냥 월만 낮춤
        }
        makeMonthDate(refreshCallback) // 이번달로 데이터 변경 메소드 실행
    }

    fun changeToNextMonth(refreshCallback: (Calendar) -> Unit) {
        // 다음달로 버튼 클릭시 데이터 변경 부분
        if(calendar.get(Calendar.MONTH) == Calendar.DECEMBER){ // 만약 이번달이 12월이면
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1) // 연도를 1 늘리고
            calendar.set(Calendar.MONTH, 0) // 1월로 설정
        }else {
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1) // 아니면 그냥 월만 늘림
        }
        makeMonthDate(refreshCallback) // 다음달로 데이터 변경 메소드 실행
    }

    private fun makeMonthDate(refreshCallback: (Calendar) -> Unit) {
        // 리사이클러뷰어댑터에서 실행한 initBaseCalendar 또는 이전달이나 다음달을 클릭했을때 콜백을 받아서 실행하는 메소드
        // 이번달의 데이터를 data 배열에 담는다. 리사이클러뷰의 변경 처리는 어댑터에서, 제목의 변경 처리는 액티비티에서 한다

        data.clear() // 일단 배열을 비움

        calendar.set(Calendar.DATE, 1) // 캘린더의 일자를 1일부터 실행

        currentMonthMaxDate = calendar.getActualMaximum(Calendar.DAY_OF_MONTH) // 이번달의 마지막 일자를 받아옴

        prevMonthTailOffset = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 이전달 일자 표시를 위해 변수에 해당 데이터를 담음

//        println("확인. prevMonthTailOffset : $prevMonthTailOffset") // 정확히 어떤 데이터가 들어가는지 확인해야함

        makePrevMonthTail(calendar.clone() as Calendar) // 이전달 일자 입력
        makeCurrentMonth(calendar) // 현재 달 일자 입력

        nextMonthHeadOffset = LOW_OF_CALENDAR * DAYS_OF_WEEK - (prevMonthTailOffset + currentMonthMaxDate)
        // 다음달 일자 표시를 위해 변수에 해당 데이터를 담음 7*6(42) - (?+30or31)
        makeNextMonthHead(calendar.clone() as Calendar) // 다음달 일자 입력

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

        for (i in 1..calendar.getActualMaximum(Calendar.DATE)){
            var tmp = calendarInt(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), i)
            data.add(tmp)
        } // 이번달 일자 데이터를 입력한다
    }

    private fun makeNextMonthHead(calendar: Calendar) {
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1) // 다음달 데이터를 가져옴

        for (i in 1..nextMonthHeadOffset) {
            var tmp = calendarInt(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), i)
            data.add(tmp) // 1일부터 표시되는 다음달 마지막일까지 데이터를 입력한다
        }
    }
}