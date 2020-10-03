package com.github.clvision

import com.github.clvision.dashboard.InMemoryDashboardService
import com.github.clvision.user.InMemoryUserService
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Before
import org.junit.Test
import kotlin.properties.Delegates

private const val TEAM_NAME = "foobar"

class DashboardsTest {

    private val visionFacade = run {
        val userService = InMemoryUserService()
        VisionFacade(InMemoryDashboardService(userService), userService)
    }
    private var userId by Delegates.notNull<Long>()
    private var teamId by Delegates.notNull<Long>()

    @Before
    fun setUp() {
        userId = visionFacade.createUser("none@none.ru", "123")
        teamId = visionFacade.createTeam(userId, TEAM_NAME)
    }

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

    private fun createEmptyFilter(tableId: Int): Query {
        return Query(null, Filter(emptyMap()), tableId)
    }

    private fun getContainerId(dashboardBriefItem: DashboardBriefItem?): Long {
        return (dashboardBriefItem as DashboardBriefItem.Container).id
    }
}