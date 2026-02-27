package com.example.oklink.network

data class AnalysisResponse(
    val data: AnalysisData
)

data class AnalysisData(
    val attributes: Attributes
)

data class Attributes(
    val stats: Stats
)

data class Stats(
    val harmless: Int,
    val malicious: Int,
    val suspicious: Int
)