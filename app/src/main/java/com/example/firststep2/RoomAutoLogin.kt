package com.example.firststep2

import androidx.room.*

@Entity
class RoomAutoLogin (@PrimaryKey(autoGenerate = false) val num: Int,
                     val id: String,
                     val UUID: String)


@Entity
class RoomCalendar(
    @PrimaryKey(autoGenerate = true) val key: Int?,
    val id: String,
    val datanum: Int,
    val year: Int,
    val month: Int,
    val day: Int,
    val Time: String,
    val Title: String,
    val important: Int
)

@Dao
interface RoomAutoLoginDao {
    @Query("SELECT * from RoomAutoLogin")
    fun getAll(): List<RoomAutoLogin>

    @Query("SELECT * from RoomAutoLogin where id = :id")
    fun getUUID(id: String): List<RoomAutoLogin>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(RoomAutoLogin:RoomAutoLogin)

    @Delete
    fun delete(RoomAutoLogin:RoomAutoLogin)
}


@Dao
interface RoomCalendarDao {
    @Query("SELECT * from RoomCalendar")
    fun getAll(): List<RoomCalendar>

    @Query("SELECT * from RoomCalendar where id = :id and year = :year and month = :month and day = :day order by important")
    fun getData(
        id: String,
        year: Int,
        month: Int,
        day: Int
    ): List<RoomCalendar>

    @Query("SELECT * from RoomCalendar where id = :id order by datanum desc")
    fun gethigh(
        id: String
    ): List<RoomCalendar>

    @Insert
    fun insert(RoomCalendar: RoomCalendar)

    @Delete
    fun delete(RoomCalendar: RoomCalendar)
}

@Database(entities = arrayOf(RoomAutoLogin::class, RoomCalendar::class), version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun autologinDao() : RoomAutoLoginDao
    abstract fun RoomCalendarDao(): RoomCalendarDao
}