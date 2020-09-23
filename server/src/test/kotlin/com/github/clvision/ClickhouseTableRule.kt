package com.github.clvision

import org.jdbi.v3.core.Jdbi
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.testcontainers.containers.ClickHouseContainer
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId


const val TABLE_ID: Int = 1
const val TIMESTAMP = 1600648443732L

val QUERY_PERIOD = AggregationPeriod(
        groupByDay = false,
        days = listOf(LocalDate.ofInstant(Instant.ofEpochMilli(TIMESTAMP), ZoneId.systemDefault()))
)

class ClickHouseTableRule : TestRule {

    private val rule: TestRule
    private val jdbi: JdbiClickhouseRule

    val clickHouseDao: ClickhouseDao
        get() {
            return jdbi.clickhouseDao
        }

    init {
        val ch = ClickHouseContainer()
        jdbi = JdbiClickhouseRule(ch)
        rule = RuleChain.outerRule(ch)
                .around(jdbi)
    }

    override fun apply(base: Statement, description: Description): Statement {
        return rule.apply(base, description)
    }
}

private class JdbiClickhouseRule(private val ch: ClickHouseContainer): ExternalResource() {

    lateinit var clickhouseDao: ClickhouseDao
    lateinit var jdbi: Jdbi

    override fun before() {
        jdbi = JdbiProvider().provide(ch.jdbcUrl)
        clickhouseDao = ClickhouseDao(jdbi) { _ -> TableInfo(
                "Test",
                setOf("source", "sourceGroup", "colFoo", "colBar").mapTo(mutableSetOf(), { Column(it) }) as Set<Column>
        )}

        jdbi.useHandle<Exception> {
            it.execute("""CREATE TABLE Test(
                |date Date DEFAULT toDate(timeMinute),
                |timeMinute DateTime,
                |source String,
                |sourceGroup String,
                |type Int8,
                |duration Int64,
                |colFoo String,
                |colBar String
            ) ENGINE = MergeTree() ORDER BY (source, date)""".trimMargin())
        }
    }
}

