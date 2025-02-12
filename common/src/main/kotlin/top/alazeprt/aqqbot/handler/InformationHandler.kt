package top.alazeprt.aqqbot.handler

import me.lucko.spark.api.statistic.StatisticWindow
import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aonebot.event.message.GroupMessageEvent
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.bot.BotProvider

class InformationHandler(val plugin: AQQBot) {
    @Deprecated(message = "This feature was replaced by custom commands")
    private fun getTPS(groupId: Long) {
        if (plugin.spark == null) {
            BotProvider.getBot()?.action(SendGroupMessage(groupId,
                plugin.getMessageManager().get("qq.information.tps.not_installed_dependency"), true))
            return
        } else {
            val tps = plugin.spark?.tps()
            val tps5Secs = roundTPS(tps?.poll(StatisticWindow.TicksPerSecond.SECONDS_5)?: -1.0)
            val tps10Secs = roundTPS(tps?.poll(StatisticWindow.TicksPerSecond.SECONDS_10)?: -1.0)
            val tps1Min = roundTPS(tps?.poll(StatisticWindow.TicksPerSecond.MINUTES_1)?: -1.0)
            val tps5Min = roundTPS(tps?.poll(StatisticWindow.TicksPerSecond.MINUTES_5)?: -1.0)
            val tps15Min = roundTPS(tps?.poll(StatisticWindow.TicksPerSecond.MINUTES_15)?: -1.0)
            BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.getMessageManager().get("qq.information.tps.result", mutableMapOf(
                Pair("tps_5_seconds", tps5Secs),
                Pair("tps_10_seconds", tps10Secs),
                Pair("tps_1_minute", tps1Min),
                Pair("tps_5_minutes", tps5Min),
                Pair("tps_15_minutes", tps15Min)
            )), true))
        }
    }

    @Deprecated(message = "This feature was replaced by custom commands")
    private fun getMSPT(groupId: Long) {
        if (plugin.spark == null) {
            BotProvider.getBot()?.action(SendGroupMessage(groupId,
                plugin.getMessageManager().get("qq.information.mspt.not_installed_dependency"), true))
            return
        } else {
            val mspt = plugin.spark?.mspt()
            val mspt10Secs = roundMSPT(mspt?.poll(StatisticWindow.MillisPerTick.SECONDS_10)?.median()?: -1.0)
            val mspt1Min = roundMSPT(mspt?.poll(StatisticWindow.MillisPerTick.MINUTES_1)?.median()?: -1.0)
            val mspt5Min = roundMSPT(mspt?.poll(StatisticWindow.MillisPerTick.MINUTES_5)?.median()?: -1.0)
            BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.getMessageManager().get("qq.information.mspt.result", mutableMapOf(
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
        val playerList = plugin.adapter!!.getPlayerList().map { it.getName() }.toList()
        BotProvider.getBot()?.action(
            SendGroupMessage(groupId, plugin.getMessageManager().get("qq.information.player_list.result", mutableMapOf(
                Pair("count", playerList.size.toString()),
                Pair("player_list", playerList.joinToString { it })
            )), true))
    }

    @Deprecated(message = "This feature was replaced by custom commands")
    private fun getCPUInfo(groupId: Long) {
        if (plugin.spark == null) {
            BotProvider.getBot()?.action(SendGroupMessage(groupId,
                plugin.getMessageManager().get("qq.information.cpu.not_installed_dependency"), true))
            return
        } else {
            val cpu = plugin.spark?.cpuSystem()
            val cpu10Secs = roundCPU(cpu?.poll(StatisticWindow.CpuUsage.SECONDS_10)?: -1.0)
            val cpu1Min = roundCPU(cpu?.poll(StatisticWindow.CpuUsage.MINUTES_1)?: -1.0)
            val cpu15Min = roundCPU(cpu?.poll(StatisticWindow.CpuUsage.MINUTES_15)?: -1.0)
            BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.getMessageManager().get("qq.information.cpu.result", mutableMapOf(
                Pair("cpu_10_seconds", cpu10Secs),
                Pair("cpu_1_minute", cpu1Min),
                Pair("cpu_15_minutes", cpu15Min)
            )), true))
        }
    }

    fun handle(message: String, event: GroupMessageEvent): Boolean {
        plugin.generalConfig.getStringList("information.tps.command").forEach {
            if (!plugin.generalConfig.getBoolean("information.tps.enable")) return@forEach
            if (message.lowercase() == it.lowercase()) {
                getTPS(event.groupId)
                return true
            }
        }
        plugin.generalConfig.getStringList("information.mspt.command").forEach {
            if (!plugin.generalConfig.getBoolean("information.mspt.enable")) return@forEach
            if (message.lowercase() == it.lowercase()) {
                getMSPT(event.groupId)
                return true
            }
        }
        plugin.generalConfig.getStringList("information.list.command").forEach {
            if (!plugin.generalConfig.getBoolean("information.list.enable")) return@forEach
            if (message.lowercase() == it.lowercase()) {
                getPlayerList(event.groupId)
                return true
            }
        }
        plugin.generalConfig.getStringList("information.cpu.command").forEach {
            if (!plugin.generalConfig.getBoolean("information.cpu.enable")) return@forEach
            if (message.lowercase() == it.lowercase()) {
                getCPUInfo(event.groupId)
                return true
            }
        }
        return false
    }
}