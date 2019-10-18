package com.example.firststep2

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_signin.*
import kotlinx.android.synthetic.main.activity_todo.*

class TodoActivity : AppCompatActivity() {
    // todo 현재 임시 액티비티임. todo 기능 추가시 삭제 후 다시 구현

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo)

        // 로그인한 유저 정보를 쉐어드 프리퍼런스에서 가져온다
        val prefs = applicationContext.getSharedPreferences("userdata", Context.MODE_PRIVATE)
        var nickname = prefs.getString("nickname", "")
        var profile = prefs.getString("profile", "")
        var gold = prefs.getString("gold", "")

        // 바텀 네비게이션을 클릭하면 해당 액티비티로 이동한다
        ib_dog.setOnClickListener { Singleton.bottomMove(this, 0, this) }
        ib_calendar.setOnClickListener { Singleton.bottomMove(this, 1, this) }
        ib_checklist.setOnClickListener { Singleton.bottomMove(this, 2, this) }
        ib_chat.setOnClickListener { Singleton.bottomMove(this, 3, this) }
        ib_setting.setOnClickListener { Singleton.bottomMove(this, 4, this) }


        textView6.setText("닉네임 : " + nickname + " 로그인")
        Glide.with(this)
            .load("http://15.164.201.56/uploads/$profile")
            .centerCrop()
            .apply(RequestOptions.bitmapTransform(CropCircleTransformation()))
            .into(imageView)

    }

    fun autologinDelete(view: View) {

        // 글자 클릭시 쉐어드 프리퍼런스에 id값 삭제
        val prefs = applicationContext.getSharedPreferences(
            "autologin",
            Context.MODE_PRIVATE
        )
        val editor = prefs!!.edit()
        editor.putString("id", "").apply()

        Toast.makeText(this, "자동로그인 삭제", Toast.LENGTH_SHORT).show()
    }
}
