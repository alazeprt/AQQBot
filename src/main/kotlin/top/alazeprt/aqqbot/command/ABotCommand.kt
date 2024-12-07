package top.alazeprt.aqqbot.command

import org.bukkit.command.CommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.info
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common.platform.function.submit
import taboolib.module.configuration.Configuration
import top.alazeprt.aonebot.BotClient
import top.alazeprt.aqqbot.AQQBot.botConfig
import top.alazeprt.aqqbot.AQQBot.config
import top.alazeprt.aqqbot.AQQBot.customCommands
import top.alazeprt.aqqbot.AQQBot.enableGroups
import top.alazeprt.aqqbot.AQQBot.isBukkit
import top.alazeprt.aqqbot.AQQBot.messageConfig
import top.alazeprt.aqqbot.AQQBot.oneBotClient
import top.alazeprt.aqqbot.DependencyImpl
import top.alazeprt.aqqbot.qq.BotListener
import top.alazeprt.aqqbot.util.ACustom
import top.alazeprt.aqqbot.util.AI18n.get
import java.net.URI

@CommandHeader("aqqbot", ["abot", "qqbot"], permission = "aqqbot.command")
object ABotCommand {
    @CommandBody
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            val s = System.currentTimeMillis()
            submit(async = true) {
                info("Reloading AQQBot ...")
                if (oneBotClient.isConnected) {
                    oneBotClient.disconnect()
                }
                enableGroups.clear()
                val configFile = releaseResourceFile("config.yml", replace = false)
                config = Configuration.loadFromFile(configFile)
                val botFile = releaseResourceFile("bot.yml", replace = false)
                botConfig = Configuration.loadFromFile(botFile)
                val messageFile = releaseResourceFile("messages.yml", replace = false)
                messageConfig = Configuration.loadFromFile(messageFile)
                customCommands.clear()
                val customFile = releaseResourceFile("custom.yml", replace = false)
                val customConfig = Configuration.loadFromFile(customFile)
                customConfig.getKeys(false).forEach {
                    if (customConfig.getBoolean("$it.enable")) {
                        val command = customConfig.getStringList("$it.command")
                        val output = customConfig.getStringList("$it.output")
                        val unbind_output = customConfig.getStringList("$it.unbind_output")
                        val format = customConfig.getBoolean("$it.format")
                        customCommands.add(ACustom(command, output, unbind_output, format))
                    }
                }
                botConfig.getStringList("groups").forEach {
                    enableGroups.add(it)
                }
                DependencyImpl.loadSpark()
                if (isBukkit) {
                    DependencyImpl.loadPlayerStats()
                    DependencyImpl.loadPAPI()
                }
                val url = "ws://" + botConfig.getString("ws.host") + ":" + botConfig.getInt("ws.port")
                oneBotClient = BotClient(URI.create(url))
                oneBotClient.connect()
                oneBotClient.registerEvent(BotListener())
                sender.sendMessage(formatString(get("game.reload", mutableMapOf("time" to (System.currentTimeMillis() - s).toString()))))
                info("Reloaded AQQBot in ${System.currentTimeMillis() - s} ms")
            }
        }
    }

    private fun formatString(input: String): String {
        return input.replace(Regex("&([0-9a-fklmnor])")) { matchResult ->
            "§" + matchResult.groupValues[1]
        }
    }
}