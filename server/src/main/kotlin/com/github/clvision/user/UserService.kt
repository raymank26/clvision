package com.github.clvision.user

interface UserService {
    fun createUser(email: String, password: String): Long
    fun createTeam(userId: Long, teamName: String): Long
    fun editAllowed(userId: Long, teamId: Long): Boolean
    fun showAllowed(userId: Long, teamId: Long): Boolean
    fun joinTeam(teamId: Long, userId: Long)
}
