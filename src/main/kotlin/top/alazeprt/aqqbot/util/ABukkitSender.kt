package top.alazeprt.aqqbot.util

import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.conversations.Conversation
import org.bukkit.conversations.ConversationAbandonedEvent
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionAttachment
import org.bukkit.permissions.PermissionAttachmentInfo
import org.bukkit.plugin.Plugin
import top.alazeprt.aqqbot.AQQBot.config
import java.util.*

class ABukkitSender : ConsoleCommandSender, ASender {
    private val messageList = mutableListOf<String>()

    override fun isOp(): Boolean {
        return true
    }

    override fun setOp(p0: Boolean) {
        throw UnsupportedOperationException()
    }

    override fun isPermissionSet(p0: String): Boolean {
        return false
    }

    override fun isPermissionSet(p0: Permission): Boolean {
        return false
    }

    override fun hasPermission(p0: String): Boolean {
        return true
    }

    override fun hasPermission(p0: Permission): Boolean {
        return true
    }

    override fun addAttachment(p0: Plugin, p1: String, p2: Boolean): PermissionAttachment {
        messageList.add(p1)
        throw UnsupportedOperationException()
    }

    override fun addAttachment(p0: Plugin): PermissionAttachment {
        throw UnsupportedOperationException()
    }

    override fun addAttachment(p0: Plugin, p1: String, p2: Boolean, p3: Int): PermissionAttachment? {
        messageList.add(p1)
        throw UnsupportedOperationException()
    }

    override fun addAttachment(p0: Plugin, p1: Int): PermissionAttachment? {
        throw UnsupportedOperationException()
    }

    override fun removeAttachment(p0: PermissionAttachment) {
        throw UnsupportedOperationException()
    }

    override fun recalculatePermissions() {
        throw UnsupportedOperationException()
    }

    override fun getEffectivePermissions(): MutableSet<PermissionAttachmentInfo> {
        throw UnsupportedOperationException()
    }

    override fun sendMessage(p0: String) {
        messageList.add(p0)
    }

    override fun sendMessage(vararg p0: String?) {
        for (s in p0) {
            sendMessage(s!!)
        }
    }

    override fun sendMessage(p0: UUID?, p1: String) {
        messageList.add(p1)
    }

    override fun sendMessage(p0: UUID?, vararg p1: String?) {
        for (s in p1) {
            sendMessage(p0, s!!)
        }
    }

    override fun getServer(): Server {
        return Bukkit.getServer()
    }

    override fun getName(): String {
        return "CONSOLE"
    }

    override fun spigot(): CommandSender.Spigot {
        throw UnsupportedOperationException()
    }

    override fun isConversing(): Boolean {
        throw UnsupportedOperationException()
    }

    override fun acceptConversationInput(p0: String) {
        messageList.add(p0)
    }

    override fun beginConversation(p0: Conversation): Boolean {
        throw UnsupportedOperationException()
    }

    override fun abandonConversation(p0: Conversation) {
        throw UnsupportedOperationException()
    }

    override fun abandonConversation(p0: Conversation, p1: ConversationAbandonedEvent) {
        throw UnsupportedOperationException()
    }

    override fun sendRawMessage(p0: String) {
        messageList.add(p0)
    }

    override fun sendRawMessage(p0: UUID?, p1: String) {
        messageList.add(p1)
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
        Bukkit.dispatchCommand(this, command)
    }
}