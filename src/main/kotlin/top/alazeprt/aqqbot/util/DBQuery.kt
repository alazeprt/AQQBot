package top.alazeprt.aqqbot.util

import top.alazeprt.aqqbot.AQQBot.dataSource
import top.alazeprt.aqqbot.AQQBot.table

object DBQuery {
    fun playerInDatabase(name: String): Boolean {
        return table.select(dataSource) {
            rows("name")
        }.map {
            if (getString("name").split(", ").toMutableList().isEmpty())
            getString("name") == name
            else getString("name").split(", ").toMutableList().contains(name)
        }.any { it }
    }

    fun qqInDatabase(userId: Long): MutableList<String> {
        return table.select(dataSource) {
            rows("name")
            where("userId" eq userId)
            limit(1)
        }.firstOrNull { if (getString("name").split(", ").toMutableList().isEmpty())
            mutableListOf(getString("name")) else getString("name").split(", ").toMutableList() }?: mutableListOf()
    }

    fun addPlayer(userId: Long, name: String) {
        var originList: MutableList<String> = mutableListOf();
        if (!qqInDatabase(userId).isEmpty()) {
            table.select(dataSource) {
                where("userId" eq userId)
                rows("name")
            }.map {
                originList = if (getString("name").split(", ").toMutableList().isEmpty())
            mutableListOf(getString("name")) else getString("name").split(", ").toMutableList()
            }
        }
        originList.add(name)
        if (originList.size == 1) {
            table.insert(dataSource, "userId", "name") {
                value(userId, originList.joinToString(", "))
            }
        } else {
            table.update(dataSource) {
                where("userId" eq userId)
                set("name", originList.joinToString(", "))
            }
        }
    }

    fun removePlayer(userId: Long, name: String) {
        table.select(dataSource) {
            rows("name")
            where("userId" eq userId)
        }.map {
            if (if (getString("name").split(", ").toMutableList().isEmpty())
            getString("name") == name else getString("name").split(", ").toMutableList().contains(name)) {
                val newList = if (getString("name").split(", ").toMutableList().isEmpty())
            mutableListOf(getString("name")) else getString("name").split(", ").toMutableList()
                newList.remove(name)
                if (newList.isEmpty()) {
                    table.delete(dataSource) {
                        where("userId" eq userId)
                    }
                } else {
                    table.update(dataSource) {
                        set("name", newList.joinToString(", "))
                        where("userId" eq userId)
                    }
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
            rows("name", "userId")
        }.map {
            if (if (getString("name").split(", ").toMutableList().isEmpty())
            getString("name") == name else getString("name").split(", ").toMutableList().contains(name)) {
                if (getString("name").split(", ").toMutableList().size == 1) {
                    table.delete(dataSource) {
                        where(("userId" eq getString("userId")) and ("name" eq getString("name")))
                    }
                } else {
                    table.update(dataSource) {
                        where("userId" eq getString("userId"))
                        set("name", getString("name").split(", ").toMutableList().filter { it != name })
                    }
                }
                return@map
            }
        }
    }

    fun getUserIdByName(name: String): Long? {
        var userId: Long? = null
        table.select(dataSource) {
            rows("userId", "name")
        }.map {
            if (if (getString("name").split(", ").toMutableList().isEmpty())
            getString("name") == name else getString("name").split(", ").toMutableList().contains(name)) {

                userId = getLong("userId")
                return@map
            }
        }
        return userId
    }
}