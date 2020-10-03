package com.github.clvision.user

class InMemoryUserService : UserService {
    private var userId = 0L
    private val users = mutableMapOf<Long, User>()

    private var teamId = 0L
    private val teams = mutableMapOf<Long, Team>()

    override fun createUser(email: String, password: String): Long {
        val newUserId = userId++
        users[newUserId] = User(newUserId, email, password)
        return newUserId
    }

    override fun createTeam(userId: Long, teamName: String): Long {
        val newTeamId = teamId ++
        teams[newTeamId] = Team(newTeamId, teamName, mutableListOf(userId))
        return newTeamId
    }

    override fun editAllowed(userId: Long, teamId: Long): Boolean {
        return teams[teamId]?.users?.contains(userId) ?: false
    }

    override fun showAllowed(userId: Long, teamId: Long): Boolean {
        return editAllowed(userId, teamId)
    }
}

data class Team(val id: Long, val name: String, val users: MutableList<Long>)

data class User(val id: Long, val email: String, val password: String)