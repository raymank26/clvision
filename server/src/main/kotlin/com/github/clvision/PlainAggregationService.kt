package com.github.clvision

import org.jdbi.v3.core.Jdbi
import org.slf4j.LoggerFactory
import java.time.format.DateTimeFormatter

private val LOG = LoggerFactory.getLogger(PlainAggregationService::class.java)

class PlainAggregationService(
        private val jdbi: Jdbi,
        private val tableRegistry: TableRegistry,
) {

    fun aggregateMetrics(period: AggregationPeriod, query: Query): List<AggregatedMetric> {
        val tableInfo = tableRegistry.getNameById(query.tableId)
        if (tableInfo == null) {
            LOG.warn("Unable to find tableInfo by id = " + query.tableId)
            return emptyList()
        }
        return when (query) {
            is Query.GroupedBy -> TODO()
            is Query.Plain ->
                jdbi.withHandle<List<AggregatedMetric>, java.lang.Exception> { handle ->
                    val whereClause = buildWhere(query.filter, tableInfo)
                    val aggFunction = buildAgg(query.filter)
                    val groupByClause = buildGroupBy(period)
                    val selectClause = buildSelectClause(period)
                    val querySt = handle.createQuery("""
                        |SELECT $selectClause, $aggFunction as value FROM ${tableInfo.name}
                        | WHERE $whereClause
                        | GROUP BY $groupByClause
                        |""".trimMargin())
                    for (match in query.filter.matches) {
                        querySt.bind(match.key, match.value)
                    }
                    querySt.bindList("dates", period.days.map { it.format(DateTimeFormatter.ISO_DATE) })
                    LOG.debug("Query = {}", query)
                    query.toString()
                    querySt.map { rs, _ ->
                        val time = rs.getString("time")
                        val value = rs.getDouble("value")
                        AggregatedMetric("value", time, value)
                    }.list()
                }
        }
    }

    private fun buildWhere(filter: Filter, tableInfo: TableInfo): String {
        val filters = mutableListOf<String>()
        val availableColumns = tableInfo.colums.map { it.name }.toSet()
        for (match in filter.matches) {
            if (!availableColumns.contains(match.key)) {
                error("Column = " + match.key + " is not found")
            }
            val field = match.key
            filters.add("$field LIKE :$field")
        }
        filters.add("type = ${filter.type.id}")
        filters.add("date in (<dates>)")
        return filters.joinToString(" AND ")
    }

    private fun buildGroupBy(period: AggregationPeriod): Any {
        return if (period.groupByDay) {
            "date"
        } else {
            "timeMinute"
        }
    }

    private fun buildSelectClause(period: AggregationPeriod): Any {
        return if (period.groupByDay) {
            "date as time"
        } else {
            "timeMinute as time"
        }
    }

    private fun buildAgg(filter: Filter): String {
        return if (filter.showDuration) {
            "sum(duration)"
        } else {
            "count(*)"
        }
    }
}