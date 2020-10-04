package com.github.clvision.user

class InMemoryUserService : UserService {
    private var userId = 0L
    private val users = mutableMapOf<Long, User>()

    private var teamId = 0L
    private val teams = mutableMapOf<Long, Team>()

    private val userIdToTeamId = mutableMapOf<Long, MutableList<Long>>()

    override fun createUser(email: String, password: String): Long {
        val newUserId = userId++
        users[newUserId] = User(newUserId, email, password)
        return newUserId
    }

    override fun createTeam(userId: Long, teamName: String): Long {
        val newTeamId = teamId ++
        teams[newTeamId] = Team(newTeamId, teamName, mutableSetOf(userId))
        updateUserTeams(userId, newTeamId)
        return newTeamId
    }

    private fun updateUserTeams(userId: Long, newTeamId: Long) {
        userIdToTeamId.compute(userId) { _, col ->
            if (col == null) {
                mutableListOf(newTeamId)
            } else {
                col.add(newTeamId)
                col
            }
        }
    }

    override fun isEditAllowed(userId: Long, teamId: Long): Boolean {
        return teams[teamId]?.users?.contains(userId) ?: false
    }

    override fun isShowAllowed(userId: Long, teamId: Long): Boolean {
        return isEditAllowed(userId, teamId)
    }

    override fun joinTeam(teamId: Long, userId: Long) {
        teams[teamId]?.users?.add(userId) ?: error("No team found by id = $teamId")
        updateUserTeams(userId, teamId)
    }

    override fun listTeams(userId: Long): List<com.github.clvision.Team> {
        val teamIds = userIdToTeamId[userId] ?: emptyList()
        return teamIds.asSequence()
                .mapNotNull { teams[it] }
                .map { com.github.clvision.Team(it.id, it.name) }
                .toList()
    }
}

private data class Team(val id: Long, val name: String, val users: MutableSet<Long>)

private data class User(val id: Long, val email: String, val password: String)