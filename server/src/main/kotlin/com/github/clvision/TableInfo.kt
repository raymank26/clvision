package com.github.clvision

data class TableInfo(
        val name: String,
        val columns: Set<Column>
)

data class Column(val name: String)
