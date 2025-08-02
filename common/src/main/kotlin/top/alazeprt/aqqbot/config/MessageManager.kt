package top.alazeprt.aqqbot.config

import top.alazeprt.aconfiguration.file.FileConfiguration
import top.alazeprt.aqqbot.AQQBot

class MessageManager(plugin: AQQBot) {
    private val messageConfig = plugin.messageConfig
    val enableGroups = mutableMapOf<String, FileConfiguration?>()

    fun get(key: String, group: Long?, map: Map<String, String> = mapOf()): String
    {
        val result =
            enableGroups[group.toString()]?.let {
                if (it.isString(key) || it.isList(key))
                {
                    if (it.getStringList(key).isEmpty()) return@let it.getString(key) ?: ""
                    else return@let it.getStringList(key).random() ?: ""
                }
                else return@let null
            }

        val content =
            result ?: if (messageConfig.getStringList(key).isEmpty()) messageConfig.getString(key) ?: ""
            else messageConfig.getStringList(key).random() ?: ""

        return content.apply {
            map.forEach {
                this.replace("\${$it.key}", it.value)
            }
        }
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