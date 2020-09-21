package com.github.clvision

import org.jdbi.v3.core.Jdbi
import org.slf4j.LoggerFactory
import java.time.format.DateTimeFormatter

private val LOG = LoggerFactory.getLogger(AggregationService::class.java)

class AggregationService(
        private val jdbi: Jdbi,
        private val tableRegistry: TableRegistry,
) {

    fun aggregateMetrics(period: AggregationPeriod, query: Query): List<AggregatedMetric> {
        val tableInfo = tableRegistry.getNameById(query.tableId)
                ?: error("Unable to find tableInfo by id = " + query.tableId)
        val columnNames: Set<String> = query.filter.matches.keys.let { set ->
            if (query.groupByField != null) {
                set + query.groupByField
            } else {
                set
            }
        }
        val availableColumnNames = tableInfo.colums.map { it.name }
        for (name in columnNames) {
            if (!availableColumnNames.contains(name)) {
                error("Unable to find columnName = '$name'")
            }
        }
        return jdbi.withHandle<List<AggregatedMetric>, java.lang.Exception> { handle ->
            val whereClause = buildWhere(query.filter)
            val aggFunction = buildAgg(query.filter)
            val groupByClause = buildGroupBy(period, query)
            val selectClause = buildSelectClause(period, query)
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
                val name = if (query.groupByField != null) {
                    rs.getString(query.groupByField)
                } else {
                    "value"
                }
                AggregatedMetric(name, time, value)
            }.list()
        }
    }

    private fun buildWhere(filter: Filter): String {
        val filters = mutableListOf<String>()
        for (match in filter.matches) {
            val field = match.key
            filters.add("$field LIKE :$field")
        }
        filters.add("type = ${filter.type.id}")
        filters.add("date in (<dates>)")
        return filters.joinToString(" AND ")
    }

    private fun buildGroupBy(period: AggregationPeriod, query: Query): Any {
        val time = if (period.groupByDay) {
            "date"
        } else {
            "timeMinute"
        }
        return if (query.groupByField != null) {
            time + ", " + query.groupByField
        } else {
            time
        }
    }

    private fun buildSelectClause(period: AggregationPeriod, query: Query): Any {
        val time = if (period.groupByDay) {
            "date as time"
        } else {
            "timeMinute as time"
        }
        return if (query.groupByField != null) {
            time + "," + query.groupByField
        } else {
            time
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