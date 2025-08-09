package top.alazeprt.aqqbot.config

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import top.alazeprt.aconfiguration.file.FileConfiguration
import top.alazeprt.aqqbot.AQQBot

class MessageManager(plugin: AQQBot) {
    private val messageConfig = plugin.messageConfig
    val enableGroups = mutableMapOf<String, FileConfiguration?>()

    fun get(key: String, group: Long?): String {
        if (!enableGroups.containsKey(group.toString()) || enableGroups[group.toString()] == null ||
            (enableGroups[group.toString()]?.isString(key) != true && enableGroups[group.toString()]?.isList(key) != true)) {
            return if (messageConfig.getStringList(key).isEmpty()) messageConfig.getString(key)?: "" else
                messageConfig.getStringList(key).random()?: ""
        } else {
            return if (enableGroups[group.toString()]!!.getStringList(key).isEmpty()) enableGroups[group.toString()]!!.getString(key)?: "" else
                enableGroups[group.toString()]!!.getStringList(key).random()?: ""
        }
    }

    fun get(key: String, map: Map<String, String>, group: Long?): String {
        var content: String
        if (!enableGroups.containsKey(group.toString()) || enableGroups[group.toString()] == null ||
            (enableGroups[group.toString()]?.isString(key) != true && enableGroups[group.toString()]?.isList(key) != true)) {
            content = if (messageConfig.getStringList(key).isEmpty()) messageConfig.getString(key)?: "" else
                messageConfig.getStringList(key).random()?: ""
        } else {
            content = if (enableGroups[group.toString()]!!.getStringList(key).isEmpty()) enableGroups[group.toString()]!!.getString(key)?: "" else
                enableGroups[group.toString()]!!.getStringList(key).random()?: ""
        }
        for ((k, v) in map) {
            content = content.replace("\${$k}", v)
        }
        return content
    }

    fun getAndFormat(key: String, map: Map<String, Component>, group: Long?): Component {
        var content = if (!enableGroups.containsKey(group.toString()) || enableGroups[group.toString()] == null ||
            (enableGroups[group.toString()]?.isString(key) != true && enableGroups[group.toString()]?.isList(key) != true)) {
            if (messageConfig.getStringList(key).isEmpty()) messageConfig.getString(key)?: "" else
                messageConfig.getStringList(key).random()?: ""
        } else {
            if (enableGroups[group.toString()]!!.getStringList(key).isEmpty()) enableGroups[group.toString()]!!.getString(key)?: "" else
                enableGroups[group.toString()]!!.getStringList(key).random()?: ""
        }
        var component = LegacyComponentSerializer.legacy('&').deserialize(content)
        for ((k, v) in map) {
            component = component.replaceText { builder -> builder.matchLiteral("\${$k}").replacement(v) } as TextComponent
        }
        return component
    }

    fun getList(key: String, group: Long?): String {
        return if (!enableGroups.containsKey(group.toString()) || enableGroups[group.toString()] == null ||
            enableGroups[group.toString()]?.isList(key) != true) {
            messageConfig.getStringList(key).joinToString("\n")
        } else {
            enableGroups[group.toString()]!!.getStringList(key).joinToString("\n")
        }
    }

    fun getList(key: String, map: Map<String, String>, group: Long?): String {
        var content = if (!enableGroups.containsKey(group.toString()) || enableGroups[group.toString()] == null ||
            enableGroups[group.toString()]?.isList(key) != true) {
            messageConfig.getStringList(key).joinToString("\n")
        } else {
            enableGroups[group.toString()]!!.getStringList(key).random()?: ""
        }
        for ((k, v) in map) {
            content = content.replace("\${$k}", v)
        }
        return content
    }

    fun getOriginList(key: String, group: Long?): List<String> {
        return if (!enableGroups.containsKey(group.toString()) || enableGroups[group.toString()] == null ||
            enableGroups[group.toString()]?.isList(key) != true) {
            messageConfig.getStringList(key)
        } else {
            enableGroups[group.toString()]!!.getStringList(key)
        }
    }

    fun getOriginList(key: String, map: Map<String, String>, group: Long?): MutableList<String> {
        val content = if (!enableGroups.containsKey(group.toString()) || enableGroups[group.toString()] == null ||
            enableGroups[group.toString()]?.isList(key) != true) {
            messageConfig.getStringList(key)
        } else {
            enableGroups[group.toString()]!!.getStringList(key)
        }
        for ((k, v) in map) {
            content.forEach {
                it.replace("\${$k}", v)
            }
        }
        return content.toMutableList()
    }
}