package com.cloudvision.libdemo

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.cloud.vision.label.detection.LabelDetection
import com.cloud.vision.label.detection.OnLabelDetectionResult


class MainActivity : AppCompatActivity(), OnLabelDetectionResult {

    lateinit var resultTextView : TextView
    lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        resultTextView = findViewById(R.id.result)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage(getString(R.string.please_wait))


        findViewById<View>(R.id.select_image_button).setOnClickListener {
            resultTextView .text = ""
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            someActivityResultLauncher.launch(intent)

        }

    }

    var someActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageUri = result.data!!.data
            val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            val imageView : ImageView = findViewById(R.id.selected_image)
            imageView.setImageBitmap(bitmap)

            progressDialog.show()
            LabelDetection(this@MainActivity,getString(R.string.api_key),this,imageUri!!)


        }
    }

    override fun onResult(result: String?) {
        progressDialog.dismiss()
       resultTextView.text = result
    }

    override fun onFailure() {
        progressDialog.dismiss()
        resultTextView.text = getString(R.string.result_not_found)
    }
}