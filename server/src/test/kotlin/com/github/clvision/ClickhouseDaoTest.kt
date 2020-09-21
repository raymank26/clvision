package com.github.clvision

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeLessThan
import org.jdbi.v3.core.Jdbi
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.testcontainers.containers.ClickHouseContainer
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs

private const val TABLE_ID: Int = 1
private const val TIMESTAMP = 1600648443732L

private val QUERY_PERIOD = AggregationPeriod(
        groupByDay = false,
        days = listOf(LocalDate.ofInstant(Instant.ofEpochMilli(TIMESTAMP), ZoneId.systemDefault()))
)

class ClickhouseDaoTest {

    @Rule
    @JvmField
    val ch = ClickHouseContainer()


    private lateinit var clickhouseDao: ClickhouseDao
    private lateinit var jdbi: Jdbi

    @Before
    fun before() {
        jdbi = JdbiProvider().provide(ch.jdbcUrl)
        clickhouseDao = ClickhouseDao(jdbi) { _ -> TableInfo(
                "Test",
                setOf("colFoo", "colBar").mapTo(mutableSetOf(), { Column(it) }) as Set<Column>
        )}

        jdbi.useHandle<Exception> {
            it.execute("""CREATE TABLE Test(
                |date Date DEFAULT toDate(timeMinute),
                |timeMinute DateTime,
                |source String,
                |sourceGroup String,
                |type Int8,
                |duration Int64,
                |colFoo String,
                |colBar String
            ) ENGINE = MergeTree() ORDER BY (source, date)""".trimMargin())
        }

        val baseMetric = Metric("srv1", "devHosts", TABLE_ID, MetricType.SUCCESS, 12, listOf("first", "second"),
                TIMESTAMP)

        clickhouseDao.insertMetrics(listOf(
                baseMetric,
                baseMetric.copy(parameters = listOf("third", "fourth"), timestamp = baseMetric.timestamp + 1)
        ))
    }

    @Test
    fun testSingleRowQuery() {
        val queriedMetrics = clickhouseDao.aggregateMetrics(
                QUERY_PERIOD,
                Query(null, Filter(mapOf("colFoo" to "%fi%"),
                        MetricType.SUCCESS, false),
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
                Query(null, Filter(emptyMap(),
                        MetricType.SUCCESS, false),
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
                Query(null, Filter(emptyMap(),
                        MetricType.SUCCESS, false),
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
                Query("colFoo", Filter(emptyMap(),
                        MetricType.SUCCESS, false),
                        TABLE_ID
                ))
        // TODO: write asserts
    }
}

infix fun Double.shouldBeAlmostEqualTo(other: Double) {
    Assert.assertTrue("Current value = '$this' doesn't equal to expected '$other'", abs(this - other) < other)
}