package com.github.clvision

typealias Field = String
typealias Pattern = String

data class Query(val groupByField: String?, val filter: Filter, val tableId: Int)

class Filter(val matches: Map<Field, Pattern>, val type: MetricType, val showDuration: Boolean)
