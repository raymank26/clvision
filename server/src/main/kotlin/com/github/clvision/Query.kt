package com.github.clvision

typealias Field = String
typealias Pattern = String

sealed class Query(val tableId: Int) {
    class GroupedBy(val field: String, val filter: Filter, tableId: Int): Query(tableId)
    class Plain(val filter: Filter, tableId: Int): Query(tableId)
}

class Filter(val matches: Map<Field, Pattern>, val type: MetricType, val showDuration: Boolean)
