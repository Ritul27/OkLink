package com.example.oklink

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.oklink.network.AnalysisResponse
import com.example.oklink.network.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

class ManualScanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_scan)

        val urlInput = findViewById<EditText>(R.id.urlInput)
        val scanButton = findViewById<Button>(R.id.scanButton)
        val resultText = findViewById<TextView>(R.id.resultText)

        scanButton.setOnClickListener {

            val url = urlInput.text.toString().trim()

            if (url.isEmpty()) {
                Toast.makeText(this, "Please enter a URL", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            resultText.text = "Scanning..."
            resultText.setTextColor(Color.GRAY)

            lifecycleScope.launch {

                try {
                    val mediaType = "application/x-www-form-urlencoded".toMediaType()
                    val body = "url=$url".toRequestBody(mediaType)

                    // STEP 1: Submit URL
                    val submitResponse = RetrofitClient.api.submitUrl(body)

                    if (!submitResponse.isSuccessful) {
                        resultText.text = "Failed to submit URL"
                        resultText.setTextColor(Color.RED)
                        return@launch
                    }

                    val analysisId = submitResponse.body()?.data?.id

                    if (analysisId == null) {
                        resultText.text = "Invalid analysis ID"
                        resultText.setTextColor(Color.RED)
                        return@launch
                    }

                    var analysisResponse: Response<AnalysisResponse>? = null
                    var status = ""

                    // Polling VirusTotal
                    repeat(10) {
                        delay(2000)
                        analysisResponse =
                            RetrofitClient.api.getAnalysis(analysisId)

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
                        resultText.text = "Failed to get results"
                        resultText.setTextColor(Color.RED)
                        return@launch
                    }

                    val malicious = stats.malicious
                    val suspicious = stats.suspicious

                    val (riskText, color) = when {
                        malicious > 5 -> Pair("DANGEROUS ⚠", Color.RED)
                        suspicious > 3 -> Pair("SUSPICIOUS ⚠", Color.YELLOW)
                        else -> Pair("SAFE ✓", Color.parseColor("#2E8B57"))
                    }

                    resultText.text = riskText
                    resultText.setTextColor(color)

                } catch (e: Exception) {
                    resultText.text = "Error: ${e.message}"
                    resultText.setTextColor(Color.RED)
                }
            }
        }
    }
}