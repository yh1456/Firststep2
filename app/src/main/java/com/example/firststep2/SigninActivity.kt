package com.example.firststep2

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
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
import com.google.gson.GsonBuilder
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class SigninActivity : AppCompatActivity() {
    var server: RetrofitService? = null // 레트로핏 연결용 변수
    var mMainPhotoPath: String = "0" // 사진 절대경로 저장용 변수
    var fileName: String = "0" // 서버 업로드용 이름 변수
    val REQUEST_TAKE_PHOTO = 1
    val TAKE_PICTURE = 2
    var rotatedBitmap: Bitmap? = null
    var tempFile: File? = null
    var photoIs = false // 사진 존재여부를 판단하여 사진 추가 취소시 분기용으로 사용

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

        iv_profile.background = ShapeDrawable(OvalShape())
        iv_profile.clipToOutline = true

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

            if (mMainPhotoPath != "0") {
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                fileName = "$id$timeStamp.png"
            } else {
                fileName = "0"
            }


            // 서버에 데이터 전송.
            server?.signinRequest(id, pw, nickname, fileName)?.enqueue(object : Callback<signinDTO> {
                override fun onFailure(call: Call<signinDTO>?, t: Throwable?) {
                    Log.e("확인 Fail1", t.toString())
                }

                override fun onResponse(call: Call<signinDTO>?, response: Response<signinDTO>?) {
                    var tmpString = response?.body().toString()
                    Log.d("확인 Response", tmpString)

                    if (tmpString == "signinDTO(result=쿼리 정상 작동)") {
                        // 데이터 전송에 이상없을시

                        // 사진 있을때 사진 업로드 추가
                        if (mMainPhotoPath != "0"){

                            val file = File(mMainPhotoPath)

                            var requestBody : RequestBody = RequestBody.create(MediaType.parse("image/*"),file)
                            var body : MultipartBody.Part = MultipartBody.Part.createFormData("uploaded_file",fileName,requestBody)

                            server?.post_Porfile_Request(id,body)?.enqueue(object: Callback<String> {
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


                        // 로그인 액티비티로 이동. intent에 아이디 입력
                        val intent = Intent()
                        intent.putExtra("id", id)
                        setResult(100, intent)
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


    }

    fun photoAdd(view: View) {
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
        iv_profile.setImageResource(R.drawable.signin_profilephoto)
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
                            iv_profile.setImageBitmap(rotatedBitmap)
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

                    iv_profile.setImageBitmap(originalBm)

                }
            }
            photoIs = true

        } else {
            if (photoIs){

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
