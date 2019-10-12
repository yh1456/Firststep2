//package com.example.firststep2
//
//import androidx.room.*
//
//
//@Entity
//class RoomCalendar(
//    @PrimaryKey(autoGenerate = true) val key: Int?,
//    val id: String,
//    val datanum: Int,
//    val year: Int,
//    val month: Int,
//    val day: Int,
//    val Time: String,
//    val Title: String,
//    val important: Int
//)
//
//@Dao
//interface RoomCalendarDao {
//    @Query("SELECT * from RoomCalendar")
//    fun getAll(): List<RoomCalendar>
//
//    @Query("SELECT * from RoomCalendar where id = :id and year = :year and month = :month and day = :day order by important")
//    fun getData(
//        id: String,
//        year: Int,
//        month: Int,
//        day: Int
//    ): List<RoomCalendar>
//
//    @Query("SELECT * from RoomCalendar where id = :id order by datanum desc")
//    fun gethigh(
//        id: String
//    ): List<RoomCalendar>
//
//    @Insert
//    fun insert(RoomCalendar: RoomCalendar)
//
//    @Delete
//    fun delete(RoomCalendar: RoomCalendar)
//}
//
//@Database(entities = arrayOf(RoomCalendar::class), version = 1, exportSchema = false)
//abstract class AppDatabaseCalenar : RoomDatabase() {
//    abstract fun RoomCalendarDao(): RoomCalendarDao
//}