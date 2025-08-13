package top.alazeprt.aqqbot.data;

import taboolib.module.database.Host
import taboolib.module.database.Table
import top.alazeprt.aqqbot.AQQBot;
import top.alazeprt.aqqbot.profile.AOfflinePlayer
import javax.sql.DataSource

abstract class DatabaseDataProvider(val plugin: AQQBot): DataProvider {

    abstract var host: Host<*>

    abstract var table: Table<*, *>

    abstract var dataSource: DataSource

    override fun hasPlayer(player: AOfflinePlayer): Boolean {
        return table.select(dataSource) {
            rows("name")
        }.map {
            if (getString("name").split(", ").toMutableList().isEmpty())
                getString("name") == player.getName()
            else getString("name").split(", ").toMutableList().contains(player.getName())
        }.any { it }
    }

    override fun hasPlayer(name: String): Boolean {
        return table.select(dataSource) {
            rows("name")
        }.map {
            if (getString("name").split(", ").toMutableList().isEmpty())
                getString("name") == name
            else getString("name").split(", ").toMutableList().contains(name)
        }.any { it }
    }

    override fun hasQQ(qq: Long): Boolean {
        return table.select(dataSource) {
            rows("name")
            where("userId" eq qq)
            limit(1)
        }.firstOrNull { getString("name") } != null
    }

    override fun addPlayer(qq: Long, player: AOfflinePlayer) {
        plugin.debugModule?.debugLogger?.log("database: add player $qq -> ${player.getName()}")
        var originList: MutableList<String> = mutableListOf();
        if (hasQQ(qq)) {
            table.select(dataSource) {
                where("userId" eq qq)
                rows("name")
            }.map {
                plugin.debugModule?.debugLogger?.log("database: get origin data for $qq: ${getString("name")}")
                originList = if (getString("name").split(", ").toMutableList().isEmpty())
                    mutableListOf(getString("name")) else getString("name").split(", ").toMutableList()
            }
        }
        originList.add(player.getName())
        plugin.debugModule?.debugLogger?.log("database: new data for $qq: ${originList.joinToString(", ")}")
        if (originList.size == 1) {
            table.insert(dataSource, "userId", "name") {
                value(qq, originList.joinToString(", "))
            }
        } else {
            table.update(dataSource) {
                where("userId" eq qq)
                set("name", originList.joinToString(", "))
            }
        }
    }

    override fun addPlayer(qq: Long, name: String) {
        plugin.debugModule?.debugLogger?.log("database: add player $qq -> $name")
        var originList: MutableList<String> = mutableListOf();
        if (hasQQ(qq)) {
            table.select(dataSource) {
                where("userId" eq qq)
                rows("name")
            }.map {
                plugin.debugModule?.debugLogger?.log("database: get origin data for $qq: ${getString("name")}")
                originList = if (getString("name").split(", ").toMutableList().isEmpty())
                    mutableListOf(getString("name")) else getString("name").split(", ").toMutableList()
            }
        }
        originList.add(name)
        plugin.debugModule?.debugLogger?.log("database: new data for $qq: ${originList.joinToString(", ")}")
        if (originList.size == 1) {
            table.insert(dataSource, "userId", "name") {
                value(qq, originList.joinToString(", "))
            }
        } else {
            table.update(dataSource) {
                where("userId" eq qq)
                set("name", originList.joinToString(", "))
            }
        }
    }

    override fun removePlayer(qq: Long) {
        plugin.debugModule?.debugLogger?.log("database: remove all player for $qq")
        table.delete(dataSource) {
            where("userId" eq qq)
        }
    }

    override fun removePlayer(player: AOfflinePlayer) {
        plugin.debugModule?.debugLogger?.log("database: remove player $player")
        val userId = ""
        val list = mutableListOf<String>()
        table.select(dataSource) {
            rows("userId", "name")
        }.map {
            if (if (getString("name").split(", ").toMutableList().isEmpty())
                    getString("name") == player.getName()
                else getString("name").split(", ").toMutableList().contains(player.getName())) {
                plugin.debugModule?.debugLogger?.log("database: get data in ${getString("userId")}: ${getString("name")}")
                val newList = if (getString("name").split(", ").toMutableList().isEmpty())
                    mutableListOf(getString("name")) else getString("name").split(", ").toMutableList()
                newList.remove(player.getName())
                list.addAll(newList)
            }
        }
        plugin.debugModule?.debugLogger?.log("database: new data for $userId: ${list.joinToString(", ")}")
        if (list.joinToString(", ").isBlank()) {
            plugin.debugModule?.debugLogger?.log("database: remove all player for $userId")
            table.delete(dataSource) {
                where("userId" eq userId)
            }
            return
        }
        plugin.debugModule?.debugLogger?.log("database: update data for $userId: ${list.joinToString(", ")}")
        table.update(dataSource) {
            set("name", list.joinToString(", "))
            where("userId" eq userId)
        }
    }

