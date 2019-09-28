package com.example.firststep2

import retrofit2.Call
import retrofit2.http.*

data class signinDTO(var result:String? = null) // 회원가입 후 정보 수신
data class loginDTO(var result:String? = null) // 로그인 후 정보 수신


interface RetrofitService{

    @FormUrlEncoded
    @POST("/Signin.php/")
    fun signinRequest(@Field("id") id: String,
                      @Field("pw") pw: String,
                      @Field("nickname") nickname: String): Call<signinDTO>

    @FormUrlEncoded
    @POST("/login.php/")
    fun loginRequest(@Field("id") id: String,
                     @Field("pw") pw: String,
                     @Field("UUID") UUID: String): Call<loginDTO>

    @FormUrlEncoded
    @POST("/autologin.php/")
    fun autologinRequest(@Field("id") id: String,
                     @Field("UUID") UUID: String): Call<loginDTO>

}