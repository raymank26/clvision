package com.github.clvision.dashboard

import feign.Feign
import feign.Logger
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.okhttp.OkHttpClient
import io.mockk.every
import io.mockk.mockk
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test

private const val PORT = 8923


class ServerTest {

    private lateinit var client: DashboardClient

    @Before
    fun before() {
        val dashboardFacade = mockk<DashboardFacade>(relaxed = true)
        every {
            dashboardFacade.login(eq("anton.ermak"), eq("footest"))
        } returns DashboardUser(123, "anton.ermak")
        val server = DashboardServer(PORT, dashboardFacade)
        server.start()
        client = Feign.builder()
                .client(OkHttpClient())
                .encoder(JacksonEncoder())
                .decoder(JacksonDecoder())
                .logLevel(Logger.Level.FULL)
                .target(DashboardClient::class.java, "http://localhost:$PORT/api")
    }

    @Test
    fun testLogin() {
        client.login("anton.ermak", "footest") shouldBeEqualTo UserJson(id = "123", username = "anton.ermak")
    }
}