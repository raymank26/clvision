package com.github.clvision.dashboard

import com.github.clvision.dashboard.user.InMemoryUserService
import com.github.clvision.sdk.AggregationPeriod
import com.github.clvision.sdk.Filter
import com.github.clvision.sdk.Query
import org.junit.rules.ExternalResource
import java.time.LocalDate
import kotlin.properties.Delegates

const val TEAM_NAME = "foobar"

class VisionDashboardRule : ExternalResource() {

    val visionFacade = run {
        val userService = InMemoryUserService()
        DashboardFacade(InMemoryDashboardService(userService), userService)
    }

    var userId by Delegates.notNull<Long>()
    var teamId by Delegates.notNull<Long>()

    override fun before() {
        userId = visionFacade.createUser("none@none.ru", "123")
        teamId = visionFacade.createTeam(userId, TEAM_NAME)
    }

    fun getContainerId(dashboardBriefItem: DashboardBriefItem?): Long {
        return (dashboardBriefItem as DashboardBriefItem.Container).id
    }

    fun createEmptyFilter(tableId: Int): Query {
        return Query(AggregationPeriod(false, LocalDate.now(), listOf()), null, Filter(emptyMap()), tableId)
    }
}
