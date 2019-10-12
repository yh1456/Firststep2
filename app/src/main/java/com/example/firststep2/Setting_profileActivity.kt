package com.example.firststep2

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage
import com.bumptech.glide.request.RequestOptions
import com.google.gson.GsonBuilder
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_setting_profile.*
import kotlinx.android.synthetic.main.activity_setting_profile.et_nickname
import kotlinx.android.synthetic.main.activity_setting_profile.ib_calendar
import kotlinx.android.synthetic.main.activity_setting_profile.ib_chat
import kotlinx.android.synthetic.main.activity_setting_profile.ib_checklist
import kotlinx.android.synthetic.main.activity_setting_profile.ib_dog
import kotlinx.android.synthetic.main.activity_setting_profile.ib_setting
import kotlinx.android.synthetic.main.activity_signin.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class Setting_profileActivity : AppCompatActivity() {

    var server: RetrofitService? = null // 레트로핏 연결용 변수
    var mMainPhotoPath: String = "0" // 사진 절대경로 저장용 변수
    var fileName: String = "0" // 서버 업로드용 이름 변수
    val REQUEST_TAKE_PHOTO = 1
    val TAKE_PICTURE = 2
    var rotatedBitmap: Bitmap? = null
    var tempFile: File? = null
    var photoIs = false // 사진 존재여부를 판단하여 사진 추가 취소시 분기용으로 사용
    var PasswordIs = false // 비밀번호 변경 클릭 여부
    var id : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_profile)

        // 로그인한 유저 정보를 쉐어드 프리퍼런스에서 가져온다
        val prefs = applicationContext.getSharedPreferences("userdata", Context.MODE_PRIVATE)
        id = prefs.getString("id", "")
        var nickname = prefs.getString("nickname", "")
        var profile :String = prefs.getString("profile", "")
        mMainPhotoPath = profile
        fileName = profile

        // 가져온 유저 정보를 화면에 반영한다
        et_nickname.setText(nickname)
        if (profile == "0") {
            iv_photo.setImageResource(R.drawable.signin_profilephoto)
        } else {
            Glide.with(this)
                .load("http://15.164.201.56/uploads/$profile")
                .centerCrop()
                .apply(RequestOptions.bitmapTransform(CropCircleTransformation()))
                .into(iv_photo)
        }

        // 바텀 네비게이션을 클릭하면 해당 액티비티로 이동한다
        ib_dog.setOnClickListener { Singleton.bottomMove(this, 0, this) }
        ib_calendar.setOnClickListener { Singleton.bottomMove(this, 1, this) }
        ib_checklist.setOnClickListener { Singleton.bottomMove(this, 2, this) }
        ib_chat.setOnClickListener { Singleton.bottomMove(this, 3, this) }
        ib_setting.setOnClickListener { Singleton.bottomMove(this, 4, this) }


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

        // 사진 입력받을 자리 라운딩 처리
        iv_photo.background = ShapeDrawable(OvalShape())
        iv_photo.clipToOutline = true


    }


    fun passwordClicked(view: View) {
        /* 패스워드 변경 버튼을 클릭했을때 메소드
           변경 확인시에 클릭했는지를 알기위해 전역변수에 값을 변경해준다
           패스워드 변경 버튼을 없애고
           패스워드를 변경할수있는 에딧텍스트를 드러낸다
         */
        PasswordIs = true
        bt_passwordchange.visibility = View.GONE
        et_password.visibility = View.VISIBLE

    }


    fun changeConfirmed(view: View) {
        /* 변경확인 버튼을 클릭했을때 메소드
           패스워드 변경 버튼을 클릭했는지를 확인하여 분기한다
           닉네임과 비밀번호가 정상적으로 입력되어있는지 확인한다
            비정상데이터일경우 해당 사실을 유저에게 알려준다
            정상데이터일경우 데이터를 분기에 따라 php로 보내서 처리한다.
         */
        var allIsOK = false
        var nickname: String = et_nickname.text.toString() // 에딧텍스트에 입력받은 닉네임
        var password: String = et_password.text.toString() // 에딧텍스트에 입력받은 비밀번호


        if (PasswordIs) {
            if (et_password.text.toString() == "") PasswordIs = false
            // 패스워드를 입력하지 않았을경우 패스워드 변경 버튼을 클릭하지 않은것으로 한다
        }

        if (nickname.length >= 2) {
            if (PasswordIs) { // 비밀번호 변경 버튼을 클릭하였을 경우 or 입력받지 않았을경우
                if (password.length >= 6) allIsOK = true
            } else {
                allIsOK = true
            }
        }

        if (photoIs) {
            if (mMainPhotoPath != "0") {
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                fileName = "$id$timeStamp.png"
            } else {
                fileName = "0"
            }
        } else {
            mMainPhotoPath = "0"
        }

        if (allIsOK) { // 모든 데이터가 정상적일 경우
            if (PasswordIs) { // 비밀번호 변경 버튼을 클릭하고 입력하였을때

                // 서버에 데이터 전송.
                server?.profilesettingRequest1(id.toString(), password, nickname, fileName)?.enqueue(object : Callback<DTO> {
                    override fun onFailure(call: Call<DTO>?, t: Throwable?) {
                        Log.e("확인 Fail1", t.toString())
                    }

                    override fun onResponse(call: Call<DTO>?, response: Response<DTO>?) {
                        var tmpString = response?.body().toString()
                        Log.d("확인 Response", tmpString)

                        if (tmpString == "DTO(result=쿼리 정상 작동)") {
                            // 데이터 전송에 이상없을시

                            // 사진 있을때 사진 업로드 추가
                            if (mMainPhotoPath != "0"){

                                val file = File(mMainPhotoPath)

                                var requestBody : RequestBody = RequestBody.create(MediaType.parse("image/*"),file)
                                var body : MultipartBody.Part = MultipartBody.Part.createFormData("uploaded_file",fileName,requestBody)

                                server?.post_Porfile_Request(id.toString(),body)?.enqueue(object: Callback<String> {
                                    override fun onFailure(call: Call<String>, t: Throwable) {
                                        Log.d("이미지 결과1",t.message)
                                    }

                                    override fun onResponse(call: Call<String>, response: Response<String>) {
                                        if (response.isSuccessful) {
                                            Toast.makeText(applicationContext, "File Uploaded Successfully...", Toast.LENGTH_LONG).show()
                                            Log.d("이미지 결과2",""+response?.body().toString())
                                        } else {
                                            Toast.makeText(applicationContext, "Some error occurred...", Toast.LENGTH_LONG).show()
                                            Log.d("이미지 결과3",""+response?.body().toString())
                                        }
                                    }
                                })
                            }
                            // 로그인 성공시 쉐어드 프리퍼런스에 유저의 데이터를 입력함
                            val prefs = applicationContext.getSharedPreferences(
                                "userdata",
                                Context.MODE_PRIVATE
                            )
                            val editor = prefs!!.edit()
                            editor.putString("id", id).apply()
                            editor.putString("nickname", nickname).apply()
                            editor.putString("profile", fileName).apply()

                            finish()

                        } else {
                            // 값이 정상적으로 들어가지 못했을때 예외처리
                            var tmpStringArray = tmpString.split("'")
                            var errorCode = tmpStringArray[3] // 에러 키값

                            when (errorCode) {
                                "user_id" -> Toast.makeText(
                                    applicationContext,
                                    "사용중인 아이디입니다.",
                                    Toast.LENGTH_LONG
                                ).show()
                                "user_nickname" -> Toast.makeText(
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

            } else { // 클릭하지 않았을 경우
                // 서버에 데이터 전송.
                server?.profilesettingRequest2(id.toString(), nickname, fileName)?.enqueue(object : Callback<DTO> {
                    override fun onFailure(call: Call<DTO>?, t: Throwable?) {
                        Log.e("확인 Fail1", t.toString())
                    }

                    override fun onResponse(call: Call<DTO>?, response: Response<DTO>?) {
                        var tmpString = response?.body().toString()
                        Log.d("확인 Response", tmpString)

                        if (tmpString == "DTO(result=쿼리 정상 작동)") {
                            // 데이터 전송에 이상없을시

                            // 사진 있을때 사진 업로드 추가
                            if (mMainPhotoPath != "0"){

                                val file = File(mMainPhotoPath)

                                var requestBody : RequestBody = RequestBody.create(MediaType.parse("image/*"),file)
                                var body : MultipartBody.Part = MultipartBody.Part.createFormData("uploaded_file",fileName,requestBody)

                                server?.post_Porfile_Request(id.toString(),body)?.enqueue(object: Callback<String> {
                                    override fun onFailure(call: Call<String>, t: Throwable) {
                                        Log.d("이미지 결과1",t.message)
                                    }

                                    override fun onResponse(call: Call<String>, response: Response<String>) {
                                        if (response.isSuccessful) {
                                            Toast.makeText(applicationContext, "File Uploaded Successfully...", Toast.LENGTH_LONG).show()
                                            Log.d("이미지 결과2",""+response?.body().toString())
                                        } else {
                                            Toast.makeText(applicationContext, "Some error occurred...", Toast.LENGTH_LONG).show()
                                            Log.d("이미지 결과3",""+response?.body().toString())
                                        }
                                    }
                                })
                            }
                            // 로그인 성공시 쉐어드 프리퍼런스에 유저의 데이터를 입력함
                            val prefs = applicationContext.getSharedPreferences(
                                "userdata",
                                Context.MODE_PRIVATE
                            )
                            val editor = prefs!!.edit()
                            editor.putString("id", id).apply()
                            editor.putString("nickname", nickname).apply()
                            editor.putString("profile", fileName).apply()

                            finish()

                        } else {
                            // 값이 정상적으로 들어가지 못했을때 예외처리
                            var tmpStringArray = tmpString.split("'")
                            var errorCode = tmpStringArray[3] // 에러 키값

                            when (errorCode) {
                                "user_id" -> Toast.makeText(
                                    applicationContext,
                                    "사용중인 아이디입니다.",
                                    Toast.LENGTH_LONG
                                ).show()
                                "user_nickname" -> Toast.makeText(
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
        } else { // 데이터가 비정상일 경우
            Toast.makeText(this, "닉네임 혹은 비밀번호 입력이 잘못되었습니다",Toast.LENGTH_LONG).show()
        }
    }


    fun photoClicked(view: View) {
        /* 사진을 클릭했을때 메소드
           사진을 변경할수있는 다이얼로그를 보여준다

         */
        // 사진 권한 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            ) {
                // 이미 권한이 있으면 바로 권한 설정 완료
            } else {
                // 권한이 없을때 권한 설정 요청
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    1
                )
            }
        }
        var items: Array<String>?

        if (mMainPhotoPath == "0") { // 사진이 없을때 사진 추가 다이얼로그

            val ListItems = ArrayList<String>()
            ListItems.add("카메라로 이동")
            ListItems.add("갤러리로 이동")
            items = ListItems.toTypedArray()

        } else { // 사진이 있을때 사진 수정/삭제 다이얼로그

            val ListItems = ArrayList<String>()
            ListItems.add("카메라로 이동")
            ListItems.add("갤러리로 이동")
            ListItems.add("사진 삭제")
            items = ListItems.toTypedArray()

        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("사진 등록 선택")
        builder.setItems(items, DialogInterface.OnClickListener { dialog, pos ->
            when (pos) {
                0 -> dispatchTakePictureIntent()//사진찍어서 저장하는 메소드로 이동
                1 -> goToAlbum()//갤러리에서 사진을 가져오는 메소드로 이동
                2 -> deletePhoto() // 사진 삭제 메소드
            }

        })

        builder.setNegativeButton("취소",
            DialogInterface.OnClickListener { dialog, which ->
                Toast.makeText(
                    applicationContext,
                    "취소하셨습니다.",
                    Toast.LENGTH_LONG
                ).show()
            })
        builder.show()
    }


    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // 카메라 액티비티를 실행하는 인텐트를 만든다.

        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // 카메라가 실행되기전에 파일을 먼저 만들어줌
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                // 예외처리
            }

            // 파일이 잘 만들어졌을때만 카메라 액티비티를 실행함.
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(
                    this,
                    "com.example.firststep2.fileprovider",
                    photoFile
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
            }
        }
    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        // 이미지파일의 이름을 지정해줌
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName, /* prefix */
            ".png", /* suffix */
            storageDir      /* directory */
        )

        // 이후에 다른곳에서 사용할 절대경로를 String 전역변수로 저장함
        mMainPhotoPath = image.absolutePath

        return image
    }


    private fun goToAlbum() {

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        startActivityForResult(intent, TAKE_PICTURE)
        // 갤러리로 감
    }


    private fun deletePhoto() {
        // 사진 삭제 시 사진을 초기화함
        iv_photo.setImageResource(R.drawable.signin_profilephoto)
        mMainPhotoPath = "0"
        photoIs = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_TAKE_PHOTO -> {
                    // 사진찍기를 실행한 상태라면
                    if (resultCode == RESULT_OK) {
                        val file = File(mMainPhotoPath)
                        // createImageFile 메소드에서 만들어둔 경로값을 가져옴
                        var bitmap: Bitmap? = null
                        try {
                            bitmap = MediaStore.Images.Media
                                .getBitmap(contentResolver, Uri.fromFile(file))
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                        // 그 경로값에 사진을 비트맵 형태로 넣음

                        if (bitmap != null) {
                            var ei: ExifInterface? = null
                            try {
                                ei = ExifInterface(mMainPhotoPath)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }

                            val orientation = ei!!.getAttributeInt(
                                ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_UNDEFINED
                            )
                            // 비트맵 파일이 있다면 사진을 돌려서 세트함
                            rotatedBitmap = null
                            when (orientation) {

                                ExifInterface.ORIENTATION_ROTATE_90 -> rotatedBitmap =
                                    rotateImage(bitmap, 90f)

                                ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap =
                                    rotateImage(bitmap, 180f)

                                ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap =
                                    rotateImage(bitmap, 270f)

                                ExifInterface.ORIENTATION_NORMAL -> rotatedBitmap = bitmap
                                else -> rotatedBitmap = bitmap
                            }
                            iv_photo.setImageBitmap(rotatedBitmap)
                            var out = FileOutputStream(mMainPhotoPath)
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                            out.close()

                        }
                    }

                }
                TAKE_PICTURE -> {

                    val photoUri = data?.data


                    var cursor: Cursor? = null

                    try {

                        /*
                         *  Uri 스키마를
                         *  content:/// 에서 file:/// 로  변경한다.
                         */
                        val proj = arrayOf(MediaStore.Images.Media.DATA)

                        assert(photoUri != null)
                        cursor = contentResolver.query(photoUri, proj, null, null, null)

                        assert(cursor != null)
                        val column_index =
                            cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

                        cursor.moveToFirst()

                        tempFile = File(cursor.getString(column_index))
                        mMainPhotoPath = tempFile!!.path

                    } finally {
                        cursor?.close()
                    }

                    val options = BitmapFactory.Options()
                    val originalBm = BitmapFactory.decodeFile(tempFile!!.absolutePath, options)

                    iv_photo.setImageBitmap(originalBm)

                }
            }
            photoIs = true

        } else {
            if (photoIs) {

            } else {
                mMainPhotoPath = "0"

            }
        }

    }

    fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)


        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
        //        사진을 찍은 그대로 가져오면 돌아가있기때문에 해당 사진을 돌리는 메소드를 미리 만들어둠
    }
}




