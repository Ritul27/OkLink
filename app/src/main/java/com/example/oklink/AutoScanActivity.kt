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
import retrofit2.Response
import com.example.oklink.network.RetrofitClient
import com.example.oklink.network.AnalysisResponse

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

                // STEP 1: Submit URL
                val submitResponse = RetrofitClient.api.submitUrl(body)

                if (!submitResponse.isSuccessful) {
                    resultText.text = "Failed to submit URL."
                    return@launch
                }

                val analysisId = submitResponse.body()?.data?.id

                if (analysisId == null) {
                    resultText.text = "Invalid analysis ID."
                    return@launch
                }

                var analysisResponse: Response<AnalysisResponse>? = null
                var status = ""

                // STEP 2: Poll until analysis completed
                repeat(5) {

                    delay(3000)

                    analysisResponse =
                        RetrofitClient.api.getAnalysis(analysisId)

                    if (!analysisResponse.isSuccessful) {
                        resultText.text = "API Error: ${analysisResponse.code()}"
                        return@launch
                    }

                    status = analysisResponse
                        ?.body()
                        ?.data
                        ?.attributes
                        ?.status ?: ""

                    if (status == "completed") return@repeat
                }

                val stats = analysisResponse
                    ?.body()
                    ?.data
                    ?.attributes
                    ?.stats

                if (stats == null) {
                    resultText.text = "Failed to read analysis data."
                    return@launch
                }

                val malicious = stats.malicious
                val suspicious = stats.suspicious
                val harmless = stats.harmless

                val riskLevel = when {
                    malicious > 5 -> "HIGH RISK"
                    suspicious > 3 -> "SUSPICIOUS"
                    else -> "SAFE"
                }

                resultText.text =
                    "Risk Level: $riskLevel\n\n" +
                            "Malicious: $malicious\n" +
                            "Suspicious: $suspicious\n" +
                            "Harmless: $harmless"

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