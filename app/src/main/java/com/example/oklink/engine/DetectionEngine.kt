package com.example.oklink.engine

data class ScanResult(
    val score: Int,
    val level: String,
    val reasons: List<String>
)

object DetectionEngine {

    fun analyze(url: String): ScanResult {

        val lowerUrl = url.lowercase()
        var score = 0
        val reasons = mutableListOf<String>()

        if (lowerUrl.contains("free") ||
            lowerUrl.contains("win") ||
            lowerUrl.contains("lottery")) {
            score += 30
            reasons.add("Contains common scam keywords")
        }

        if (lowerUrl.length > 60) {
            score += 20
            reasons.add("URL is unusually long")
        }

        if (!lowerUrl.startsWith("https")) {
            score += 20
            reasons.add("Not using secure HTTPS")
        }

        if (lowerUrl.count { it.isDigit() } > 5) {
            score += 20
            reasons.add("Too many numbers in URL")
        }

        val level = when {
            score >= 70 -> "HIGH RISK"
            score >= 40 -> "SUSPICIOUS"
            else -> "SAFE"
        }

        return ScanResult(score, level, reasons)
    }
}