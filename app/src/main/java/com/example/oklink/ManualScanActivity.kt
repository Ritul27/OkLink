package com.example.oklink

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.oklink.engine.DetectionEngine

class ManualScanActivity : AppCompatActivity() {

    private lateinit var urlInput: EditText
    private lateinit var scanButton: Button
    private lateinit var resultText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_scan)

        urlInput = findViewById(R.id.urlInput)
        scanButton = findViewById(R.id.scanButton)
        resultText = findViewById(R.id.resultText)

        // Handle shared links
        if (intent?.action == Intent.ACTION_SEND) {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!sharedText.isNullOrEmpty()) {
                urlInput.setText(sharedText)
            }
        }

        scanButton.setOnClickListener {
            val url = urlInput.text.toString()

            if (url.isNotEmpty()) {
                val result = DetectionEngine.analyze(url)

                val displayText = """
                    Level: ${result.level}
                    
                    Score: ${result.score}
                    
                    Reasons:
                    ${result.reasons.joinToString("\n")}
                """.trimIndent()

                resultText.text = displayText
            }
        }
    }
}