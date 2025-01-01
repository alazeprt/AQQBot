package top.alazeprt.aqqbot.util

import top.alazeprt.aqqbot.AQQBot.dataSource
import top.alazeprt.aqqbot.AQQBot.table

object DBQuery {
    fun playerInDatabase(name: String): Boolean {
        return table.select(dataSource) {
            rows("userId")
            where("name" eq name)
            limit(1)
        }.firstOrNull { getString("userId") } != null
    }

    fun qqInDatabase(userId: Long): String? {
        return table.select(dataSource) {
            rows("name")
            where("userId" eq userId)
            limit(1)
        }.firstOrNull { getString("name") }
    }

    fun addPlayer(userId: Long, name: String) {
        table.insert(dataSource, "userId", "name") {
            value(userId, name)
        }
    }

    fun removePlayer(userId: Long, name: String) {
        table.delete(dataSource) {
            where(("userId" eq userId) and ("name" eq name))
        }
    }

    fun removePlayerByUserId(userId: Long) {
        table.delete(dataSource) {
            where("userId" eq userId)
        }
    }

    fun removePlayerByName(name: String) {
        table.delete(dataSource) {
            where("name" eq name)
        }
    }

    fun getUserIdByName(name: String): Long? {
        return table.select(dataSource) {
            rows("userId")
            where("name" eq name)
            limit(1)
        }.firstOrNull { getLong("userId") }
    }
}