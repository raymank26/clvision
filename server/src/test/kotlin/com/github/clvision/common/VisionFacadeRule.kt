package com.github.clvision.common

import com.github.clvision.AggregatedMetric
import com.github.clvision.AggregationPeriod
import com.github.clvision.DashboardBriefItem
import com.github.clvision.Filter
import com.github.clvision.Query
import com.github.clvision.VisionFacade
import com.github.clvision.clickhouse.ClickhouseDao
import com.github.clvision.dashboard.InMemoryDashboardService
import com.github.clvision.user.InMemoryUserService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.amshove.kluent.called
import org.amshove.kluent.was
import org.junit.rules.ExternalResource
import java.time.LocalDate
import kotlin.properties.Delegates

const val TEAM_NAME = "foobar"

class VisionFacadeRule : ExternalResource() {

    private val clickhouseDao = mockk<ClickhouseDao>(relaxed = true)

    val visionFacade = run {
        val userService = InMemoryUserService()
        VisionFacade(InMemoryDashboardService(userService), userService, clickhouseDao)
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

    fun setupClickhouseAggregationAnswer(query: Query, result: List<AggregatedMetric>) {
        every { clickhouseDao.aggregateMetrics(eq(query)) } answers { result }
    }

    fun verifyClickhouseAddCalled() {
        verify { clickhouseDao.insertMetrics(any()) } was called
    }
}