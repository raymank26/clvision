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
        teams[newTeamId] = Team(newTeamId, teamName, mutableSetOf(userId))
        return newTeamId
    }

    override fun isEditAllowed(userId: Long, teamId: Long): Boolean {
        return teams[teamId]?.users?.contains(userId) ?: false
    }

    override fun isShowAllowed(userId: Long, teamId: Long): Boolean {
        return isEditAllowed(userId, teamId)
    }

    override fun joinTeam(teamId: Long, userId: Long) {
        teams[teamId]?.users?.add(userId) ?: error("No team found by id = $teamId")
    }
}

data class Team(val id: Long, val name: String, val users: MutableSet<Long>)

data class User(val id: Long, val email: String, val password: String)