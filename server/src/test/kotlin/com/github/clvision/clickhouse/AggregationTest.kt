package com.github.clvision.clickhouse

import com.github.clvision.Filter
import com.github.clvision.GroupBy
import com.github.clvision.Metric
import com.github.clvision.MetricByte
import com.github.clvision.MetricColumn
import com.github.clvision.MetricLong
import com.github.clvision.MetricString
import com.github.clvision.MetricType
import com.github.clvision.Query
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeLessThan
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.math.abs

class AggregationTest {

    @Rule
    @JvmField
    val ch = ClickHouseTableRule()

    private lateinit var clickhouseDao: ClickhouseDao

    @Before
    fun before() {
        clickhouseDao = ch.clickHouseDao
        val baseMetric = Metric(TABLE_ID, listOf(
                MetricColumn("source", MetricString("srv1")),
                MetricColumn("sourceGroup", MetricString("devHosts")),
                MetricColumn("type", MetricByte(MetricType.SUCCESS.id)),
                MetricColumn("duration", MetricLong(12)),
                MetricColumn("colFoo", MetricString("first")),
                MetricColumn("colBar", MetricString("second"))
        ), TIMESTAMP)

        clickhouseDao.insertMetrics(listOf(
                baseMetric,
                baseMetric.withMetrics(listOf(
                        MetricColumn("source", MetricString("srv2")),
                        MetricColumn("colFoo", MetricString("third")),
                        MetricColumn("colBar", MetricString("fourth")),
                )).copy(timestamp = baseMetric.timestamp + 1)
        ))
    }

    @Test
    fun testSingleRowQuery() {
        val queriedMetrics = clickhouseDao.aggregateMetrics(
                QUERY_PERIOD,
                Query(null, Filter(mapOf("colFoo" to "%fi%")),
                        TABLE_ID
                )
        )
        queriedMetrics.size shouldBeEqualTo 1
        val aggMetric = queriedMetrics.first()
        aggMetric.name shouldBeEqualTo "value"
        aggMetric.time shouldBeEqualTo "2020-09-21 00:34:00"
        abs(1.0 - aggMetric.value) shouldBeLessThan 0.00001
    }

    @Test
    fun testTwoRowsQuery() {
        val queriedMetrics = clickhouseDao.aggregateMetrics(
                QUERY_PERIOD,
                Query(null, Filter(emptyMap()),
                        TABLE_ID
                )
        )
        queriedMetrics.size shouldBeEqualTo 1
        val aggMetric = queriedMetrics.first()
        aggMetric.name shouldBeEqualTo "value"
        aggMetric.time shouldBeEqualTo "2020-09-21 00:34:00"
        aggMetric.value shouldBeAlmostEqualTo 2.0
    }

    @Test
    fun testAggregationByDays() {
        val queriedMetrics = clickhouseDao.aggregateMetrics(
                QUERY_PERIOD.copy(groupByDay = true),
                Query(null, Filter(emptyMap()),
                        TABLE_ID
                )
        )
        queriedMetrics.size shouldBeEqualTo 1
        val aggMetric = queriedMetrics.first()
        aggMetric.name shouldBeEqualTo "value"
        aggMetric.time shouldBeEqualTo "2020-09-21"
        aggMetric.value shouldBeAlmostEqualTo 2.0
    }

    @Test
    fun testAggregationWithGroupBy() {
        val metrics = clickhouseDao.aggregateMetrics(QUERY_PERIOD,
                Query(GroupBy("colFoo", "count"), Filter(emptyMap()),
                        TABLE_ID
                ))
        // TODO: write asserts
    }

    @Test
    fun testGroupBySource() {
        val metrics = clickhouseDao.aggregateMetrics(QUERY_PERIOD,
                Query(GroupBy("source", "count"), Filter(emptyMap()), TABLE_ID))
        metrics.size shouldBeEqualTo 2
        metrics[0].name shouldBeEqualTo "srv1"
        metrics[1].name shouldBeEqualTo "srv2"
    }
}

infix fun Double.shouldBeAlmostEqualTo(other: Double) {
    Assert.assertTrue("Current value = '$this' doesn't equal to expected '$other'", abs(this - other) < other)
}

fun Metric.withMetrics(list: List<MetricColumn>): Metric {
    val excludeNames = list.map { it.name }.toSet()
    val temp = this.parameters.filter { !excludeNames.contains(it.name) }
    return this.copy(parameters = temp + list)
}