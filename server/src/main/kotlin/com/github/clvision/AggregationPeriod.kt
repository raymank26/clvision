package com.github.clvision

import java.time.LocalDate

data class AggregationPeriod(val groupByDay: Boolean, val dates: List<LocalDate>)

