package com.github.clvision.dashboard

import com.github.clvision.dashboard.user.UserService
import com.github.clvision.sdk.AggregationPeriod
import com.github.clvision.sdk.Filter
import com.github.clvision.sdk.GroupBy
import com.github.clvision.sdk.Query

class DashboardFacade(
        private val dashboardService: DashboardService,
        private val userService: UserService

) {
    fun login(username: String, password: String): DashboardUser {
        TODO()
    }

    fun createUser(email: String, password: String): Long {
        return userService.createUser(email, password)
    }

    fun createTeam(userId: Long, teamName: String): Long {
        val teamId = userService.createTeam(userId, teamName)
        createDashboard(userId, teamId, null, teamName)
        return teamId
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

    fun updateChart(userId: Long, teamId: Long, chartId: ChartId, query: Query) {
        return dashboardService.updateChart(userId, teamId, chartId, query)
    }

    fun joinTeam(teamId: Long, userId: Long) {
        return userService.joinTeam(teamId, userId)
    }

    fun listTeams(userId: Long): List<Team> {
        return userService.listTeams(userId)
    }
}

typealias DashboardId = Long
typealias ChartId = Long
typealias TableId = Long

sealed class DashboardBriefItem {
    data class Container(val id: DashboardId, val name: String, val children: List<DashboardBriefItem>): DashboardBriefItem()
    data class Chart(val id: ChartId, val name: String, val query: Query): DashboardBriefItem()
}

data class Team(val id: Long, val name: String)

data class ChartQuery(val groupBy: GroupBy?, val filter: Filter, val aggregationPeriod: AggregationPeriod)