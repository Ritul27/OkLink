package com.example.oklink

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import com.example.oklink.network.RetrofitClient

class AutoScanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auto_scan)

        val url = intent?.dataString ?: "No URL received"

        val urlText = findViewById<TextView>(R.id.urlText)
        val resultText = findViewById<TextView>(R.id.resultText)
        val openButton = findViewById<Button>(R.id.openButton)
        val cancelButton = findViewById<Button>(R.id.cancelButton)

        urlText.text = url

        resultText.text = "Scanning with VirusTotal..."

        lifecycleScope.launch {

            try {
                val mediaType = "application/x-www-form-urlencoded".toMediaType()
                val body = "url=$url".toRequestBody(mediaType)

                // Step 1: Submit URL
                val submitResponse = RetrofitClient.api.submitUrl(body)

                if (submitResponse.isSuccessful) {

                    val analysisId = submitResponse.body()?.data?.id

                    if (analysisId != null) {

                        delay(3000) // wait for analysis to complete

                        // Step 2: Get analysis result
                        val analysisResponse =
                            RetrofitClient.api.getAnalysis(analysisId)

                        if (analysisResponse.isSuccessful) {

                            val stats = analysisResponse.body()?.data?.attributes?.stats

                            if (stats != null) {

                                val malicious = stats.malicious
                                val suspicious = stats.suspicious

                                val riskLevel = when {
                                    malicious > 5 -> "HIGH RISK"
                                    suspicious > 3 -> "SUSPICIOUS"
                                    else -> "SAFE"
                                }

                                resultText.text =
                                    "Risk Level: $riskLevel\n\n" +
                                            "Malicious: ${stats.malicious}\n" +
                                            "Suspicious: ${stats.suspicious}\n" +
                                            "Harmless: ${stats.harmless}"

                            } else {
                                resultText.text = "Failed to read analysis data."
                            }

                        } else {
                            resultText.text = "Failed to fetch analysis result."
                        }

                    } else {
                        resultText.text = "Invalid analysis ID."
                    }

                } else {
                    resultText.text = "Failed to submit URL."
                }

            } catch (e: Exception) {
                resultText.text = "Error: ${e.message}"
            }
        }
        openButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.setPackage("com.android.chrome")
            startActivity(intent)
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }
}