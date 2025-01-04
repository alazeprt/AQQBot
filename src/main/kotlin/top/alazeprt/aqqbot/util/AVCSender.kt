package top.alazeprt.aqqbot.util

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.permission.Tristate
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.chat.ChatType
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.identity.Identified
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.permission.PermissionChecker
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.util.TriState
import taboolib.platform.VelocityPlugin
import top.alazeprt.aqqbot.AQQBot.config
import java.util.concurrent.CompletableFuture

class AVCSender : CommandSource, ASender {

    val messageList = mutableListOf<String>()

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

    override fun sendRichMessage(message: String, vararg resolvers: TagResolver) {
        messageList.add(message)
    }

    override fun sendMessage(message: Component) {
        if (message is TextComponent) {
            messageList.add(message.content())
        }
    }

    override fun sendMessage(message: ComponentLike) {
        val component = message.asComponent()
        if (component is TextComponent) {
            messageList.add(component.content())
        }
    }

    override fun sendMessage(source: Identified, message: Component, type: MessageType) {
        if (message is TextComponent) {
            messageList.add(message.content())
        }
    }

    override fun sendMessage(source: Identified, message: ComponentLike, type: MessageType) {
        val component = message.asComponent()
        if (component is TextComponent) {
            messageList.add(component.content())
        }
    }

    override fun sendMessage(source: Identity, message: Component, type: MessageType) {
        if (message is TextComponent) {
            messageList.add(message.content())
        }
    }

    override fun sendMessage(source: Identity, message: ComponentLike, type: MessageType) {
        val component = message.asComponent()
        if (component is TextComponent) {
            messageList.add(component.content())
        }
    }

    override fun sendMessage(message: Component, type: MessageType) {
        if (message is TextComponent) {
            messageList.add(message.content())
        }
    }

    override fun sendMessage(message: ComponentLike, type: MessageType) {
        val component = message.asComponent()
        if (component is TextComponent) {
            messageList.add(component.content())
        }
    }

    override fun sendMessage(source: Identified, message: Component) {
        if (message is TextComponent) {
            messageList.add(message.content())
        }
    }

    override fun sendMessage(source: Identified, message: ComponentLike) {
        val component = message.asComponent()
        if (component is TextComponent) {
            messageList.add(component.content())
        }
    }

    override fun sendMessage(source: Identity, message: Component) {
        if (message is TextComponent) {
            messageList.add(message.content())
        }
    }

    override fun sendMessage(source: Identity, message: ComponentLike) {
        val component = message.asComponent()
        if (component is TextComponent) {
            messageList.add(component.content())
        }
    }

    override fun sendRichMessage(message: String) {
        messageList.add(message)
    }

    override fun sendPlainMessage(message: String) {
        messageList.add(message)
    }

    override fun sendMessage(signedMessage: SignedMessage, boundChatType: ChatType.Bound) {
        messageList.add(signedMessage.message())
    }

    override fun sendMessage(message: ComponentLike, boundChatType: ChatType.Bound) {
        val component = message.asComponent()
        if (component is TextComponent) {
            messageList.add(component.content())
        }
    }

    override fun sendMessage(message: Component, boundChatType: ChatType.Bound) {
        if (message is TextComponent) {
            messageList.add(message.content())
        }
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