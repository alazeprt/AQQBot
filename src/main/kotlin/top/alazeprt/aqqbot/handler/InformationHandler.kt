package top.alazeprt.aqqbot.handler

import me.lucko.spark.api.statistic.StatisticWindow
import org.bukkit.Bukkit
import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aonebot.event.message.GroupMessageEvent
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.AQQBot.isBukkit
import top.alazeprt.aqqbot.DependencyImpl.Companion.spark
import top.alazeprt.aqqbot.util.AI18n.get

class InformationHandler {
    companion object {
        private fun getTPS(groupId: Long) {
            if (spark == null) {
                AQQBot.oneBotClient.action(SendGroupMessage(groupId,
                    get("qq.information.tps.not_installed_dependency"), true))
                return
            } else {
                val tps = spark?.tps()
                val tps5Secs = roundTPS(tps?.poll(StatisticWindow.TicksPerSecond.SECONDS_5)?: -1.0)
                val tps10Secs = roundTPS(tps?.poll(StatisticWindow.TicksPerSecond.SECONDS_10)?: -1.0)
                val tps1Min = roundTPS(tps?.poll(StatisticWindow.TicksPerSecond.MINUTES_1)?: -1.0)
                val tps5Min = roundTPS(tps?.poll(StatisticWindow.TicksPerSecond.MINUTES_5)?: -1.0)
                val tps15Min = roundTPS(tps?.poll(StatisticWindow.TicksPerSecond.MINUTES_15)?: -1.0)
                AQQBot.oneBotClient.action(SendGroupMessage(groupId, get("qq.information.tps.result", mutableMapOf(
                    Pair("tps_5_seconds", tps5Secs),
                    Pair("tps_10_seconds", tps10Secs),
                    Pair("tps_1_minute", tps1Min),
                    Pair("tps_5_minutes", tps5Min),
                    Pair("tps_15_minutes", tps15Min)
                )), true))
            }
        }
        
        private fun getMSPT(groupId: Long) {
            if (spark == null) {
                AQQBot.oneBotClient.action(SendGroupMessage(groupId,
                    get("qq.information.mspt.not_installed_dependency"), true))
                return
            } else {
                val mspt = spark?.mspt()
                val mspt10Secs = roundMSPT(mspt?.poll(StatisticWindow.MillisPerTick.SECONDS_10)?.median()?: -1.0)
                val mspt1Min = roundMSPT(mspt?.poll(StatisticWindow.MillisPerTick.MINUTES_1)?.median()?: -1.0)
                val mspt5Min = roundMSPT(mspt?.poll(StatisticWindow.MillisPerTick.MINUTES_5)?.median()?: -1.0)
                AQQBot.oneBotClient.action(SendGroupMessage(groupId, get("qq.information.mspt.result", mutableMapOf(
                    Pair("mspt_10_seconds", mspt10Secs),
                    Pair("mspt_1_minute", mspt1Min),
                    Pair("mspt_5_minutes", mspt5Min)
                )), true))
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
                SendGroupMessage(groupId, get("qq.information.player_list.result", mutableMapOf(
                    Pair("count", playerList.size.toString()),
                    Pair("player_list", playerList.joinToString { it.name })
                )), true))
        }

        private fun getCPUInfo(groupId: Long) {
            if (spark == null) {
                AQQBot.oneBotClient.action(SendGroupMessage(groupId,
                    get("qq.information.cpu.not_installed_dependency"), true))
                return
            } else {
                val cpu = spark?.cpuSystem()
                val cpu10Secs = roundCPU(cpu?.poll(StatisticWindow.CpuUsage.SECONDS_10)?: -1.0)
                val cpu1Min = roundCPU(cpu?.poll(StatisticWindow.CpuUsage.MINUTES_1)?: -1.0)
                val cpu15Min = roundCPU(cpu?.poll(StatisticWindow.CpuUsage.MINUTES_15)?: -1.0)
                AQQBot.oneBotClient.action(SendGroupMessage(groupId, get("qq.information.cpu.result", mutableMapOf(
                    Pair("cpu_10_seconds", cpu10Secs),
                    Pair("cpu_1_minute", cpu1Min),
                    Pair("cpu_15_minutes", cpu15Min)
                )), true))
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