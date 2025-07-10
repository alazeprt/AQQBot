package top.alazeprt.aqqbot.event

import org.geysermc.floodgate.api.FloodgateApi
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.event.AEventUtil.playerStatusHandler
import top.alazeprt.aqqbot.event.AEventUtil.whitelistHandler
import top.alazeprt.aqqbot.profile.APlayer
import java.util.function.Consumer

class AJoinEvent(val plugin: AQQBot, private val player: APlayer, val kickMethod: Consumer<String>) : AEvent {
    override fun handle() {
        plugin.debugModule?.debugLogger?.log("${player.getName()} joined the game")
        var handle2 = false
        var kickMessage = ""
        val handle1 = whitelistHandler(plugin, player.getName()) { it ->
            if (plugin.floodgateApi && FloodgateApi.getInstance()?.isFloodgatePlayer(player.getUUID()) == true) {
                plugin.debugModule?.debugLogger?.log("${player.getName()} is bedrock player")
                if (FloodgateApi.getInstance()?.getPlayer(player.getUUID())?.username.isNullOrBlank()) {
                    plugin.debugModule?.debugLogger?.log("kick ${player.getName()} because floodgate username is null")
                    kickMessage = it
                    return@whitelistHandler
                }
                handle2 = whitelistHandler(plugin,
                    FloodgateApi.getInstance()?.getPlayer(player.getUUID())?.username!!) {
                    plugin.debugModule?.debugLogger?.log("kick ${player.getName()} because unbind")
                    kickMessage = it
                }
            } else {
                plugin.debugModule?.debugLogger?.log("kick ${player.getName()} because unbind")
                kickMessage = it
            }
        }
        if (handle1 || handle2) {
            kickMethod.accept(kickMessage)
        }
        if (!handle1 || !handle2) {
            playerStatusHandler(plugin, player, true)
            if (plugin.configNeedUpdate() && player.hasPermission("aqqbot.admin")) {
                plugin.submitLater(20) {
                    player.sendMessage("§a检测到你正在使用 AQQBot 的低版本配置文件, 这可能会引起一些问题")
                    player.sendMessage("§a插件已自动释放新版本配置文件并命名为 config_new.yml, 请根据你的旧版本配置文件 (config.yml) 修改该文件并重命名为 config.yml, 最后执行 /aqqbot reload 应用修改")
                }
            }
        }
    }


}