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
            val availableColumnNames = table.allowedColumns
            val joinedColumnNames = availableColumnNames.joinToString(", ")
            val joinedColumnPlaceholders = availableColumnNames.joinToString(", ") { ":$it" }

            jdbi.useHandle<Exception> { handle ->
                val preparedBatch = handle.prepareBatch("INSERT INTO $tableName(timeMinute, $joinedColumnNames)" +
                        " VALUES (:timeMinute, $joinedColumnPlaceholders)")
                for (metric in metrics) {
                    preparedBatch
                            .bind("timeMinute", Instant.ofEpochMilli(metric.timestamp).truncatedTo(ChronoUnit.MINUTES))
                    val nameToValue: Map<String, MetricColumn> = metric.parameters.associateBy { it.name }
                    for (columnName in availableColumnNames) {
                        val column: MetricColumn? = nameToValue[columnName]
                        if (column == null) {
                            preparedBatch.bind(columnName, "null")
                        } else {
                            when (column.value) {
                                is MetricDouble ->
                                    preparedBatch.bind(columnName, column.value.value)
                                is MetricString ->
                                    preparedBatch.bind(columnName, column.value.value)
                            }
                        }
                    }
                    for (parameter in metric.parameters) {
                        if (!table.allowedColumns.contains(parameter.name)) {
                            throw FieldNotFoundException("No field found name = ${parameter.name}")
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

