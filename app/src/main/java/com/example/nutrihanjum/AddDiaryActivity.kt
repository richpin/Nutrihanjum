package com.example.nutrihanjum

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.Camera
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.example.nutrihanjum.databinding.ActivityAddDiaryBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.content.ClipData

import android.os.Build




class AddDiaryActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAddDiaryBinding
    private lateinit var photoLauncher: ActivityResultLauncher<Uri>
    private lateinit var photoURI: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddDiaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        photoLauncher = registerForActivityResult(TakePictureContract()) {
            if (it) {
                binding.imageviewPreview.setImageURI(photoURI)
            }
        }

        binding.imageviewPreview.setOnClickListener {
            getPhoto()
        }

        getPhoto()
    }

    private fun getPhoto() {
        val photoFile = createImageFile()

        photoFile?.also {
            photoURI = FileProvider.getUriForFile(this, "${packageName}.fileprovider", it)
            photoLauncher.launch(photoURI)
        }
    }

    private fun createImageFile(): File? {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return try {
            File.createTempFile("DIARY_${timestamp}_", ".jpg", storageDir)
        } catch (e: Exception) {
            null
        }
    }

    inner class TakePictureContract: ActivityResultContracts.TakePicture() {
        override fun createIntent(context: Context, input: Uri): Intent {
            val takePictureIntent = super.createIntent(context, input)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                takePictureIntent.clipData = ClipData.newRawUri("", photoURI)
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            return takePictureIntent
        }
    }

}