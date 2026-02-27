package com.example.oklink.network

data class AnalysisResponse(
    val data: AnalysisData
)

data class AnalysisData(
    val attributes: AnalysisAttributes
)

data class AnalysisAttributes(
    val status: String,
    val stats: Stats
)

data class Stats(
    val malicious: Int,
    val suspicious: Int,
    val harmless: Int
)