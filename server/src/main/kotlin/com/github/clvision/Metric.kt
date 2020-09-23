package com.github.clvision

data class Metric(
        val tableId: Int,
        val parameters: List<MetricColumn>,
        val timestamp: Long
)

data class MetricColumn(val name: String, val value: MetricValue)

sealed class MetricValue

data class MetricDouble(val value: Double): MetricValue()
data class MetricString(val value: String): MetricValue()
data class MetricByte(val value: Byte): MetricValue()
data class MetricLong(val value: Long): MetricValue()
