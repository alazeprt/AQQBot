package top.alazeprt.aqqbot.handler

import com.artemis.the.gr8.playerstats.core.statrequest.PlayerStatRequest
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.BanList
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Statistic
import org.bukkit.ban.ProfileBanList
import taboolib.common5.util.replace
import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aonebot.event.message.GroupMessageEvent
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.AQQBot.config
import top.alazeprt.aqqbot.AQQBot.isFileStorage
import top.alazeprt.aqqbot.DependencyImpl.Companion.playerStats
import top.alazeprt.aqqbot.DependencyImpl.Companion.withPAPI
import top.alazeprt.aqqbot.util.AI18n.get
import top.alazeprt.aqqbot.util.AI18n.getList
import top.alazeprt.aqqbot.util.AI18n.getOriginList
import top.alazeprt.aqqbot.util.DBQuery.qqInDatabase
import java.text.SimpleDateFormat

class StatsHandler {
    companion object {

        private val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        private fun formatDuration(seconds: Int): String {
            val days = seconds / (24 * 3600)
            val hours = (seconds % (24 * 3600)) / 3600
            val minutes = (seconds % 3600) / 60
            val remainingSeconds = seconds % 60

            return when {
                days > 0 -> "$days 天 $hours 时 $minutes 分 $remainingSeconds 秒"
                hours > 0 -> "$hours 时 $minutes 分 $remainingSeconds 秒"
                minutes > 0 -> "$minutes 分 $remainingSeconds 秒"
                else -> "$remainingSeconds 秒"
            }
        }

        private fun formatString(input: String): String {
            return input.replace(Regex("&[0-9a-fklmnor]"), "")
        }

        private fun getStats(groupId: Long, userId: Long) {
            if (playerStats == null) {
                AQQBot.oneBotClient.action(SendGroupMessage(groupId,
                    get("qq.stats.not_installed_dependency"), true))
                return
            }
            if (isFileStorage && !AQQBot.dataMap.containsKey(userId.toString())) {
                AQQBot.oneBotClient.action(SendGroupMessage(groupId, get("qq.stats.not_bind"), true))
                return
            } else if (!isFileStorage && qqInDatabase(userId) == null) {
                AQQBot.oneBotClient.action(SendGroupMessage(groupId, get("qq.stats.not_bind"), true))
                return
            }
            val name = if(isFileStorage) AQQBot.dataMap[userId.toString()]!! else qqInDatabase(userId)!!
            val banEntry = Bukkit.getBanList<ProfileBanList>(BanList.Type.PROFILE).getBanEntry(name)
            val uuid = Bukkit.getOfflinePlayer(name).uniqueId
            val lastLogin = Bukkit.getOfflinePlayer(uuid).lastPlayed
            if (banEntry != null) {
                val banTime = banEntry.created
                val expireTime = banEntry.expiration
                val reason = banEntry.reason
                AQQBot.oneBotClient.action(SendGroupMessage(groupId, getList("qq.stats.result.ban", mutableMapOf(
                    Pair("userId", userId.toString()),
                    Pair("name", name),
                    Pair("reason", reason ?: get("qq.stats.result.dont_have_reason")),
                    Pair("ban_time", format.format(banTime)),
                    Pair("unban_time", if (expireTime == null) "永久封禁" else format.format(expireTime)),
                    Pair("last_login_time", if (lastLogin == 0L) get("qq.stats.result.did_not_login") else format.format(lastLogin)),
                    Pair("uuid", uuid.toString())
                ))))
                return
            }
            val mob_killed = playerStats!!.statManager.executePlayerStatRequest(PlayerStatRequest(name)
                .untyped(Statistic.MOB_KILLS)).value
            val online_time = playerStats!!.statManager.executePlayerStatRequest(PlayerStatRequest(name)
                .untyped(Statistic.TOTAL_WORLD_TIME)).value
            val walk_distance = playerStats!!.statManager.executePlayerStatRequest(PlayerStatRequest(name)
                .untyped(Statistic.WALK_ONE_CM)).value
            val run_distance = playerStats!!.statManager.executePlayerStatRequest(PlayerStatRequest(name)
                .untyped(Statistic.SPRINT_ONE_CM)).value
            val mine_ancient_debris = playerStats!!.statManager.executePlayerStatRequest(PlayerStatRequest(name)
                .blockOrItemType(Statistic.MINE_BLOCK, Material.ANCIENT_DEBRIS)).value
            var messageList = getOriginList("qq.stats.result.normal", mutableMapOf(
                Pair("userId", userId.toString()),
                Pair("name", name),
                Pair("online", if (Bukkit.getOfflinePlayer(name).isOnline) "在线" else "离线"),
                Pair("last_login_time", if (lastLogin == 0L) "你还没有有玩过捏" else format.format(lastLogin)),
                Pair("uuid", uuid.toString()),
                Pair("kill_mobs_count", mob_killed?.toString() ?: "0"),
                Pair("online_time", if (online_time == null) "0" else formatDuration(online_time/20)),
                Pair("walk_distance", if (walk_distance == null && run_distance == null) "0" else if (walk_distance == null) (run_distance/100.0).toString()
                else if (run_distance == null) (walk_distance/100.0).toString() else ((run_distance+walk_distance)/100).toString()),
                Pair("break_ancient_debris_count", mine_ancient_debris?.toString() ?: "0")
            ))
            if (config.getBoolean("stats.party.enable") && withPAPI && Bukkit.getOfflinePlayer(name).isOnline) {
                var party = PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(name), config.getString("stats.party.placeholder")!!)
                if (config.getBoolean("stats.party.format")) party = formatString(party)
                messageList = messageList.replace(Pair("\${organization}", party.ifEmpty { get("qq.stats.result.dont_have_organization") }))
                    .toMutableList()
            } else {
                messageList.removeIf {
                    it.contains("\${organization}")
                }
            }
            if (config.getBoolean("stats.prefix.enable") && withPAPI && Bukkit.getOfflinePlayer(name).isOnline) {
                var prefix = PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(name), config.getString("stats.prefix.placeholder")!!)
                if (config.getBoolean("stats.prefix.format")) prefix = formatString(prefix)
                messageList =
                    messageList.replace(Pair("\${prefix}", prefix.ifEmpty { get("qq.stats.result.dont_have_prefix") })).toMutableList()
            } else {
                messageList.removeIf {
                    it.contains("\${prefix}")
                }
            }
            AQQBot.oneBotClient.action(SendGroupMessage(groupId, messageList.joinToString("\n"), true))
        }


        fun handle(message: String, event: GroupMessageEvent): Boolean {
            config.getStringList("stats.command").forEach {
                if (!config.getBoolean("stats.enable")) return@forEach
                if (message.lowercase() == it.lowercase()) {
                    getStats(event.groupId, event.senderId)
                    return true
                }
            }
            return false
        }
    }
}