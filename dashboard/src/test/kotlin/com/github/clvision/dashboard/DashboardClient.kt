package com.github.clvision.dashboard

import feign.Body
import feign.Param
import feign.RequestLine

interface DashboardClient {
    @RequestLine("POST /login")
    @Body("username={username}&password={password}")
    fun login(@Param("username") username: String, @Param("password") password: String): UserJson
}