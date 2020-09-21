package com.github.clvision

import org.jdbi.v3.core.Jdbi
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.temporal.ChronoUnit

private val LOG = LoggerFactory.getLogger(ClickhouseDao::class.java)
private val SPECIAL_COLUMNS = setOf("source", "sourceGroup", "type", "duration", "timeMinute")

class ClickhouseDao(
        private val jdbi: Jdbi,
        private val tableRegistry: TableRegistry

) {

    private val aggregationService = AggregationService(jdbi, tableRegistry)

    fun insertMetrics(rawMetrics: List<Metric>) {
        for ((tableId, metrics: List<Metric>) in rawMetrics.groupBy { it.tableId }) {
            val table = tableRegistry.getNameById(tableId)
            if (table == null) {
                LOG.warn("No table found for id = $tableId")
                return
            }
            val tableName = table.name
            val availableColumnNames = table.columns.map { it.name }.filter { !SPECIAL_COLUMNS.contains(it) }
            val joinedColumnNames = availableColumnNames.joinToString(", ")
            val joinedColumnPlaceholders = availableColumnNames.joinToString(", ") { ":$it" }

            jdbi.useHandle<Exception> { handle ->
                val preparedBatch = handle.prepareBatch("INSERT INTO $tableName(source, sourceGroup, type, duration, timeMinute, $joinedColumnNames)" +
                        " VALUES (:source, :sourceGroup, :type, :duration, :timeMinute, $joinedColumnPlaceholders)")
                for (metric in metrics) {
                    preparedBatch
                            .bind("source", metric.source)
                            .bind("sourceGroup", metric.sourceGroup)
                            .bind("type", metric.type.id)
                            .bind("duration", metric.duration)
                            .bind("timeMinute", Instant.ofEpochMilli(metric.timestamp).truncatedTo(ChronoUnit.MINUTES))
                    for ((index, columnName) in availableColumnNames.withIndex()) {
                        preparedBatch.bind(columnName, metric.parameters.getOrNull(index))
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

