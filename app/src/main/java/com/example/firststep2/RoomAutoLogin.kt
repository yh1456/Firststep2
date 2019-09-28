package com.example.firststep2

import androidx.room.*

@Entity
class RoomAutoLogin (@PrimaryKey(autoGenerate = false) val num: Int,
                     val id: String,
                     val UUID: String)

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

@Database(entities = arrayOf(RoomAutoLogin::class), version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun autologinDao() : RoomAutoLoginDao
}