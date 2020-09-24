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
            if (query.groupBy != null) {
                set + query.groupBy.field
            } else {
                set
            }
        }
        for (name in columnNames) {
            if (!tableInfo.allowedColumnNames.contains(name)) {
                throw FieldNotFoundException("Unable to find field = $name")
            }
        }
        return jdbi.withHandle<List<AggregatedMetric>, java.lang.Exception> { handle ->
            val whereClause = buildWhere(query.filter)
            val aggFunction = buildAgg(query.groupBy)
            val groupByClause = buildGroupBy(period, query)
            val selectClause = buildSelectClause(period, query)
            val querySt = handle.createQuery("""
                        |SELECT $selectClause, $aggFunction as value FROM ${tableInfo.name}
                        | WHERE $whereClause
                        | GROUP BY $groupByClause
                        | ORDER BY time
                        |""".trimMargin())
            for (match in query.filter.matches) {
                querySt.bind(match.key, match.value)
            }

            val dates: List<String> = (listOf(period.date) + period.offsetsBefore.map { period.date.minusDays(it.toLong()) })
                    .map { it.format(DateTimeFormatter.ISO_DATE) }

            querySt.bindList("dates", dates)
            LOG.debug("Query = {}", query)
            query.toString()
            querySt.map { rs, _ ->
                val time = rs.getString("time")
                val value = rs.getDouble("value")
                val name = if (query.groupBy != null) rs.getString(query.groupBy.field) else "value"
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
        filters.add("date in (<dates>)")
        return filters.joinToString(" AND ")
    }

    private fun buildGroupBy(period: AggregationPeriod, query: Query): Any {
        val time = if (period.groupByDay) {
            "date"
        } else {
            "timeMinute"
        }
        return if (query.groupBy != null) {
            time + ", " + query.groupBy.field
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
        return if (query.groupBy != null) {
            time + "," + query.groupBy.field
        } else {
            time
        }
    }

    private fun buildAgg(groupBy: GroupBy?): String {
        return if (groupBy != null) {
            "${groupBy.function}(${groupBy.field})"
        } else {
            "count(*)"
        }
    }
}