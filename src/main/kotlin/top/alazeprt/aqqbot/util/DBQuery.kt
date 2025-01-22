package top.alazeprt.aqqbot.util

import top.alazeprt.aqqbot.AQQBot.dataSource
import top.alazeprt.aqqbot.AQQBot.table

object DBQuery {
    fun playerInDatabase(name: String): Boolean {
        return table.select(dataSource) {
            rows("name")
        }.map {
            getString("name").split(", ").toMutableList().contains(name)
        }.any { it }
    }

    fun qqInDatabase(userId: Long): MutableList<String> {
        return table.select(dataSource) {
            rows("name")
            where("userId" eq userId)
            limit(1)
        }.firstOrNull { getString("name").split(", ").toMutableList() }?: mutableListOf()
    }

    fun addPlayer(userId: Long, name: String) {
        var originList: MutableList<String> = mutableListOf();
        if (playerInDatabase(name)) {
            table.select(dataSource) {
                where("userId" eq userId)
                rows("name")
            }.map {
                originList = getString("name").split(", ").toMutableList()
            }
        }
        originList.add(name)
        table.insert(dataSource, "userId", "name") {
            value(userId, originList.joinToString(", "))
        }
    }

    fun removePlayer(userId: Long, name: String) {
        table.select(dataSource) {
            rows("name")
            where("userId" eq userId)
        }.map {
            if (getString("name").split(", ").toMutableList().contains(name)) {
                val newList = getString("name").split(", ").toMutableList()
                newList.remove(name)
                table.update(dataSource) {
                    where("userId" eq userId)
                    updateString("name", newList.joinToString(", "))
                }
                return@map
            }
        }
    }

    fun removePlayerByUserId(userId: Long) {
        table.delete(dataSource) {
            where("userId" eq userId)
        }
    }

    fun removePlayerByName(name: String) {
        table.select(dataSource) {
            rows("name")
            rows("userId")
        }.map {
            if (getString("name").split(", ").toMutableList().contains(name)) {
                table.delete(dataSource) {
                    where(("userId" eq getString("userId")) and ("name" eq getString("name")))
                }
                return@map
            }
        }
    }

    fun getUserIdByName(name: String): Long? {
        var userId: Long? = null
        table.select(dataSource) {
            rows("userId")
            rows("name")
        }.map {
            if (getString("name").split(", ").toMutableList().contains(name)) {
                userId = getLong("userId")
                return@map
            }
        }
        return userId
    }
}