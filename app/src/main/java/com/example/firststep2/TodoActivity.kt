package com.example.firststep2

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_todo.*

class TodoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo)

        val prefs = applicationContext.getSharedPreferences("userdata", Context.MODE_PRIVATE)
        var nickname = prefs.getString("nickname", "")

        textView6.setText(nickname + " 로그인")
    }
}
