package com.techdoctorbd.imageencodedecode

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.techdoctorbd.imageencodedecode.databinding.ActivityMainBinding
import java.io.ByteArrayOutputStream


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private var encodedImage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonEncode.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    100
                )
            } else {
                selectImage()
            }
        }

        binding.buttonDecode.setOnClickListener {
            val text = binding.edEncodedText.text.toString()
            if (!TextUtils.isEmpty(text)) {
                try {
                    val bytes = Base64.decode(text, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    binding.imageDecoded.setImageBitmap(bitmap)
                } catch (exception: Exception) {
                    Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
                }
            } else {
                binding.edEncodedText.error = "Enter encoded text here"
                binding.edEncodedText.requestFocus()
            }
        }

        binding.buttonCopy.setOnClickListener {
            copyToClipboard(encodedImage!!)
        }
    }

    private fun selectImage() {
        binding.tvEncoded.text = ""
        binding.imageDecoded.setImageBitmap(null)

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, 101)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectImage()
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            val uri = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                val stream = ByteArrayOutputStream()
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val bytes = stream.toByteArray()
                encodedImage = Base64.encodeToString(bytes, Base64.DEFAULT)
                binding.tvEncoded.text = encodedImage
                binding.buttonCopy.visibility = View.VISIBLE
            } catch (e: Exception) {
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun copyToClipboard(hashData: String) {
        val clipBoardManager = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Encrypted text", hashData)
        clipBoardManager.setPrimaryClip(clipData)

        Toast.makeText(this, "Text copied", Toast.LENGTH_SHORT).show()
    }
}