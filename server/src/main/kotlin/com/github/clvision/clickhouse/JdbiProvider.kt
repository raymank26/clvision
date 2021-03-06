package com.github.clvision.clickhouse

import org.jdbi.v3.core.Jdbi

class JdbiProvider {
    fun provide(url: String): Jdbi {
        val jdbi = Jdbi.create(url)
        return jdbi
    }
}