package com.github.clvision

import org.jdbi.v3.core.Jdbi

class JdbiProvider {
    fun provide(url: String): Jdbi {
        val jdbi = Jdbi.create(url)
        jdbi.registerArrayType(String::class.java, "string")
        return jdbi
    }
}