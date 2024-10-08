package top.alazeprt.aqqbot.handler

import me.lucko.spark.api.statistic.StatisticWindow
import org.bukkit.Bukkit
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.DependencyImpl.Companion.spark

class InformationHandler {
    companion object {
        fun getTPS(groupId: Long) {
            if(spark == null) {
                AQQBot.oneBotClient.bot.sendGroupMsg(groupId,
                    "服务器尚未安装spark插件, 无法获取TPS! 请联系服务器管理员!", true)
                return
            } else {
                val tps = spark?.tps()
                val tps5Secs = roundTPS(tps?.poll(StatisticWindow.TicksPerSecond.SECONDS_5)?: -1.0)
                val tps10Secs = roundTPS(tps?.poll(StatisticWindow.TicksPerSecond.SECONDS_10)?: -1.0)
                val tps1Min = roundTPS(tps?.poll(StatisticWindow.TicksPerSecond.MINUTES_1)?: -1.0)
                val tps5Min = roundTPS(tps?.poll(StatisticWindow.TicksPerSecond.MINUTES_5)?: -1.0)
                val tps15Min = roundTPS(tps?.poll(StatisticWindow.TicksPerSecond.MINUTES_15)?: -1.0)
                AQQBot.oneBotClient.bot.sendGroupMsg(groupId,
                    "服务器TPS: $tps5Secs, $tps10Secs, $tps1Min, $tps5Min, $tps15Min", true)
            }

        }

        private fun roundTPS(tps: Double): String {
            return if (tps > 20) {
                String.format("%.2f", 20.00)
            } else {
                String.format("%.2f", tps)
            }
        }

        fun getPlayerList(groupId: Long) {
            val playerList = Bukkit.getOnlinePlayers()
            AQQBot.oneBotClient.bot.sendGroupMsg(groupId, "服务器在线玩家(${playerList.size}): " +
                    playerList.map{ it.name }.joinToString(", "), true)
        }
    }
}