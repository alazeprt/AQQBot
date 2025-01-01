package top.alazeprt.aqqbot.util;

import taboolib.common5.util.replace
import top.alazeprt.aqqbot.AQQBot.messageConfig

object AI18n {
    fun get(key: String): String {
        return messageConfig.getString(key)?: ""
    }

    fun get(key: String, map: Map<String, String>): String {
        var content = messageConfig.getString(key)?: ""
        for ((k, v) in map) {
            content = content.replace("\${$k}", v)
        }
        return content
    }

    fun getList(key: String): String {
        return messageConfig.getStringList(key).joinToString("\n")
    }

    fun getList(key: String, map: Map<String, String>): String {
        var content = messageConfig.getStringList(key).joinToString("\n")
        for ((k, v) in map) {
            content = content.replace("\${$k}", v)
        }
        return content
    }

    fun getOriginList(key: String): List<String> {
        return messageConfig.getStringList(key)
    }

    fun getOriginList(key: String, map: Map<String, String>): MutableList<String> {
        var content = messageConfig.getStringList(key)
        for ((k, v) in map) {
            content = content.replace(Pair("\${$k}", v))
        }
        return content.toMutableList()
    }
}
