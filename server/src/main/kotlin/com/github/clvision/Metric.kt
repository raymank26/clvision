package com.github.clvision

data class Metric(
        val source: String,
        val sourceGroup: String,
        val tableId: Int,
        val type: MetricType,
        val duration: Long,
        val parameters: List<String>,
        val timestamp: Long
)