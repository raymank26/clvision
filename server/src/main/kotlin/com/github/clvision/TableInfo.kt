package com.github.clvision

private val SPECIAL_COLUMNS = setOf("source", "sourceGroup", "type", "duration", "timeMinute")

data class TableInfo(
        val name: String,
        val columns: Set<Column>
) {
    val userDefinedColumnNames = columns.filter { !SPECIAL_COLUMNS.contains(it.name) }.map { it.name }
}

data class Column(val name: String)
