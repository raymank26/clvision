package com.github.clvision

data class TableInfo(
        val name: String,
        val columns: Set<Column>
) {
    val allowedColumnNames = columns.map { it.name }
}

data class Column(val name: String, val type: Int, val nullable: Boolean)
