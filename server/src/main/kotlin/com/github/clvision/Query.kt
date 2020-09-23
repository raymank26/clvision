package com.github.clvision

typealias Field = String
typealias Pattern = String

data class Query(val groupBy: GroupBy?, val filter: Filter, val tableId: Int)

class Filter(val matches: Map<Field, Pattern>)

data class GroupBy(val field: String, val function: String)
