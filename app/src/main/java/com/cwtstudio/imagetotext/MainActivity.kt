package com.cwtstudio.imagetotext

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cwtstudio.imagetotext.databinding.ActivityMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class MainActivity : AppCompatActivity() {
    /**
     * initialize the recognizer and the binding using lazy, so it will be initialized only when
     * first used IDK !
     */
    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    /**
     * request code for the camera image activity result and bitmap for holding the image that we
     * get, now its currently null
     */
    private val REQUEST_CODE = 101
    private var bitmapImage: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        /** call the capture image function when btnCapture clicked */
        binding.btnCapture.setOnClickListener { captureImage() }
        /**
         * when btnCopy is clicked first check if its empty or not. if its empty don't copy anything
         * and just exit or return. else if there is text then copy it to clipboard.
         */
        binding.btnCopy.setOnClickListener {
            if (binding.txtResult.text.toString().isEmpty()) {
                Toast.makeText(this, "Nothing to copy.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val text = binding.txtResult.text.toString()
            val clipboardManager: ClipboardManager =
                getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("ocr", text)
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(this, "Copied successfully.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * create a intent to open camera usin the ACTION_IMAGE_CAPTURE and startActivityForResult with
     * the intent and the request code as argument.
     */
    private fun captureImage() {
        val intentCaptureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(intentCaptureImage, REQUEST_CODE)
        } catch (e: Exception) {
            Toast.makeText(this@MainActivity, "Failed to capture image.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * extract the text from the bitmap image and set it to the text view if successful else show a
     * toast
     */
    private fun extractText() {
        if (bitmapImage != null) {
            val image = bitmapImage?.let { InputImage.fromBitmap(it, 0) }
            image?.let { it ->
                recognizer
                    .process(it)
                    .addOnSuccessListener { binding.txtResult.text = it.text }
                    .addOnFailureListener {
                        Toast.makeText(
                            this@MainActivity,
                            "Failed to extract text.",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
            }
        } else {
            Toast.makeText(this@MainActivity, "Please select a image.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * here if the request code matches with out request code and the result is result ok then get
     * the extras and convert it into bitmap or typecast it as bitmap and store it. then set it in
     * the view and call the extract function which will handle the rest
     */
    @Deprecated("")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            val extras: Bundle? = data?.extras
            bitmapImage = extras?.get("data") as Bitmap
            if (bitmapImage != null) {
                binding.capturedImage.setImageBitmap(bitmapImage)
            }
            extractText()
        }
    }
}
