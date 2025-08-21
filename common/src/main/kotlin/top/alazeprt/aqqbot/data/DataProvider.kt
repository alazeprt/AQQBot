package top.alazeprt.aqqbot.data

import top.alazeprt.aqqbot.profile.AOfflinePlayer

interface DataProvider {
    fun loadData(type: DataStorageType)

    fun loadDataDependencies()

    fun getStorageType(): DataStorageType

    fun saveData(type: DataStorageType)

    fun saveData(type: Int)

    fun hasPlayer(name: String): Boolean

    fun hasPlayer(player: AOfflinePlayer): Boolean

    fun hasQQ(qq: Long): Boolean

    fun addPlayer(qq: Long, player: AOfflinePlayer)

    fun addPlayer(qq: Long, name: String)

    fun removePlayer(player: AOfflinePlayer)

    fun removePlayer(name: String)

    fun removePlayer(qq: Long)

    fun removePlayer(qq: Long, player: AOfflinePlayer)

    fun removePlayer(qq: Long, name: String)

    fun getQQByPlayer(player: AOfflinePlayer): Long?

    fun getQQByPlayer(name: String): Long?

    fun getPlayerByQQ(qq: Long): List<AOfflinePlayer>

    fun getPlayerNameByQQ(qq: Long): List<String>

    fun getAllData(): Map<Long, List<AOfflinePlayer>>
}