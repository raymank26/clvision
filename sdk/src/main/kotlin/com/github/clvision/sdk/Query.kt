package com.github.clvision.sdk

import java.time.LocalDate

typealias Field = String
typealias Pattern = String

data class AggregationPeriod(val groupByDay: Boolean, val date: LocalDate, val offsetsBefore: List<Int>)

data class Query(val aggregationPeriod: AggregationPeriod, val groupBy: GroupBy?, val filter: Filter, val tableId: Int)

data class Filter(val matches: Map<Field, Pattern>)

data class GroupBy(val field: String, val function: String)
