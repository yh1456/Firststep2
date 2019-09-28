package com.example.firststep2

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_signin.*
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URL
import com.google.gson.GsonBuilder
import com.google.gson.Gson
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.widget.Toast


class SigninActivity : AppCompatActivity() {
    var server: RetrofitService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

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

    fun finishClikded(view: View) {
        // 뒤로가기 버튼 or 취소버튼 클릭시
        // 액티비티를 종료한다
        finish()
    }

    fun SigninClikded(view: View) {
        // 회원가입 버튼 클릭시
        var allIsOk = true // 입력값에 문제있을시 false로 바꿔서 분기 확인
        var id: String = et_id.text.toString() // 에딧텍스트에 입력받은 아이디
        var pw: String = et_pw.text.toString() // 에딧텍스트에 입력받은 비밀번호
        var nickname: String = et_nickname.text.toString() // 에딧텍스트에 입력받은 닉네임

        // 아이디 6글자 비밀번호 6글자 닉네임 2글자 미만일시 해당 경고 띄워줌
        if (id.length < 6) {
            allIsOk = false
            tv_idalert.visibility = View.VISIBLE
        } else {
            tv_idalert.visibility = View.INVISIBLE
//            Toast.makeText(this, id, Toast.LENGTH_LONG).show()
        }

        if (pw.length < 6) {
            allIsOk = false
            tv_pwalert.visibility = View.VISIBLE
        } else {
            tv_pwalert.visibility = View.INVISIBLE
        }


        if (nickname.length < 2) {
            allIsOk = false
            tv_nicknamealert.visibility = View.VISIBLE
        } else {
            tv_nicknamealert.visibility = View.INVISIBLE
        }


        // 세가지 조건 모두 만족할때
        if (allIsOk) {

            //TODO 사진 업로드 추가해야함

            // 서버에 데이터 전송.
            server?.signinRequest(id, pw, nickname)?.enqueue(object : Callback<signinDTO> {
                override fun onFailure(call: Call<signinDTO>?, t: Throwable?) {
                    Log.e("확인 Fail1", t.toString())
                }

                override fun onResponse(call: Call<signinDTO>?, response: Response<signinDTO>?) {
                    var tmpString = response?.body().toString()
                    Log.d("확인 Response", tmpString)

                    if (tmpString == "signinDTO(result=쿼리 정상 작동)") {

                        // 데이터 전송에 이상없을시 로그인 액티비티로 이동. intent에 아이디 입력

                        val intent = Intent()
                        intent.putExtra("id", id)
                        setResult(100, intent)
                        finish()

                    } else {
                        // 값이 정상적으로 들어가지 못했을때 예외처리
                        var tmpStringArray = tmpString.split("'")
                        var errorCode = tmpStringArray[3] // 에러 키값

                        when (errorCode) {
                            "id" -> Toast.makeText(
                                applicationContext,
                                "사용중인 아이디입니다.",
                                Toast.LENGTH_LONG
                            ).show()
                            "nickname" -> Toast.makeText(
                                applicationContext,
                                "사용중인 닉네임 입니다.",
                                Toast.LENGTH_LONG
                            ).show()
                            else -> Toast.makeText(
                                applicationContext,
                                "서버 연결 상태를 확인해주세요.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }


                }

            })


        }


    }
}
