package com.github.clvision

import org.junit.Rule
import org.junit.Test

class TableFieldsTest {

    @Rule
    @JvmField
    val ch = ClickHouseTableRule()

    @Test(expected = FieldNotFoundException::class)
    fun testNoInsertField() {
        ch.clickHouseDao.insertMetrics(listOf(Metric(TABLE_ID, listOf(MetricColumn("random", MetricString("123"))), 123L)))
    }

    @Test(expected = FieldNotFoundException::class)
    fun testNoAggField() {
        ch.clickHouseDao.insertMetrics(listOf(Metric(TABLE_ID, listOf(MetricColumn("colFoo", MetricString("123"))), 123L)))
        ch.clickHouseDao.aggregateMetrics(QUERY_PERIOD, Query(null, Filter(mapOf("random" to "123")), TABLE_ID))
    }
}