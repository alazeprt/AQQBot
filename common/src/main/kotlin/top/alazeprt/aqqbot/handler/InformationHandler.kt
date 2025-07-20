package top.alazeprt.aqqbot.handler

import me.lucko.spark.api.SparkProvider
import me.lucko.spark.api.statistic.StatisticWindow
import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aonebot.event.message.GroupMessageEvent
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.api.AQQBotAPI
import top.alazeprt.aqqbot.api.event.qq.PostInformationEvent
import top.alazeprt.aqqbot.api.event.qq.PreInformationEvent
import top.alazeprt.aqqbot.api.event.qq.reason.InformationCancelReason
import top.alazeprt.aqqbot.bot.BotProvider

class InformationHandler(val plugin: AQQBot) {
    @Deprecated(message = "This feature was replaced by custom commands")
    private fun getTPS(groupId: Long, userId: Long) {
        if (!plugin.spark) {
            BotProvider.getBot()?.action(SendGroupMessage(groupId,
                plugin.messageManager.get("qq.information.tps.not_installed_dependency", groupId), true))
            AQQBotAPI.fireEvent(PostInformationEvent(groupId, userId, PreInformationEvent.Type.TPS, true, InformationCancelReason.NOT_INSTALLED_DEPENDENCY))
            return
        } else {
            val event = PreInformationEvent(groupId, userId, PreInformationEvent.Type.TPS)
            AQQBotAPI.fireEvent(event)
            if (event.isCanceled()) {
                AQQBotAPI.fireEvent(PostInformationEvent(groupId, userId, PreInformationEvent.Type.TPS, true, InformationCancelReason.CANCEL_BY_PLUGIN))
                BotProvider.getBot()?.action(SendGroupMessage(groupId,
                    plugin.messageManager.get("qq.cancel_by_plugin", groupId), true))
                return
            }
            val tps = SparkProvider.get().tps()
            val tps5Secs = roundTPS(tps?.poll(StatisticWindow.TicksPerSecond.SECONDS_5)?: -1.0)
            val tps10Secs = roundTPS(tps?.poll(StatisticWindow.TicksPerSecond.SECONDS_10)?: -1.0)
            val tps1Min = roundTPS(tps?.poll(StatisticWindow.TicksPerSecond.MINUTES_1)?: -1.0)
            val tps5Min = roundTPS(tps?.poll(StatisticWindow.TicksPerSecond.MINUTES_5)?: -1.0)
            val tps15Min = roundTPS(tps?.poll(StatisticWindow.TicksPerSecond.MINUTES_15)?: -1.0)
            BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.messageManager.get("qq.information.tps.result", mutableMapOf(
                Pair("tps_5_seconds", tps5Secs),
                Pair("tps_10_seconds", tps10Secs),
                Pair("tps_1_minute", tps1Min),
                Pair("tps_5_minutes", tps5Min),
                Pair("tps_15_minutes", tps15Min)
            ), groupId), true))
            AQQBotAPI.fireEvent(PostInformationEvent(groupId, userId, PreInformationEvent.Type.TPS, false, null))
        }
    }

    @Deprecated(message = "This feature was replaced by custom commands")
    private fun getMSPT(groupId: Long, userId: Long) {
        if (!plugin.spark) {
            BotProvider.getBot()?.action(SendGroupMessage(groupId,
                plugin.messageManager.get("qq.information.mspt.not_installed_dependency", groupId), true))
            AQQBotAPI.fireEvent(PostInformationEvent(groupId, userId, PreInformationEvent.Type.MSPT, true, InformationCancelReason.NOT_INSTALLED_DEPENDENCY))
            return
        } else {
            val event = PreInformationEvent(groupId, userId, PreInformationEvent.Type.MSPT)
            AQQBotAPI.fireEvent(event)
            if (event.isCanceled()) {
                AQQBotAPI.fireEvent(PostInformationEvent(groupId, userId, PreInformationEvent.Type.MSPT, true, InformationCancelReason.CANCEL_BY_PLUGIN))
                BotProvider.getBot()?.action(SendGroupMessage(groupId,
                    plugin.messageManager.get("qq.cancel_by_plugin", groupId), true))
                return
            }
            val mspt = SparkProvider.get().mspt()
            val mspt10Secs = roundMSPT(mspt?.poll(StatisticWindow.MillisPerTick.SECONDS_10)?.median()?: -1.0)
            val mspt1Min = roundMSPT(mspt?.poll(StatisticWindow.MillisPerTick.MINUTES_1)?.median()?: -1.0)
            val mspt5Min = roundMSPT(mspt?.poll(StatisticWindow.MillisPerTick.MINUTES_5)?.median()?: -1.0)
            BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.messageManager.get("qq.information.mspt.result", mutableMapOf(
                Pair("mspt_10_seconds", mspt10Secs),
                Pair("mspt_1_minute", mspt1Min),
                Pair("mspt_5_minutes", mspt5Min)
            ), groupId), true))
            AQQBotAPI.fireEvent(PostInformationEvent(groupId, userId, PreInformationEvent.Type.MSPT, false, null))
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

    private fun getPlayerList(groupId: Long, userId: Long) {
        val event = PreInformationEvent(groupId, userId, PreInformationEvent.Type.PLAYER_LIST)
        AQQBotAPI.fireEvent(event)
        if (event.isCanceled()) {
            AQQBotAPI.fireEvent(PostInformationEvent(groupId, userId, PreInformationEvent.Type.PLAYER_LIST, true, InformationCancelReason.CANCEL_BY_PLUGIN))
            BotProvider.getBot()?.action(SendGroupMessage(groupId,
                plugin.messageManager.get("qq.cancel_by_plugin", groupId), true))
            return
        }
        val playerList = plugin.adapter!!.getPlayerList().map { it.getName() }.toList()
        BotProvider.getBot()?.action(
            SendGroupMessage(groupId, plugin.messageManager.get("qq.information.player_list.result", mutableMapOf(
                Pair("count", playerList.size.toString()),
                Pair("player_list", playerList.joinToString { it })
            ), groupId), true))
        AQQBotAPI.fireEvent(PostInformationEvent(groupId, userId, PreInformationEvent.Type.PLAYER_LIST, false, null))
    }

    @Deprecated(message = "This feature was replaced by custom commands")
    private fun getCPUInfo(groupId: Long, userId: Long) {
        if (!plugin.spark) {
            AQQBotAPI.fireEvent(PostInformationEvent(groupId, userId, PreInformationEvent.Type.CPU_USAGE, true, InformationCancelReason.NOT_INSTALLED_DEPENDENCY))
            BotProvider.getBot()?.action(SendGroupMessage(groupId,
                plugin.messageManager.get("qq.information.cpu.not_installed_dependency", groupId), true))
            return
        } else {
            val cpu = SparkProvider.get().cpuSystem()
            val cpu10Secs = roundCPU(cpu?.poll(StatisticWindow.CpuUsage.SECONDS_10)?: -1.0)
            val cpu1Min = roundCPU(cpu?.poll(StatisticWindow.CpuUsage.MINUTES_1)?: -1.0)
            val cpu15Min = roundCPU(cpu?.poll(StatisticWindow.CpuUsage.MINUTES_15)?: -1.0)
            val event = PreInformationEvent(groupId, userId, PreInformationEvent.Type.CPU_USAGE)
            AQQBotAPI.fireEvent(event)
            if (event.isCanceled()) {
                AQQBotAPI.fireEvent(PostInformationEvent(groupId, userId, PreInformationEvent.Type.CPU_USAGE, true, InformationCancelReason.CANCEL_BY_PLUGIN))
                BotProvider.getBot()?.action(SendGroupMessage(groupId,
                    plugin.messageManager.get("qq.cancel_by_plugin", groupId), true))
                return
            }
            BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.messageManager.get("qq.information.cpu.result", mutableMapOf(
                Pair("cpu_10_seconds", cpu10Secs),
                Pair("cpu_1_minute", cpu1Min),
                Pair("cpu_15_minutes", cpu15Min)
            ), groupId), true))
            AQQBotAPI.fireEvent(PostInformationEvent(groupId, userId, PreInformationEvent.Type.CPU_USAGE, false, null))
        }
    }

    fun handle(message: String, event: GroupMessageEvent): Boolean {
        plugin.generalConfig.getStringList("information.tps.command", event.groupId).forEach {
            if (!plugin.generalConfig.getBoolean("information.tps.enable", event.groupId)) {
                AQQBotAPI.fireEvent(PostInformationEvent(event.groupId, event.senderId, PreInformationEvent.Type.TPS, true, InformationCancelReason.NOT_ENABLE))
                return@forEach
            }
            if (message.lowercase() == it.lowercase()) {
                getTPS(event.groupId, event.senderId)
                return true
            }
        }
        plugin.generalConfig.getStringList("information.mspt.command", event.groupId).forEach {
            if (!plugin.generalConfig.getBoolean("information.mspt.enable", event.groupId)) {
                AQQBotAPI.fireEvent(PostInformationEvent(event.groupId, event.senderId, PreInformationEvent.Type.MSPT, true, InformationCancelReason.NOT_ENABLE))
                return@forEach
            }
            if (message.lowercase() == it.lowercase()) {
                getMSPT(event.groupId, event.senderId)
                return true
            }
        }
        plugin.generalConfig.getStringList("information.list.command", event.groupId).forEach {
            if (!plugin.generalConfig.getBoolean("information.list.enable", event.groupId)) {
                AQQBotAPI.fireEvent(PostInformationEvent(event.groupId, event.senderId, PreInformationEvent.Type.PLAYER_LIST, true, InformationCancelReason.NOT_ENABLE))
                return@forEach
            }
            if (message.lowercase() == it.lowercase()) {
                getPlayerList(event.groupId, event.senderId)
                return true
            }
        }
        plugin.generalConfig.getStringList("information.cpu.command", event.groupId).forEach {
            if (!plugin.generalConfig.getBoolean("information.cpu.enable", event.groupId)) {
                AQQBotAPI.fireEvent(PostInformationEvent(event.groupId, event.senderId, PreInformationEvent.Type.CPU_USAGE, true, InformationCancelReason.NOT_ENABLE))
                return@forEach
            }
            if (message.lowercase() == it.lowercase()) {
                getCPUInfo(event.groupId, event.senderId)
                return true
            }
        }
        return false
    }
}