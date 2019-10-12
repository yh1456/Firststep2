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

    var server: RetrofitService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // gson 빌드
        val gson = GsonBuilder()
            .setLenient()
            .create()

        // Retrofit 빌드
        var retrofit = Retrofit.Builder()
            .baseUrl("http://15.164.201.56")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        server = retrofit.create(RetrofitService::class.java)

        // Room 빌드
        var db = Room.databaseBuilder(
            applicationContext,AppDatabase::class.java,"Database"
        ).build()

        // 쉐어드 프리퍼런스에서 id를 가져온다
        val prefs = applicationContext.getSharedPreferences("autologin", Context.MODE_PRIVATE)
        var id = prefs.getString("id", "")

        if (id == ""){
            // 쉐어드 프리퍼런스에 id가 없을시 로그인 페이지로 이동
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // id가 있으면 Room 에서 UUID를 꺼내온다
//            textView2.text = id
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
                        var outhCode = tmpStringArray[0] // 로그인 성공시 정보 받아옴

                        if (outhCode == "loginDTO(result=쿼리 정상 작동") {

                            // 로그인 성공시 아이디와 닉네임 값을 받아와서 쉐어드프리퍼런스에 넣고 사용
                            val prefs = applicationContext.getSharedPreferences("userdata", Context.MODE_PRIVATE)
                            val editor = prefs!!.edit()
                            editor.putString("id", id).apply()
                            var nickname = tmpStringArray[1]
                            editor.putString("nickname", nickname).apply()
                            var profile = tmpStringArray[2]
                            editor.putString("profile", profile).apply()
                            var gold = tmpStringArray[3]
                            editor.putString("gold", gold).apply()


                            // 로그인 성공시 액티비티 이동

                        val intent = Intent(applicationContext, TodoActivity::class.java)
                        startActivity(intent)
                        finish()

                        } else {
                            // 로그인 실패시 예외처리(로그인 액티비티로 보냄)
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
