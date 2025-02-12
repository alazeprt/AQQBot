package top.alazeprt.aqqbot.data

import top.alazeprt.aqqbot.profile.AOfflinePlayer

interface DataProvider {
    fun loadData(type: DataStorageType)

    fun getStorageType(): DataStorageType

    fun saveData(type: DataStorageType)

    fun hasPlayer(player: AOfflinePlayer): Boolean

    fun hasQQ(qq: Long): Boolean

    fun addPlayer(qq: Long, player: AOfflinePlayer)

    fun removePlayer(player: AOfflinePlayer)

    fun removePlayer(qq: Long)

    fun removePlayer(qq: Long, player: AOfflinePlayer)

    fun getQQByPlayer(player: AOfflinePlayer): Long?

    fun getPlayerByQQ(qq: Long): List<AOfflinePlayer>
}