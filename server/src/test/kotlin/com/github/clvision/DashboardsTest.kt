package com.github.clvision

import com.github.clvision.common.TEAM_NAME
import com.github.clvision.common.VisionFacadeRule
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Rule
import org.junit.Test

class DashboardsTest {

    @JvmField
    @Rule
    val visionFacadeRule = VisionFacadeRule()

    private val userId by lazy { visionFacadeRule.userId }
    private val teamId by lazy { visionFacadeRule.teamId }
    private val visionFacade by lazy { visionFacadeRule.visionFacade }

    @Test
    fun createDashboard() {
        val teamDashboard = visionFacade.getDashboardBrief(userId, teamId, null)
        teamDashboard shouldBeInstanceOf DashboardBriefItem.Container::class
    }

    @Test
    fun createDashboardHierarchy() {
        val teamDashboardId = getContainerId(visionFacade.getDashboardBrief(userId, teamId, null))

        val firstLevelFirstItemId = visionFacade.createDashboard(userId, teamId, teamDashboardId, "First level(1)")
        val chartQuery = createEmptyFilter(123)
        val chartId = visionFacade.createChart(userId, teamId, "Simple chart", chartQuery, firstLevelFirstItemId)

        val firstLevelSecondItemId = visionFacade.createDashboard(userId, teamId, teamDashboardId, "First level(2)")
        val secondLevelFirstItemId = visionFacade.createDashboard(userId, teamId, firstLevelFirstItemId, "Second level")
        visionFacade.getDashboardBrief(userId, teamId, null) shouldBeEqualTo
                DashboardBriefItem.Container(
                        teamDashboardId, TEAM_NAME, listOf(
                        DashboardBriefItem.Container(firstLevelFirstItemId, "First level(1)", listOf(
                                DashboardBriefItem.Chart(chartId, "Simple chart", chartQuery),
                                DashboardBriefItem.Container(secondLevelFirstItemId, "Second level", listOf())
                        )),
                        DashboardBriefItem.Container(firstLevelSecondItemId, "First level(2)", listOf()))
                )
    }

    @Test
    fun updateChartData() {
        val teamDashboardId = getContainerId(visionFacade.getDashboardBrief(userId, teamId, null))
        val chartId = visionFacade.createChart(userId, teamId, "Chart", createEmptyFilter(123), teamDashboardId)
        val newQuery = createEmptyFilter(124)
        visionFacade.updateChart(userId, teamId, chartId, newQuery)

        val dashboard = visionFacade.getDashboardBrief(userId, teamId, null)
        dashboard shouldBeEqualTo
                DashboardBriefItem.Container(
                        teamDashboardId, TEAM_NAME, listOf(
                        DashboardBriefItem.Chart(chartId, "Chart", newQuery))
                )
    }

    private fun createEmptyFilter(tableId: Int): Query {
        return visionFacadeRule.createEmptyFilter(tableId)
    }

    private fun getContainerId(dashboardBriefItem: DashboardBriefItem?): Long {
        return visionFacadeRule.getContainerId(dashboardBriefItem)
    }
}