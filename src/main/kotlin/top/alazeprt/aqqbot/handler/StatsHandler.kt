package top.alazeprt.aqqbot.handler

import com.artemis.the.gr8.playerstats.core.statrequest.PlayerStatRequest
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.BanList
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Statistic
import org.bukkit.ban.ProfileBanList
import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aonebot.event.message.GroupMessageEvent
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.AQQBot.config
import top.alazeprt.aqqbot.DependencyImpl.Companion.withPAPI
import top.alazeprt.aqqbot.DependencyImpl.Companion.playerStats
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
                    "服务器尚未安装PlayerStats插件, 无法获取玩家统计信息! 请联系服务器管理员!", true))
                return
            }
            if (!AQQBot.dataMap.containsKey(userId.toString())) {
                AQQBot.oneBotClient.action(SendGroupMessage(groupId, "你尚未绑定账号! 无法获取玩家统计信息!", true))
                return
            }
            val name = AQQBot.dataMap[userId.toString()]!!
            val banEntry = Bukkit.getBanList<ProfileBanList>(BanList.Type.PROFILE).getBanEntry(name)
            if (banEntry != null) {
                val banTime = banEntry.created
                val expireTime = banEntry.expiration
                val reason = banEntry.reason
                AQQBot.oneBotClient.action(SendGroupMessage(groupId, "QQ号: ${userId}\n" +
                        "游戏名: ${name}\n" +
                        "在线状态: 封禁\n" +
                        "封禁原因: ${reason}\n" +
                        "封禁时间: ${format.format(banTime)}\n" +
                        "解封时间: ${if (expireTime == null) "永久封禁" else format.format(expireTime)}"))
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
            val uuid = Bukkit.getOfflinePlayer(name).uniqueId
            val lastLogin = Bukkit.getOfflinePlayer(uuid).lastPlayed
            var message = "QQ号: ${userId}\n" +
                    "游戏名: ${name}\n" +
                    "在线状态: ${if (Bukkit.getOfflinePlayer(name).isOnline) "在线" else "离线"}\n" +
                    "上次登录时间: ${if (lastLogin == 0L) "你还没有有玩过捏" else format.format(lastLogin)}\n" +
                    "UUID: ${uuid}\n" +
                    "击杀怪物数: ${mob_killed ?: 0}\n" +
                    "在线时长: ${if (online_time == null) 0 else formatDuration(online_time/20)}\n" +
                    "行走距离: ${if (walk_distance == null && run_distance == null) 0 else if (walk_distance == null) run_distance/100.0 
                    else if (run_distance == null) walk_distance/100.0 else (run_distance+walk_distance)/100}m\n" +
                    "挖掘残骸数: ${mine_ancient_debris ?: 0}"
            if (config.getBoolean("stats.party.enable") && withPAPI && Bukkit.getOfflinePlayer(name).isOnline) {
                var party = PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(name), config.getString("stats.party.placeholder")!!)
                if (config.getBoolean("stats.party.format")) party = formatString(party)
                message += "\n组织: ${party.ifEmpty { "你还没有加入组织捏" }}"
            }
            if (config.getBoolean("stats.prefix.enable") && withPAPI && Bukkit.getOfflinePlayer(name).isOnline) {
                var prefix = PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(name), config.getString("stats.prefix.placeholder")!!)
                if (config.getBoolean("stats.prefix.format")) prefix = formatString(prefix)
                message += "\n称号: ${prefix.ifEmpty { "你还没有称号捏" }}"
            }
            AQQBot.oneBotClient.action(SendGroupMessage(groupId, message, true))
        }


        fun handle(message: String, event: GroupMessageEvent): Boolean {
            config.getStringList("stats.command").forEach {
                if (message.lowercase() == it.lowercase()) {
                    getStats(event.groupId, event.senderId)
                    return true
                }
            }
            return false
        }
    }
}