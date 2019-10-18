package com.example.firststep2

data class item_CalendarBottom(
    // 캘린더 액티비티의 하단 리사이클러뷰에서 사용하는 데이터 클래스
    val key: Int,
    val id: String,
    val datanum: Int,
    val year: Int,
    val month: Int,
    val day: Int,
    val Time: String,
    val Title: String
)