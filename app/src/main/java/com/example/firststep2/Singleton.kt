package com.example.firststep2

import android.app.Activity
import android.app.Application
import android.view.View
import android.widget.Toast
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity

class Singleton private constructor(context: Context) {

    /*
    현재 위치를 저장할 변수를 만든다
    현재 액티비티라면 작동하지 않는다
    현재 위치와 같으면 페이드 애니메이션을 보여주면서 초기 화면으로 넘어간다
    다른 액티비티를 클릭했을때 숫자가 작으면 왼쪽 애니메이션 크면 오른쪽 애니메이션을 보여준다
    해당 액티비티로 이동 후 현재 위치를 저장한다
     */
    companion object {

        @Volatile
        private var INSTANCE: Singleton? = null
        var Now = 2

        fun getInstance(context: Context): Singleton =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Singleton(context).also { INSTANCE = it }
            }

        fun bottomMove(context: Context, number: Int, activity: Activity) {
            var intent:Intent? = null
            when(number){
                0 -> intent = Intent(context, LoginActivity::class.java)
                1 -> intent = Intent(context, CalendarActivity::class.java)
                2 -> intent = Intent(context, TodoActivity::class.java)
                3 -> intent = Intent(context, LoginActivity::class.java)
                4 -> intent = Intent(context, SettingActivity::class.java)
                else -> intent = Intent(context, LoginActivity::class.java)

            }

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(context, intent, null)
            if (Now > number){
                activity.overridePendingTransition(R.anim.anim_slide_in_left,R.anim.anim_slide_out_right)
            } else if (Now < number) {
                activity.overridePendingTransition(R.anim.anim_slide_in_right,R.anim.anim_slide_out_left)
            } else {
                activity.overridePendingTransition(R.anim.anim_fade_in, R.anim.anim_fade_out)
            }
            Now = number

        }
    }
}
