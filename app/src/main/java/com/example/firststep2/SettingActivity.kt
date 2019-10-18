package com.example.firststep2

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        val prefs = applicationContext.getSharedPreferences("userdata", Context.MODE_PRIVATE)
        var nickname = prefs.getString("nickname", "")
        var profile = prefs.getString("profile", "")
        var gold = prefs.getString("gold", "")

        ib_dog.setOnClickListener { Singleton.bottomMove(this, 0, this) }
        ib_calendar.setOnClickListener { Singleton.bottomMove(this, 1, this) }
        ib_checklist.setOnClickListener { Singleton.bottomMove(this, 2, this) }
        ib_chat.setOnClickListener { Singleton.bottomMove(this, 3, this) }
        ib_setting.setOnClickListener { Singleton.bottomMove(this, 4, this) }

    }

    fun logoutClicked(view: View) {
        val prefs = applicationContext.getSharedPreferences(
            "userdata",
            Context.MODE_PRIVATE
        )
        val editor = prefs!!.edit()
        editor.clear().apply()

        val prefs2 = applicationContext.getSharedPreferences(
            "autologin",
            Context.MODE_PRIVATE
        )
        val editor2 = prefs2!!.edit()
        editor2.clear().apply()

        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()

    }

    fun profileClicked(view: View) {
        val intent = Intent(applicationContext, Setting_profileActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intent)
    }
}
