package top.alazeprt.aqqbot.util

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.permission.Tristate
import com.velocitypowered.proxy.util.TranslatableMapper
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.permission.PermissionChecker
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer
import net.kyori.adventure.util.TriState
import net.kyori.ansi.ColorLevel
import taboolib.platform.VelocityPlugin
import top.alazeprt.aqqbot.AQQBot.config
import java.util.concurrent.CompletableFuture

class AVCSender : CommandSource, ASender {

    val messageList = mutableListOf<String>()

    var tmpMessage = ""

    lateinit var future: CompletableFuture<Boolean>;

    override fun getPermissionValue(p0: String?): Tristate {
        return Tristate.TRUE
    }

    override fun hasPermission(p0: String?): Boolean {
        return true
    }

    override fun getPermissionChecker(): PermissionChecker {
        return PermissionChecker.always(TriState.TRUE)
    }

    fun addContent(message: Component) {
        if (message.children().isEmpty()) {
            if (message is TextComponent) {
                tmpMessage += message.content()
            } else if (message is TranslatableComponent) {
                val serializer = ANSIComponentSerializer.builder().flattener(TranslatableMapper.FLATTENER).colorLevel(ColorLevel.NONE).build()
                tmpMessage += serializer.serialize(message)
            }
        } else {
            message.children().forEach {
                addContent(it)
            }
        }
    }

    override fun sendMessage(source: Identity, message: Component, type: MessageType) {
        addContent(message)
        messageList.addAll(tmpMessage.split("\n"))
    }

    override fun getFormatString(): String {
        var str = messageList.joinToString("\n").replace(Regex("§([0-9a-fklmnor])"), "")
        config.getStringList("command_execution.format_list").forEach {
            if (it != "") {
                str = str.replace(it, "")
            }
        }
        return str
    }

    override fun getRawString(): String {
        var str = messageList.joinToString("\n")
        config.getStringList("command_execution.format_list").forEach {
            if (it != "") {
                str = str.replace(it, "")
            }
        }
        return str
    }

    override fun execute(command: String) {
        future = VelocityPlugin.getInstance().server.commandManager.executeImmediatelyAsync(this, command)
    }
}