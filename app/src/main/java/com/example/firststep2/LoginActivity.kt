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

        val gson = GsonBuilder()
            .setLenient()
            .create()

        var retrofit = Retrofit.Builder()
            .baseUrl("http://15.164.201.56")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        server = retrofit.create(RetrofitService::class.java)

    }

    fun signinClicked(view: View) {
        val intent = Intent(this, SigninActivity::class.java)
        startActivityForResult(intent, 100)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null) {
            val id = data.getStringExtra("id")
            et_id.setText(id)
        }
    }

    fun loginClicked(view: View) {
        // 로그인 클릭시 메소드. 유저가 입력한 데이터를 검증하고 서버에 확인해서 로그인 처리를 한다

        var id = et_id.text.toString()
        var pw = et_pw.text.toString()
        val UUID = randomUUID().toString()

        if (id == "" || pw == "") {

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

                        if (cb_autologin.isChecked) {
                            // 자동로그인 체크 하고 로그인 했을시 데이터베이스에 UUID 입력
                            var db = Room.databaseBuilder(
                                applicationContext, AppDatabase::class.java, "Database"
                            ).build()

                            CoroutineScope(Dispatchers.IO).launch {
                                db.autologinDao().insert(RoomAutoLogin(1, id, UUID))
                                // 번호는 1로 고정. 하나의 아이디만 저장하기 위해서

                                val prefs = applicationContext.getSharedPreferences(
                                    "autologin",
                                    Context.MODE_PRIVATE
                                )
                                val editor = prefs!!.edit()
                                editor.putString("id", id).apply()


                            }
                        } else{
                            // 자동로그인 체크 안하고 로그인 할시 쉐어드 프리퍼런스에 id값 삭제
                            val prefs = applicationContext.getSharedPreferences(
                                "autologin",
                                Context.MODE_PRIVATE
                            )
                            val editor = prefs!!.edit()
                            editor.putString("id", "").apply()

                        }

                        val intent = Intent(applicationContext, TodoActivity::class.java)
                        startActivity(intent)
                        finish()

                    } else {
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



