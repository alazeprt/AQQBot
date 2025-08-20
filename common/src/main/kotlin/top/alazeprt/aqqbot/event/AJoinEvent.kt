package top.alazeprt.aqqbot.event

import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.api.AQQBotAPI
import top.alazeprt.aqqbot.api.event.game.PostPlayerJoinEvent
import top.alazeprt.aqqbot.api.event.game.PrePlayerJoinEvent
import top.alazeprt.aqqbot.api.event.game.reason.JoinFailedReason
import top.alazeprt.aqqbot.event.AEventUtil.playerStatusHandler
import top.alazeprt.aqqbot.event.AEventUtil.whitelistHandler
import top.alazeprt.aqqbot.profile.APlayer
import java.util.function.Consumer

class AJoinEvent(val plugin: AQQBot, private val player: APlayer, val kickMethod: Consumer<String>) : AEvent {
    override fun handle() {
        plugin.debugModule?.debugLogger?.log("${player.getName()} joined the game")
        val event = PrePlayerJoinEvent(this, player.getName(), plugin.getQQByPlayer(player)?: -1)
        AQQBotAPI.fireEvent(event)
        if (event.isCanceled) {
            AQQBotAPI.fireEvent(PostPlayerJoinEvent(player.getName(), plugin.getQQByPlayer(player)?: -1, true,
                JoinFailedReason.CANCEL_BY_PLUGIN))
            return
        }
        var kickMessage = ""
        val handle1 = whitelistHandler(plugin, player.getName()) {
            plugin.debugModule?.debugLogger?.log("kick ${player.getName()} because unbind")
            kickMessage = it
            AQQBotAPI.fireEvent(PostPlayerJoinEvent(player.getName(), -1, true, JoinFailedReason.UNBIND))
        }
        if (handle1 && !player.hasPermission(plugin.generalConfig.getString("whitelist.bypass_permission", null))) {
            kickMethod.accept(kickMessage)
        } else if (handle1) {
            plugin.debugModule?.debugLogger?.log("allow ${player.getName()} to join because bypass permission")
        }
        if (!handle1) {
            playerStatusHandler(plugin, player, true)
            if (plugin.configNeedUpdate() && player.hasPermission("aqqbot.admin")) {
                plugin.submitLater(20) {
                    player.sendMessage("§a检测到你正在使用 AQQBot 的低版本配置文件, 这可能会引起一些问题")
                    player.sendMessage("§a插件已自动释放新版本配置文件并命名为 config_new.yml, 请根据你的旧版本配置文件 (config.yml) 修改该文件并重命名为 config.yml, 最后执行 /aqqbot reload 应用修改")
                }
            }
        }
        AQQBotAPI.fireEvent(PostPlayerJoinEvent(player.getName(), plugin.getQQByPlayer(player)?: -1, false, null))
    }


}