package top.alazeprt.aqqbot.data

import top.alazeprt.aconfiguration.file.FileConfiguration
import top.alazeprt.aconfiguration.file.YamlConfiguration
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.profile.AOfflinePlayer
import java.io.File

class FileDataProvider(val plugin: AQQBot) : DataProvider {

    private val file = File(plugin.getDataFolder(), "data.yml")
    private lateinit var dataConfig: FileConfiguration
    val dataMap: MutableMap<String, MutableList<String>> = mutableMapOf()

    override fun loadData(type: DataStorageType) {
        dataConfig = YamlConfiguration.loadConfiguration(file)
        dataConfig.getKeys(false).forEach {
            dataMap[it] = dataConfig.getStringList(it).toMutableList()
        }
    }

    override fun getStorageType(): DataStorageType {
        return DataStorageType.FILE
    }

    override fun saveData(type: DataStorageType) {
        dataMap.forEach {
            dataConfig[it.key] = it.value
        }
        dataConfig.save(File(plugin.getDataFolder(), "data.yml"))
    }

    override fun hasPlayer(player: AOfflinePlayer): Boolean {
        dataMap.values.forEach {
            if (it.contains(player.getName())) {
                return true
            }
        }
        return false
    }

    override fun hasQQ(qq: Long): Boolean {
        return dataMap.containsKey(qq.toString())
    }

    override fun addPlayer(qq: Long, player: AOfflinePlayer) {
        val list = getPlayerByQQ(qq).map { it.getName() }.toMutableList()
        list.add(player.getName())
        dataMap[qq.toString()] = list
    }

    override fun removePlayer(player: AOfflinePlayer) {
        dataMap.values.forEach { value ->
            if (value.contains(player.getName())) {
                value.remove(player.getName())
                return
            }
        }
    }

    override fun removePlayer(qq: Long) {
        if (hasQQ(qq)) dataMap.remove(qq.toString())
    }

    override fun removePlayer(qq: Long, player: AOfflinePlayer) {
        if (hasQQ(qq)) {
            dataMap.get(qq.toString())!!.forEach {
                if (it == player.getName()) {
                    dataMap[qq.toString()]!!.remove(it)
                    return
                }
            }
        }
    }

    override fun getQQByPlayer(player: AOfflinePlayer): Long? {
        dataMap.forEach {
            it.value.forEach { value ->
                if (value == player.getName()) {
                    return it.key.toLong()
                }
            }
        }
        return null
    }

    override fun getPlayerByQQ(qq: Long): List<AOfflinePlayer> {
        if (!hasQQ(qq)) return emptyList()
        return dataMap[qq.toString()]!!.map { plugin.adapter!!.getOfflinePlayer(it) }
    }
}