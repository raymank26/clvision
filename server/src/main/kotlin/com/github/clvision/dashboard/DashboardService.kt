package com.github.clvision.dashboard

import com.github.clvision.ChartId
import com.github.clvision.DashboardBriefItem
import com.github.clvision.DashboardId
import com.github.clvision.Query

interface DashboardService {
    fun createDashboard(userId: Long, teamId: Long, parentId: DashboardId?, name: String): DashboardId
    fun getDashboardBrief(userId: Long, teamId: Long, id: DashboardId?): DashboardBriefItem?
    fun getChartBrief(chartId: Long): DashboardBriefItem.Chart?
    fun createChart(userId: Long, teamId: Long, dashboardId: DashboardId, name: String, query: Query): Long
    fun updateChart(userId: Long, teamId: Long, chartId: ChartId, query: Query)
}