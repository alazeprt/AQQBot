package top.alazeprt.aqqbot.handler

import me.lucko.spark.api.statistic.StatisticWindow
import org.bukkit.Bukkit
import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aonebot.event.message.GroupMessageEvent
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.AQQBot.isBukkit
import top.alazeprt.aqqbot.DependencyImpl.Companion.spark

class InformationHandler {
    companion object {
        private fun getTPS(groupId: Long) {
            if (spark == null) {
                AQQBot.oneBotClient.action(SendGroupMessage(groupId,
                    "服务器尚未安装spark插件, 无法获取TPS! 请联系服务器管理员!", true))
                return
            } else {
                val tps = spark?.tps()
                val tps5Secs = roundTPS(tps?.poll(StatisticWindow.TicksPerSecond.SECONDS_5)?: -1.0)
                val tps10Secs = roundTPS(tps?.poll(StatisticWindow.TicksPerSecond.SECONDS_10)?: -1.0)
                val tps1Min = roundTPS(tps?.poll(StatisticWindow.TicksPerSecond.MINUTES_1)?: -1.0)
                val tps5Min = roundTPS(tps?.poll(StatisticWindow.TicksPerSecond.MINUTES_5)?: -1.0)
                val tps15Min = roundTPS(tps?.poll(StatisticWindow.TicksPerSecond.MINUTES_15)?: -1.0)
                AQQBot.oneBotClient.action(SendGroupMessage(groupId,
                    "服务器TPS: $tps5Secs, $tps10Secs, $tps1Min, $tps5Min, $tps15Min", true))
            }
        }
        
        private fun getMSPT(groupId: Long) {
            if (spark == null) {
                AQQBot.oneBotClient.action(SendGroupMessage(groupId,
                    "服务器尚未安装spark插件, 无法获取MSPT! 请联系服务器管理员!", true))
                return
            } else {
                val mspt = spark?.mspt()
                val mspt10Secs = roundMSPT(mspt?.poll(StatisticWindow.MillisPerTick.SECONDS_10)?.median()?: -1.0)
                val mspt1Min = roundMSPT(mspt?.poll(StatisticWindow.MillisPerTick.MINUTES_1)?.median()?: -1.0)
                val mspt5Min = roundMSPT(mspt?.poll(StatisticWindow.MillisPerTick.MINUTES_5)?.median()?: -1.0)
                AQQBot.oneBotClient.action(SendGroupMessage(groupId,
                    "服务器MSPT: $mspt10Secs, $mspt1Min, $mspt5Min", true))
            }
        }

        private fun roundTPS(tps: Double): String {
            return if (tps >= 20) {
                String.format("%.2f", 20.00)
            } else {
                String.format("%.2f", tps)
            }
        }

        private fun roundMSPT(mspt: Double): String {
            return String.format("%.2f", mspt)
        }

        private fun roundCPU(cpu: Double): String {
            return String.format("%.2f", cpu*100)
        }

        private fun getPlayerList(groupId: Long) {
            val playerList = Bukkit.getOnlinePlayers()
            AQQBot.oneBotClient.action(
                SendGroupMessage(groupId, "服务器在线玩家(${playerList.size}): " +
                    playerList.map{ it.name }.joinToString(", "), true))
        }

        private fun getCPUInfo(groupId: Long) {
            if (spark == null) {
                AQQBot.oneBotClient.action(SendGroupMessage(groupId,
                    "服务器尚未安装spark插件, 无法获取CPU信息! 请联系服务器管理员!", true))
                return
            } else {
                val cpu = spark?.cpuSystem()
                val cpu10Secs = roundCPU(cpu?.poll(StatisticWindow.CpuUsage.SECONDS_10)?: -1.0)
                val cpu1Min = roundCPU(cpu?.poll(StatisticWindow.CpuUsage.MINUTES_1)?: -1.0)
                val cpu15Min = roundCPU(cpu?.poll(StatisticWindow.CpuUsage.MINUTES_15)?: -1.0)
                AQQBot.oneBotClient.action(SendGroupMessage(groupId, "服务器CPU占用率: $cpu10Secs%, $cpu1Min%, $cpu15Min%", true))
            }
        }

        fun handle(message: String, event: GroupMessageEvent): Boolean {
            AQQBot.config.getStringList("information.tps.command").forEach {
                if (!AQQBot.config.getBoolean("information.tps.enable")) return@forEach
                if (message.lowercase() == it.lowercase() && isBukkit) {
                    getTPS(event.groupId)
                    return true
                }
            }
            AQQBot.config.getStringList("information.mspt.command").forEach {
                if (!AQQBot.config.getBoolean("information.mspt.enable")) return@forEach
                if (message.lowercase() == it.lowercase() && isBukkit) {
                    getMSPT(event.groupId)
                    return true
                }
            }
            AQQBot.config.getStringList("information.list.command").forEach {
                if (!AQQBot.config.getBoolean("information.list.enable")) return@forEach
                if (message.lowercase() == it.lowercase() && isBukkit) {
                    getPlayerList(event.groupId)
                    return true
                }
            }
            AQQBot.config.getStringList("information.cpu.command").forEach {
                if (!AQQBot.config.getBoolean("information.cpu.enable")) return@forEach
                if (message.lowercase() == it.lowercase()) {
                    getCPUInfo(event.groupId)
                    return true
                }
            }
            return false
        }
    }
}