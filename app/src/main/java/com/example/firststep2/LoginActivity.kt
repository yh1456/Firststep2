package com.example.firststep2

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.room.Room
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID.randomUUID
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class LoginActivity : AppCompatActivity() {
    var server: RetrofitService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

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


    }

    fun signinClicked(view: View) { // 회원가입 클릭시 해당 액티비티로 이동
        val intent = Intent(this, SigninActivity::class.java)
        startActivityForResult(intent, 100)


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // 회원가입에서 돌아왔을때 아이디 값 입력
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null) {
            val id = data.getStringExtra("id")
            et_id.setText(id)
        }
    }

    fun loginClicked(view: View) {
        //로그인 클릭시 메소드
        var id = et_id.text.toString()
        var pw = et_pw.text.toString()
        val UUID = randomUUID().toString()

        if (id == "" || pw == "") { // 아이디나 비밀번호가 비어있으면 알림
            Toast.makeText(applicationContext, "아이디 혹은 비밀번호를 입력해주세요", Toast.LENGTH_LONG).show()
        } else {


            server?.loginRequest(id, pw, UUID)?.enqueue(object : Callback<loginDTO> {
                override fun onFailure(call: Call<loginDTO>?, t: Throwable?) {
                    Log.e("확인 Fail1", t.toString())
                }

                override fun onResponse(call: Call<loginDTO>?, response: Response<loginDTO>?) {
                    var tmpString = response?.body().toString()


                    Log.d("확인 Response", tmpString)

                    var tmpStringArray = tmpString.split(",")
                    var outhCode = tmpStringArray[0] // 로그인 성공시 정보 받아옴

                    if (outhCode == "loginDTO(result=쿼리 정상 작동") {

                        val prefs = applicationContext.getSharedPreferences("userdata", Context.MODE_PRIVATE)
                        val editor = prefs!!.edit()
                        editor.putString("id", id).apply()
                        var nickname = tmpStringArray[1]
                        editor.putString("nickname", nickname).apply()

                    if (cb_autologin.isChecked){

                        var db = Room.databaseBuilder(
                            applicationContext,AppDatabase::class.java,"Database"
                        ).build()

                        CoroutineScope(Dispatchers.IO).launch {
                            db.autologinDao().insert(RoomAutoLogin(1,id,UUID))



                        }


                        val prefs = applicationContext.getSharedPreferences("autologin", Context.MODE_PRIVATE)
                        val editor = prefs!!.edit()
                        editor.putString("id", id).apply()

                    }
                        // 로그인 성공시 액티비티 이동

                        val intent = Intent(applicationContext, TodoActivity::class.java)
                        startActivity(intent)
                        finish()

                    } else {
                        // 로그인 실패시 예외처리

                        Toast.makeText(
                            applicationContext,
                            "아이디 혹은 비밀번호를 확인해주세요.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }
            }

            )
        }

    }
}



