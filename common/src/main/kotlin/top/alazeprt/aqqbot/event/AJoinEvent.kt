package top.alazeprt.aqqbot.event

import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.event.AEventUtil.playerStatusHandler
import top.alazeprt.aqqbot.event.AEventUtil.whitelistHandler
import top.alazeprt.aqqbot.profile.APlayer

class AJoinEvent(val plugin: AQQBot, private val player: APlayer) : AEvent {
    override fun handle() {
        if (plugin.configNeedUpdate() && player.hasPermission("aqqbot.admin")) {
            player.sendMessage("§a检测到你正在使用 AQQBot 的低版本配置文件, 这可能会引起一些问题")
            player.sendMessage("§a插件已自动释放新版本配置文件并命名为 config_new.yml, 请根据你的旧版本配置文件 (config.yml) 修改该文件并重命名为 config.yml, 最后执行 /aqqbot reload 应用修改")
        }
        val handle = whitelistHandler(plugin, player.getName()) {
            player.kick(it)
        }
        if (!handle) playerStatusHandler(plugin, player.getName(), true)
    }


}