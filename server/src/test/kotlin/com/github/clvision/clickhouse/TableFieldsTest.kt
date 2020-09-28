package com.github.clvision.clickhouse

import com.github.clvision.Filter
import com.github.clvision.Metric
import com.github.clvision.MetricColumn
import com.github.clvision.MetricString
import com.github.clvision.Query
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Rule
import org.junit.Test

class TableFieldsTest {

    @Rule
    @JvmField
    val ch = ClickHouseTableRule()

    @Test(expected = FieldNotFoundException::class)
    fun testNoAggField() {
        ch.clickHouseDao.insertMetrics(listOf(Metric(TABLE_ID, listOf(
                MetricColumn("colFoo", MetricString("abc")),
                MetricColumn("colBar", MetricString("second"))

        ), 123L)))
        ch.clickHouseDao.aggregateMetrics(QUERY_PERIOD, Query(null, Filter(mapOf("random" to "123")), TABLE_ID))
    }

    @Test
    fun testNullableViolation() {
        ch.clickHouseDao.insertMetrics(listOf(Metric(TABLE_ID, listOf(
                MetricColumn("colBar", MetricString("second"))

        ), 123L)))
        checkNoEntries()
    }

    private fun checkNoEntries() {
        val res = ch.clickHouseDao.aggregateMetrics(QUERY_PERIOD, Query(null, Filter(emptyMap()), TABLE_ID))
        res.size shouldBeEqualTo 0
    }
}