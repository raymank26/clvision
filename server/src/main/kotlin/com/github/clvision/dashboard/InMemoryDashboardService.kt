package com.github.clvision.dashboard

import com.github.clvision.ChartId
import com.github.clvision.DashboardBriefItem
import com.github.clvision.DashboardId
import com.github.clvision.Query
import com.github.clvision.user.UserService

class InMemoryDashboardService(private val userService: UserService) : DashboardService {

    private var dashboardId = 0L
    private val teamToDashboardId = mutableMapOf<Long, Long>()
    private val dashboards = mutableMapOf<Long, Dashboard>()
    private val childrenItems = mutableMapOf<Long, MutableList<Target>>()

    private var chartId = 0L
    private val charts = mutableMapOf<Long, Chart>()

    override fun createDashboard(userId: Long, teamId: Long, parentId: DashboardId?, name: String): DashboardId {
        checkPermission(userId, teamId)
        if (parentId == null && teamToDashboardId[teamId] != null) {
            error("Team already has a dashboard")
        }
        val newDashboardId = dashboardId++
        dashboards[newDashboardId] = Dashboard(newDashboardId, name, teamId, parentId)
        if (parentId == null) {
            teamToDashboardId[teamId] = newDashboardId
        } else {
            val newTarget = Target(IdType.DASHBOARD, newDashboardId)
            addTarget(parentId, newTarget)
        }
        return newDashboardId
    }

    private fun addTarget(parentId: DashboardId, newTarget: Target) {
        childrenItems.compute(parentId) { _, prev: MutableList<Target>? ->
            if (prev == null) {
                mutableListOf(newTarget)
            } else {
                prev.add(newTarget)
                prev
            }
        }
    }

    private fun checkPermission(userId: Long, teamId: Long) {
        if (!userService.isEditAllowed(userId, teamId)) {
            throw PermissionDenied("Unable to create dashboard by userId = $userId in teamId = $teamId")
        }
    }

    override fun createChart(userId: Long, teamId: Long, dashboardId: DashboardId, name: String, query: Query): Long {
        checkPermission(userId, teamId)
        dashboards[dashboardId] ?: error("No dashboard found by id = $dashboardId")
        val newChartId = chartId ++
        charts[newChartId] = Chart(newChartId, name, query)

        val newTarget = Target(IdType.CHART, newChartId)
        addTarget(dashboardId, newTarget)

        return newChartId
    }

    override fun updateChart(userId: Long, teamId: Long, chartId: ChartId, query: Query) {
        checkPermission(userId, teamId)
        val chart = charts[chartId] ?: error("No chart found, id = $chartId")
        charts[chartId] = chart.copy(query = query)
    }

    override fun getDashboardBrief(userId: Long, teamId: Long, id: DashboardId?): DashboardBriefItem? {
        if (!userService.isShowAllowed(userId, teamId)) {
            throw PermissionDenied("Unable to show dashboard for userId = $userId in teamId = $teamId")
        }
        val dashboardId = id ?: (teamToDashboardId[teamId] ?: error("No dashboard created for teamId = $teamId"))
        return buildItemTree(IdType.DASHBOARD, dashboardId)
    }

    override fun getChartBrief(chartId: Long): DashboardBriefItem.Chart? {
        val chart = charts[chartId] ?: return null
        return DashboardBriefItem.Chart(chartId, chart.name, chart.query)
    }

    private fun buildItemTree(idType: IdType, id: Long): DashboardBriefItem? {
        return when (idType) {
            IdType.DASHBOARD -> {
                val dashboard = dashboards[id]
                if (dashboard != null) {
                    val childrenIds = childrenItems[id] ?: emptyList()
                    DashboardBriefItem.Container(id, dashboard.name, (childrenIds).mapNotNull { buildItemTree(it.idType, it.id) })
                } else {
                    null
                }
            }
            IdType.CHART -> {
                val chart = charts[id]
                return if (chart != null) {
                    DashboardBriefItem.Chart(chart.id, chart.name, chart.query)
                } else {
                    null
                }
            }
        }
    }
}


private data class Dashboard(val id: Long, val name: String, val teamId: Long, val parentDashboardId: Long?)

private data class Chart(val id: Long, val name: String, val query: Query)

private data class Target(val idType: IdType, val id: Long)

private enum class IdType {
    DASHBOARD,
    CHART
}
