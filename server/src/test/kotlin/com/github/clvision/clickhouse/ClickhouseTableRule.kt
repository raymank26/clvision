package com.github.clvision.clickhouse

import com.github.clvision.AggregationPeriod
import com.github.clvision.Column
import com.github.clvision.TableInfo
import org.jdbi.v3.core.Jdbi
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.testcontainers.containers.ClickHouseContainer
import java.sql.Types
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId


const val TABLE_ID: Int = 1
const val TIMESTAMP = 1600648443732L

val QUERY_PERIOD = AggregationPeriod(
        groupByDay = false,
        date = LocalDate.ofInstant(Instant.ofEpochMilli(TIMESTAMP), ZoneId.systemDefault()),
        offsetsBefore = emptyList()
)

class ClickHouseTableRule : TestRule {

    private val rule: TestRule
    private val jdbi: JdbiClickhouseRule

    val clickHouseDao: ClickhouseDao
        get() {
            return jdbi.clickhouseDao
        }

    init {
        val ch = ClickHouseContainer("yandex/clickhouse-server:20.9.2.20")
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
        clickhouseDao = ClickhouseDao(jdbi) {
            TableInfo(
                "Test",
                setOf(
                        Column("source", Types.VARCHAR, true),
                        Column("sourceGroup" , Types.VARCHAR, true),
                        Column("type", Types.SMALLINT, true),
                        Column("duration", Types.BIGINT, true),
                        Column("colFoo", Types.VARCHAR, false),
                        Column("colBar", Types.VARCHAR, true)
                )
        )}

        jdbi.useHandle<Exception> {
            it.execute("""CREATE TABLE Test(
                |date Date DEFAULT toDate(timeMinute),
                |timeMinute DateTime,
                |source Nullable(String),
                |sourceGroup Nullable(String),
                |type Nullable(Int8),
                |duration Nullable(Int64),
                |colFoo String,
                |colBar Nullable(String)
            ) ENGINE = MergeTree() ORDER BY (date)""".trimMargin())
        }
    }
}

