package com.example.firststep2

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings.Secure
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.provider.Settings
import android.widget.TextView
import androidx.room.Room
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {
    // fixme 스플래시액티비티로 전환할 예정

    var server: RetrofitService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gson = GsonBuilder()
            .setLenient()
            .create()

        var retrofit = Retrofit.Builder()
            .baseUrl("http://15.164.201.56")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        server = retrofit.create(RetrofitService::class.java)

        var db = Room.databaseBuilder(
            applicationContext, AppDatabase::class.java, "Database"
        ).build()

        // 자동로그인에 사용하기 위해서 쉐어드 프리퍼런스에서 id를 가져온다
        val prefs = applicationContext.getSharedPreferences("autologin", Context.MODE_PRIVATE)
        var id = prefs.getString("id", "")

        if (id == "") {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // id가 있으면 Room 에서 UUID를 가져와서 서버의 id, UUID와 비교하여 자동 로그인 성공 여부를 판별한다.

            CoroutineScope(Dispatchers.IO).launch {
                var UUID = db.autologinDao().getUUID(id).get(0).UUID

                server?.autologinRequest(id, UUID)?.enqueue(object : Callback<loginDTO> {
                    override fun onFailure(call: Call<loginDTO>?, t: Throwable?) {
                        Log.e("확인 Fail1", t.toString())
                    }

                    override fun onResponse(call: Call<loginDTO>?, response: Response<loginDTO>?) {
                        var tmpString = response?.body().toString()
                        Log.d("확인 Response", tmpString)

                        var tmpStringArray = tmpString.split(",")
                        var outhCode = tmpStringArray[0]

                        if (outhCode == "loginDTO(result=쿼리 정상 작동") {

                            val prefs = applicationContext.getSharedPreferences(
                                "userdata",
                                Context.MODE_PRIVATE
                            )
                            val editor = prefs!!.edit()
                            editor.putString("id", id).apply()
                            var nickname = tmpStringArray[1]
                            editor.putString("nickname", nickname).apply()
                            var profile = tmpStringArray[2]
                            editor.putString("profile", profile).apply()
                            var gold = tmpStringArray[3]
                            editor.putString("gold", gold).apply()


                            val intent = Intent(applicationContext, CalendarActivity::class.java)
                            startActivity(intent)
                            finish()

                        } else {
                            val intent = Intent(applicationContext, LoginActivity::class.java)
                            startActivity(intent)
                        }

                    }
                }

                )

            }

        }


    }

}
