package com.github.clvision

import org.amshove.kluent.shouldBeEqualTo
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class OrderingTest {

    @Rule
    @JvmField
    val ch = ClickHouseTableRule()

    private val clickhouseDao: ClickhouseDao
        get() {
            return ch.clickHouseDao
        }

    @Test
    fun testProperOrder() {
        val aggMetrics = processMetrics({ offset -> TimeUnit.MINUTES.toMillis(offset) }, QUERY_PERIOD)

        val times = aggMetrics.map { LocalDateTime.parse(it.time, DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")) }
        times shouldBeEqualTo times.sorted()
    }

    @Test
    fun testProperOrderDays() {
        val aggMetrics = processMetrics({ offset -> TimeUnit.DAYS.toMillis(offset) }, QUERY_PERIOD.copy(groupByDay = true, offsetsBefore = (1 until 10).toList()))

        val times = aggMetrics.map { LocalDate.parse(it.time, DateTimeFormatter.ofPattern("uuuu-MM-dd")) }
        times shouldBeEqualTo times.sorted()
    }

    private fun processMetrics(offsetProvider: (Long) -> Long, aggregationPeriod: AggregationPeriod): List<AggregatedMetric> {
        val baseMetric = Metric(TABLE_ID, listOf(
                MetricColumn("source", MetricString("srv1")),
                MetricColumn("sourceGroup", MetricString("devHosts")),
                MetricColumn("type", MetricByte(MetricType.SUCCESS.id)),
                MetricColumn("duration", MetricLong(12)),
                MetricColumn("colFoo", MetricString("first")),
                MetricColumn("colBar", MetricString("second"))
        ), TIMESTAMP)
        val metrics = (0 until 10L).map { offset ->
            baseMetric.copy(
                    timestamp = TIMESTAMP - offsetProvider(offset))
        }.toList()
        clickhouseDao.insertMetrics(metrics)

        val aggMetrics = clickhouseDao.aggregateMetrics(aggregationPeriod, Query(null, Filter(emptyMap()), TABLE_ID))
        aggMetrics.size shouldBeEqualTo 10
        return aggMetrics
    }
}