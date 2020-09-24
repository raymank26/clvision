package com.github.clvision

import org.jdbi.v3.core.Jdbi
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.temporal.ChronoUnit

private val LOG = LoggerFactory.getLogger(ClickhouseDao::class.java)

class ClickhouseDao(
        private val jdbi: Jdbi,
        private val tableRegistry: TableRegistry

) {

    private val aggregationService = AggregationService(jdbi, tableRegistry)

    fun insertMetrics(rawMetrics: List<Metric>) {
        for ((tableId, metrics: List<Metric>) in rawMetrics.groupBy { it.tableId }) {
            val table = tableRegistry.getNameById(tableId) ?: error("No table found for id = $tableId")

            val tableName = table.name
            val availableColumnNames = table.allowedColumnNames
            val joinedColumnNames = availableColumnNames.joinToString(", ")
            val joinedColumnPlaceholders = availableColumnNames.joinToString(", ") { ":$it" }

            jdbi.useHandle<Exception> { handle ->
                val preparedBatch = handle.prepareBatch("INSERT INTO $tableName(timeMinute, $joinedColumnNames)" +
                        " VALUES (:timeMinute, $joinedColumnPlaceholders)")

                for (metric in metrics) {
                    if (hasInvalidColumns(metric, table)) {
                        continue
                    }
                    preparedBatch
                            .bind("timeMinute", Instant.ofEpochMilli(metric.timestamp).truncatedTo(ChronoUnit.MINUTES))
                    val nameToValue: Map<String, MetricColumn> = metric.parameters.associateBy { it.name }
                    for (tableColumn in table.columns) {
                        val columnName = tableColumn.name
                        val metricColumn: MetricColumn? = nameToValue[columnName]
                        if (metricColumn == null) {
                            preparedBatch.bindNull(columnName, tableColumn.type)
                        } else {
                            when (metricColumn.value) {
                                is MetricDouble ->
                                    preparedBatch.bind(columnName, metricColumn.value.value)
                                is MetricString ->
                                    preparedBatch.bind(columnName, metricColumn.value.value)
                                is MetricByte ->
                                    preparedBatch.bind(columnName, metricColumn.value.value)
                                is MetricLong ->
                                    preparedBatch.bind(columnName, metricColumn.value.value)
                            }.exhaustive
                        }
                    }
                    preparedBatch.add()
                }
                val executeResult = preparedBatch.execute()
                val totalWritten = executeResult.sum()
                if (totalWritten < metrics.size) {
                    LOG.warn("""Expected written = $totalWritten, but ${metrics.size} found""")
                }
            }
        }
    }

    private fun hasInvalidColumns(metric: Metric, table: TableInfo): Boolean {
        val metricColumns: Set<String> = metric.parameters.mapTo(mutableSetOf()) { it.name }
        for (tableColumn in table.columns) {
            if (!tableColumn.nullable && !metricColumns.contains(tableColumn.name)) {
                LOG.warn("Nullability violated for column = $tableColumn, tableId = ${table.name}")
                return true
            }
        }
        return false
    }

    fun aggregateMetrics(period: AggregationPeriod, query: Query): List<AggregatedMetric> {
        return aggregationService.aggregateMetrics(period, query)
    }

    fun executeQuery(query: String) {
        jdbi.useHandle<java.lang.Exception> { handle ->
            var header: String? = null
            val result = handle.createQuery(query).map { rs, _ ->
                if (header == null) {
                    header = (1..rs.metaData.columnCount).map { rs.metaData.getColumnName(it) }.joinToString(",")
                }
                (1..rs.metaData.columnCount).joinToString(",") { rs.getString(it).toString() }
            }.list().take(10)
            LOG.info("Execution result = \n${header + '\n' + result}")
        }
    }
}