    override fun removePlayer(name: String) {
        plugin.debugModule?.debugLogger?.log("database: remove player $name")
        val userId = ""
        val list = mutableListOf<String>()
        table.select(dataSource) {
            rows("userId", "name")
        }.map {
            if (if (getString("name").split(", ").toMutableList().isEmpty())
                    getString("name") == name
                else getString("name").split(", ").toMutableList().contains(name)) {
                plugin.debugModule?.debugLogger?.log("database: get data in ${getString("userId")}: ${getString("name")}")
                val newList = if (getString("name").split(", ").toMutableList().isEmpty())
                    mutableListOf(getString("name")) else getString("name").split(", ").toMutableList()
                newList.remove(name)
                list.addAll(newList)
            }
        }
        plugin.debugModule?.debugLogger?.log("database: new data for $userId: ${list.joinToString(", ")}")
        if (list.joinToString(", ").isBlank()) {
            plugin.debugModule?.debugLogger?.log("database: remove all player for $userId")
            table.delete(dataSource) {
                where("userId" eq userId)
            }
            return
        }
        plugin.debugModule?.debugLogger?.log("database: update data for $userId: ${list.joinToString(", ")}")
        table.update(dataSource) {
            set("name", list.joinToString(", "))
            where("userId" eq userId)
        }
    }

    override fun removePlayer(qq: Long, name: String) {
        plugin.debugModule?.debugLogger?.log("database: remove player $qq")
        val list = mutableListOf<String>()
        table.select(dataSource) {
            rows("name")
            where("userId" eq qq)
        }.map {
            if (if (getString("name").split(", ").toMutableList().isEmpty())
                    getString("name") == name
                else getString("name").split(", ").toMutableList().contains(name)) {
                plugin.debugModule?.debugLogger?.log("database: get data in $qq: ${getString("name")}")
                val newList = if (getString("name").split(", ").toMutableList().isEmpty())
                    mutableListOf(getString("name")) else getString("name").split(", ").toMutableList()
                newList.remove(name)
                list.addAll(newList)
            }
        }
        plugin.debugModule?.debugLogger?.log("database: new data for $qq: ${list.joinToString(", ")}")
        if (list.joinToString(", ").isBlank()) {
            plugin.debugModule?.debugLogger?.log("database: remove all player for $qq")
            table.delete(dataSource) {
                where("userId" eq qq)
            }
            return
        }
        plugin.debugModule?.debugLogger?.log("database: update data for $qq: ${list.joinToString(", ")}")
        table.update(dataSource) {
            set("name", list.joinToString(", "))
            where("userId" eq qq)
        }
    }

    override fun removePlayer(qq: Long, player: AOfflinePlayer) {
        plugin.debugModule?.debugLogger?.log("database: remove player $qq")
        val list = mutableListOf<String>()
        table.select(dataSource) {
            rows("name")
            where("userId" eq qq)
        }.map {
            if (if (getString("name").split(", ").toMutableList().isEmpty())
                    getString("name") == player.getName()
                else getString("name").split(", ").toMutableList().contains(player.getName())) {
                plugin.debugModule?.debugLogger?.log("database: get data in $qq: ${getString("name")}")
                val newList = if (getString("name").split(", ").toMutableList().isEmpty())
                    mutableListOf(getString("name")) else getString("name").split(", ").toMutableList()
                newList.remove(player.getName())
                list.addAll(newList)
            }
        }
        plugin.debugModule?.debugLogger?.log("database: new data for $qq: ${list.joinToString(", ")}")
        if (list.joinToString(", ").isBlank()) {
            plugin.debugModule?.debugLogger?.log("database: remove all player for $qq")
            table.delete(dataSource) {
                where("userId" eq qq)
            }
            return
        }
        plugin.debugModule?.debugLogger?.log("database: update data for $qq: ${list.joinToString(", ")}")
        table.update(dataSource) {
            set("name", list.joinToString(", "))
            where("userId" eq qq)
        }
    }

    override fun getQQByPlayer(player: AOfflinePlayer): Long? {
        var userId: Long? = null
        table.select(dataSource) {
            rows("userId", "name")
        }.map {
            if (if (getString("name").split(", ").toMutableList().isEmpty())
                    getString("name") == player.getName() else getString("name").split(", ").toMutableList().contains(player.getName())) {

                userId = getLong("userId")
                return@map
            }
        }
        return userId
    }

    override fun getQQByPlayer(name: String): Long? {
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

    override fun getPlayerByQQ(qq: Long): List<AOfflinePlayer> {
        val list = table.select(dataSource) {
            rows("name")
            where("userId" eq qq)
            limit(1)
        }.firstOrNull { if (getString("name").split(", ").toMutableList().isEmpty())
            mutableListOf(getString("name")) else getString("name").split(", ").toMutableList() }?: mutableListOf()
        return list.map { plugin.adapter!!.getOfflinePlayer(it) }
    }

    override fun getPlayerNameByQQ(qq: Long): List<String> {
        val list = table.select(dataSource) {
            rows("name")
            where("userId" eq qq)
            limit(1)
        }.firstOrNull { if (getString("name").split(", ").toMutableList().isEmpty())
            mutableListOf(getString("name")) else getString("name").split(", ").toMutableList() }?: mutableListOf()
        return list
    }

    override fun getAllData(): Map<Long, List<AOfflinePlayer>> {
        val map = mutableMapOf<Long, List<AOfflinePlayer>>()
        table.select(dataSource) {
            rows("userId", "name")
        }.map {
            map[getString("userId").toLong()] = getString("name").split(", ").toMutableList()
                .filter { it.isNotBlank() }
                .map { plugin.adapter.getOfflinePlayer(it) }
        }
        return map
    }

}
