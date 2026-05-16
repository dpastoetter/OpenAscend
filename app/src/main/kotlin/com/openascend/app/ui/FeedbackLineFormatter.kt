package com.openascend.app.ui

import com.openascend.domain.model.CoreStat

object FeedbackLineFormatter {
    fun seal(xp: Int, statLabel: String, pathLabel: String?): String {
        val path = pathLabel?.let { " · $it" }.orEmpty()
        return "+$xp XP · $statLabel$path"
    }

    fun quest(xp: Int, stat: CoreStat, pathLabel: String?): String =
        seal(xp, stat.name, pathLabel)

    fun habit(xp: Int, stat: CoreStat, pathLabel: String?): String =
        seal(xp, stat.name, pathLabel)

    fun checkIn(pathLabel: String?): String {
        val path = pathLabel?.let { " · $it" }.orEmpty()
        return "+12 XP · Chronicle sealed$path"
    }
}
