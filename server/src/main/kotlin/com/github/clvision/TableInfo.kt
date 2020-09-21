package com.github.clvision

data class TableInfo(
        val name: String,
        val colums: Set<Column>
)

data class Column(val name: String)
