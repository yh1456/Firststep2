package com.example.firststep2

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

data class signinDTO(var result:String? = null) // 회원가입 후 정보 수신
data class loginDTO(var result:String? = null) // 로그인 후 정보 수신

data class DTO(var result:String? = null) // 로그인 후 정보 수신


interface RetrofitService{

    @FormUrlEncoded
    @POST("/Signin.php/")
    // 회원가입
    fun signinRequest(
        @Field("id") id: String,
        @Field("pw") pw: String,
        @Field("nickname") nickname: String,
        @Field("filename") fileName: String
    ): Call<signinDTO>

    @FormUrlEncoded
    @POST("/login.php/")
    // 로그인
    fun loginRequest(@Field("id") id: String,
                     @Field("pw") pw: String,
                     @Field("UUID") UUID: String): Call<loginDTO>

    @FormUrlEncoded
    @POST("/autologin.php/")
    // 자동로그인
    fun autologinRequest(@Field("id") id: String,
                     @Field("UUID") UUID: String): Call<loginDTO>

    @FormUrlEncoded
    @POST("/profilesetting1.php/")
    // 프로필설정(비밀번호 변경시)
    fun profilesettingRequest1(
        @Field("id") id: String,
        @Field("pw") pw: String,
        @Field("nickname") nickname: String,
        @Field("filename") fileName: String
    ): Call<DTO>

    @FormUrlEncoded
    @POST("/profilesetting2.php/")
    // 프로필설정(비밀번호 미변경시)
    fun profilesettingRequest2(
        @Field("id") id: String,
        @Field("nickname") nickname: String,
        @Field("filename") fileName: String
    ): Call<DTO>


    @Multipart
    @POST("setUserProfileImage.php/")
    // 프로필 이미지 서버로 보내기
    fun post_Porfile_Request(
        @Part("id") id: String,
        @Part imageFile: MultipartBody.Part
    ): Call<String>

}