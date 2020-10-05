package com.github.clvision.dashboard

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeGreaterThan
import org.junit.Rule
import org.junit.Test

class PermissionsTest {

    @JvmField
    @Rule
    val visionFacadeRule = VisionDashboardRule()

    private val userId by lazy { visionFacadeRule.userId }
    private val teamId by lazy { visionFacadeRule.teamId }
    private val visionFacade by lazy { visionFacadeRule.visionFacade }

    @Test
    fun testTeamJoining() {
        val dashboardId = visionFacadeRule.getContainerId(visionFacade.getDashboardBrief(userId, teamId, null))
        val secondUserId = visionFacade.createUser("second@gmail.com", "123")
        visionFacade.joinTeam(teamId, secondUserId)
        val secondDashboardId = visionFacade.createDashboard(secondUserId, teamId, dashboardId, "second dashboard")
        secondDashboardId shouldBeGreaterThan 0
    }

    @Test
    fun testTeamCollection() {
        val secondUserId = visionFacade.createUser("second@gmail.com", "123")
        val anotherTeamName = "Another name"
        val anotherTeamId = visionFacade.createTeam(secondUserId, anotherTeamName)
        visionFacade.joinTeam(teamId, secondUserId)
        visionFacade.listTeams(secondUserId).toSet() shouldBeEqualTo setOf(
                Team(teamId, TEAM_NAME),
                Team(anotherTeamId, anotherTeamName)
        )
    }
}