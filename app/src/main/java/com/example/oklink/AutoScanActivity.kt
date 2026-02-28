package com.example.oklink

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.oklink.network.RetrofitClient
import com.example.oklink.network.AnalysisResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

class AutoScanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val isAutoEnabled = prefs.getBoolean("auto_mode", false)

        val incomingUri: Uri? = intent?.data
        val urlString = incomingUri?.toString() ?: ""

        // 🔹 If Auto Mode OFF → Open immediately (no scan)
        if (!isAutoEnabled) {
            if (incomingUri != null) {
                val browserIntent = Intent(Intent.ACTION_VIEW, incomingUri)
                startActivity(browserIntent)
            }
            finish()
            return
        }

        // 🔹 Auto Mode ON → Show scanning screen
        setContentView(R.layout.activity_auto_scan)

        val urlText = findViewById<TextView>(R.id.urlText)
        val resultText = findViewById<TextView>(R.id.resultText)
        val openButton = findViewById<Button>(R.id.openButton)
        val cancelButton = findViewById<Button>(R.id.cancelButton)

        urlText.text = urlString
        resultText.text = "Scanning..."

        lifecycleScope.launch {
            try {

                val mediaType = "application/x-www-form-urlencoded".toMediaType()
                val body = "url=$urlString".toRequestBody(mediaType)

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

                // STEP 2: Poll until completed
                repeat(6) {
                    delay(3000)

                    analysisResponse =
                        RetrofitClient.api.getAnalysis(analysisId)

                    if (!analysisResponse!!.isSuccessful) {
                        resultText.text = "API Error"
                        resultText.setTextColor(Color.RED)
                        return@launch
                    }

                    status = analysisResponse!!
                        .body()
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
                    resultText.text = "Analysis Failed"
                    resultText.setTextColor(Color.RED)
                    return@launch
                }

                val malicious = stats.malicious
                val suspicious = stats.suspicious
                val harmless = stats.harmless

                val riskText: String
                val riskColor: Int

                if (malicious > 0 || suspicious > 3) {
                    riskText = "UNSAFE"
                    riskColor = Color.RED
                } else {
                    riskText = "SAFE"
                    riskColor = Color.GREEN
                }

                resultText.text =
                    "Status: $riskText\n\n" +
                            "Malicious: $malicious\n" +
                            "Suspicious: $suspicious\n" +
                            "Harmless: $harmless"

                resultText.setTextColor(riskColor)

            } catch (e: Exception) {
                resultText.text = "Error: ${e.message}"
                resultText.setTextColor(Color.RED)
            }
        }

        // 🔹 Open in browser button
        openButton.setOnClickListener {

            val browserIntent = Intent(Intent.ACTION_VIEW, incomingUri)

            // Force Chrome
            browserIntent.setPackage("com.android.chrome")

            try {
                startActivity(browserIntent)
            } catch (e: Exception) {
                // If Chrome not installed → show chooser
                val chooser = Intent.createChooser(
                    Intent(Intent.ACTION_VIEW, incomingUri),
                    "Open with"
                )
                startActivity(chooser)
            }

            finish()
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }
}