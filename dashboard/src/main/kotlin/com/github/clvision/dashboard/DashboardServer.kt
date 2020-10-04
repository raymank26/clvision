package com.github.clvision.dashboard

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.apibuilder.ApiBuilder.post

class DashboardServer(
        private val portNumber: Int,
        private val dashboardFacade: DashboardFacade
) {

    fun start() {
        val javalin = Javalin.create()
        javalin.routes {
            path("api") {
                post("login") { ctx ->
                    val username = ctx.formParam("username", String::class.java).value!!
                    val password = ctx.formParam("password", String::class.java).value!!
                    val user = dashboardFacade.login(username, password)
                    ctx.json(UserJson(user.id.toString(), user.username))
                }
            }
        }

        javalin.start(portNumber)
    }
}