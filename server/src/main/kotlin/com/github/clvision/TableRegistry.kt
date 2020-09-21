package com.github.clvision

fun interface TableRegistry {
    fun getNameById(tableId: Int): TableInfo?
}