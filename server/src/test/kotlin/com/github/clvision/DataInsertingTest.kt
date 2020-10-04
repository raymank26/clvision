package com.github.clvision

import com.github.clvision.common.VisionFacadeRule
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

private const val TABLE_ID = 124

class DataInsertingTest {

    @JvmField
    @Rule
    val visionFacadeRule = VisionFacadeRule()

    private val userId by lazy { visionFacadeRule.userId }
    private val teamId by lazy { visionFacadeRule.teamId }
    private val visionFacade by lazy { visionFacadeRule.visionFacade }

    @Test
    fun updateChartData() {
        val teamDashboardId = visionFacadeRule.getContainerId(visionFacade.getDashboardBrief(userId, teamId, null))
        val chartId = visionFacade.createChart(userId, teamId, "Chart", visionFacadeRule.createEmptyFilter(TABLE_ID),
                teamDashboardId)
        val query = emptyChartQuery()
        val expectedReturn = listOf(AggregatedMetric("foo", "bar", 1.0))
        visionFacadeRule.setupClickhouseAggregationAnswer(Query(query.aggregationPeriod, query.groupBy, query.filter, tableId = TABLE_ID),
                expectedReturn)

        visionFacade.addMetrics(listOf(Metric(TABLE_ID, listOf(MetricColumn("foobar", MetricString("baz"))), 2389L)))
        val chartData = visionFacade.getChartData(chartId, query)
        chartData shouldBeEqualTo expectedReturn
        visionFacadeRule.verifyClickhouseAddCalled()
    }

    private fun emptyChartQuery() =
            ChartQuery(null, Filter(emptyMap()), AggregationPeriod(false, LocalDate.now(), emptyList()))
}