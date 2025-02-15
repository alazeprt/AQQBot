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

    override fun hasQQ(qq: Long): Boolean {
        return table.select(dataSource) {
            rows("name")
            where("userId" eq qq)
            limit(1)
        }.firstOrNull { getString("name") } != null
    }

    override fun addPlayer(qq: Long, player: AOfflinePlayer) {
        var originList: MutableList<String> = mutableListOf();
        if (hasQQ(qq)) {
            table.select(dataSource) {
                where("userId" eq qq)
                rows("name")
            }.map {
                originList = if (getString("name").split(", ").toMutableList().isEmpty())
                    mutableListOf(getString("name")) else getString("name").split(", ").toMutableList()
            }
        }
        originList.add(player.getName())
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
        table.delete(dataSource) {
            where("userId" eq qq)
        }
    }

    override fun removePlayer(player: AOfflinePlayer) {
        val userId = ""
        val list = mutableListOf<String>()
        table.select(dataSource) {
            rows("userId", "name")
        }.map {
            if (if (getString("name").split(", ").toMutableList().isEmpty())
                    getString("name") == player.getName()
                else getString("name").split(", ").toMutableList().contains(player.getName())) {
                val newList = if (getString("name").split(", ").toMutableList().isEmpty())
                    mutableListOf(getString("name")) else getString("name").split(", ").toMutableList()
                newList.remove(player.getName())
                list.addAll(newList)
            }
        }
        table.update(dataSource) {
            set("name", list.joinToString(", "))
            where("userId" eq userId)
        }
    }

    override fun removePlayer(qq: Long, player: AOfflinePlayer) {
        val list = mutableListOf<String>()
        table.select(dataSource) {
            rows("name")
            where("userId" eq qq)
        }.map {
            if (if (getString("name").split(", ").toMutableList().isEmpty())
                    getString("name") == player.getName()
                else getString("name").split(", ").toMutableList().contains(player.getName())) {
                val newList = if (getString("name").split(", ").toMutableList().isEmpty())
                    mutableListOf(getString("name")) else getString("name").split(", ").toMutableList()
                newList.remove(player.getName())
                list.addAll(newList)
            }
        }
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

    override fun getPlayerByQQ(qq: Long): List<AOfflinePlayer> {
        val list = table.select(dataSource) {
            rows("name")
            where("userId" eq qq)
            limit(1)
        }.firstOrNull { if (getString("name").split(", ").toMutableList().isEmpty())
            mutableListOf(getString("name")) else getString("name").split(", ").toMutableList() }?: mutableListOf()
        return list.map { plugin.adapter!!.getOfflinePlayer(it) }
    }

}
