package top.alazeprt.aqqbot.data

import top.alazeprt.aconfiguration.file.FileConfiguration
import top.alazeprt.aconfiguration.file.YamlConfiguration
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.profile.AOfflinePlayer
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class FileDataProvider(val plugin: AQQBot) : DataProvider {

    private val file = File(plugin.getDataFolder(), "data.yml")
    private lateinit var dataConfig: FileConfiguration
    val dataMap: MutableMap<String, MutableList<String>> = ConcurrentHashMap()

    override fun loadDataDependencies() {}

    override fun loadData(type: DataStorageType) {
        dataConfig = YamlConfiguration.loadConfiguration(file)
        dataConfig.getKeys(false).forEach {
            plugin.debugModule?.debugLogger?.log("Loading data: $it -> ${dataConfig.getStringList(it).joinToString(", ")}")
            dataMap[it] = dataConfig.getStringList(it).toMutableList()
        }
    }

    override fun getStorageType(): DataStorageType {
        return DataStorageType.FILE
    }

    override fun saveData(type: DataStorageType) {
        dataMap.forEach {
            plugin.debugModule?.debugLogger?.log("Saving data: $it -> ${it.value.joinToString(", ")}")
            dataConfig[it.key] = it.value
        }
        dataConfig.save(File(plugin.getDataFolder(), "data.yml"))
    }

    override fun saveData(type: Int) {
        when (type) {
            0 -> saveData(DataStorageType.MYSQL)
            1 -> saveData(DataStorageType.SQLITE)
            2 -> saveData(DataStorageType.FILE)
        }
    }

    override fun hasPlayer(player: AOfflinePlayer): Boolean {
        dataMap.values.forEach {
            if (it.contains(player.getName())) {
                return true
            }
        }
        return false
    }

    override fun hasPlayer(name: String): Boolean {
        dataMap.values.forEach {
            if (it.contains(name)) {
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

    override fun addPlayer(qq: Long, name: String) {
        val list = getPlayerByQQ(qq).map { it.getName() }.toMutableList()
        list.add(name)
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

    override fun removePlayer(name: String) {
        dataMap.values.forEach { value ->
            if (value.contains(name)) {
                value.remove(name)
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

    override fun removePlayer(qq: Long, name: String) {
        if (hasQQ(qq)) {
            dataMap.get(qq.toString())!!.forEach {
                if (it == name) {
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

    override fun getQQByPlayer(name: String): Long? {
        dataMap.forEach {
            it.value.forEach { value ->
                if (value.contains(name)) {
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

    override fun getPlayerNameByQQ(qq: Long): List<String> {
        if (!hasQQ(qq)) return emptyList()
        return dataMap[qq.toString()]!!
    }

    override fun getAllData(): Map<Long, List<AOfflinePlayer>> {
        val map = mutableMapOf<Long, List<AOfflinePlayer>>()
        dataMap.forEach {
            map[it.key.toLong()] = it.value.map { plugin.adapter.getOfflinePlayer(it) }
        }
        return map
    }
}