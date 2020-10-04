package com.github.clvision

import com.github.clvision.common.VisionFacadeRule
import org.amshove.kluent.shouldBeGreaterThan
import org.junit.Rule
import org.junit.Test

class PermissionsTest {

    @JvmField
    @Rule
    val visionFacadeRule = VisionFacadeRule()

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
}