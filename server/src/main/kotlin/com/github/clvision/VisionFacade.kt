package com.github.clvision

import com.github.clvision.clickhouse.ClickhouseDao
import com.github.clvision.dashboard.DashboardService
import com.github.clvision.user.UserService

class VisionFacade(
        private val dashboardService: DashboardService,
        private val userService: UserService,
        private val clickhouseDao: ClickhouseDao
) {

    fun createUser(email: String, password: String): Long {
        return userService.createUser(email, password)
    }

    fun createTeam(userId: Long, teamName: String): Long {
        val teamId = userService.createTeam(userId, teamName)
        createDashboard(userId, teamId, null, teamName)
        return teamId
    }

    fun addMetrics(metrics: List<Metric>) {
        clickhouseDao.insertMetrics(metrics)
    }

    fun createDashboard(userId: Long, teamId: Long, parentId: DashboardId?, name: String): DashboardId {
        return dashboardService.createDashboard(userId, teamId, parentId, name)
    }

    fun createChart(userId: Long, teamId: Long, name: String, query: Query, dashboardId: DashboardId): Long {
        return dashboardService.createChart(userId, teamId, dashboardId, name, query)
    }

    fun getDashboardBrief(userId: Long, teamId: Long, id: DashboardId?): DashboardBriefItem? {
        return dashboardService.getDashboardBrief(userId, teamId, id)
    }

    fun getChartData(chartId: ChartId, query: ChartQuery?): List<AggregatedMetric> {
        val chartBrief = dashboardService.getChartBrief(chartId) ?: error("No chart for id = $chartId")

        val preparedQuery = if (query != null) {
            Query(query.aggregationPeriod, query.groupBy, query.filter, chartBrief.query.tableId)
        } else {
            chartBrief.query
        }
        return clickhouseDao.aggregateMetrics(preparedQuery)
    }

    fun updateChart(userId: Long, teamId: Long, chartId: ChartId, query: Query) {
        return dashboardService.updateChart(userId, teamId, chartId, query)
    }
}

typealias DashboardId = Long
typealias ChartId = Long
typealias TableId = Long

sealed class DashboardBriefItem {
    data class Container(val id: DashboardId, val name: String, val children: List<DashboardBriefItem>): DashboardBriefItem()
    data class Chart(val id: ChartId, val name: String, val query: Query): DashboardBriefItem()
}

data class ChartQuery(val groupBy: GroupBy?, val filter: Filter, val aggregationPeriod: AggregationPeriod)