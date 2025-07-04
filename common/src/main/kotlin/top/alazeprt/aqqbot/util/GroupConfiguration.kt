package top.alazeprt.aqqbot.util

import top.alazeprt.aconfiguration.ConfigurationSection
import top.alazeprt.aconfiguration.file.FileConfiguration
import top.alazeprt.aqqbot.AQQBot

class GroupConfiguration(val plugin: AQQBot, val generalConfig: FileConfiguration) {
    fun getString(node: String, group: Long?): String {
        return if (group == null || !plugin.enableGroups.containsKey(group.toString()) ||
            plugin.enableGroups[group.toString()]?.isString(node) != true
        ) {
            generalConfig.getString(node)
        } else {
            plugin.enableGroups[group.toString()]!!.getString(node)
        }
    }
    
    fun getInt(node: String, group: Long?): Int {
        return if (group == null || !plugin.enableGroups.containsKey(group.toString()) ||
            plugin.enableGroups[group.toString()]?.isInt(node) != true) {
            generalConfig.getInt(node)
        } else {
            plugin.enableGroups[group.toString()]!!.getInt(node)
        }
    }
    
    fun getLong(node: String, group: Long?): Long {
        return if (group == null || !plugin.enableGroups.containsKey(group.toString()) ||
            plugin.enableGroups[group.toString()]?.isLong(node) != true) {
            generalConfig.getLong(node)
        } else {
            plugin.enableGroups[group.toString()]!!.getLong(node)
        }
    }
    
    fun getDouble(node: String, group: Long?): Double {
        return if (group == null || !plugin.enableGroups.containsKey(group.toString()) ||
            plugin.enableGroups[group.toString()]?.isDouble(node) != true) {
            generalConfig.getDouble(node)
        } else {
            plugin.enableGroups[group.toString()]!!.getDouble(node)
        }
    }

    fun getBoolean(node: String, group: Long?): Boolean {
        return if (group == null || !plugin.enableGroups.containsKey(group.toString()) ||
            plugin.enableGroups[group.toString()]?.isBoolean(node) != true) {
            generalConfig.getBoolean(node)
        } else {
            plugin.enableGroups[group.toString()]!!.getBoolean(node)
        }
    }

    fun getConfigurationSection(node: String, group: Long?): ConfigurationSection {
        return if (group == null || !plugin.enableGroups.containsKey(group.toString()) ||
            plugin.enableGroups[group.toString()]?.isConfigurationSection(node) != true) {
            generalConfig.getConfigurationSection(node)
        } else {
            plugin.enableGroups[group.toString()]!!.getConfigurationSection(node)
        }
    }

    fun getStringList(node: String, group: Long?): List<String> {
        return if (group == null || !plugin.enableGroups.containsKey(group.toString()) ||
            plugin.enableGroups[group.toString()]?.isList(node) != true) {
            generalConfig.getStringList(node)
        } else {
            plugin.enableGroups[group.toString()]!!.getStringList(node)
        }
    }

    fun set(node: String, value: Any?) {
        generalConfig.set(node, value)
        for (group in plugin.enableGroups.values) {
            group?.set(node, value)
        }
    }

    fun set(group: Long?, node: String, value: Any?) {
        val config = plugin.enableGroups[group.toString()]
        if (config != null) {
            config.set(node, value)
            plugin.enableGroups[group.toString()] = config
        }
    }

    fun setIfNotExists(node: String, value: Any?) {
        if (!generalConfig.contains(node)) {
            generalConfig.set(node, value)
        }
        for (group in plugin.enableGroups.values) {
            if (group != null && !group.contains(node)) {
                group.set(node, value)
            }
        }
    }

    fun getAllString(node: String): List<String> {
        val list = mutableListOf<String>()
        for (config in plugin.enableGroups.values) {
            if (config != null && config.isString(node)) {
                list.add(config.getString(node))
            }
        }
        list.add(generalConfig.getString(node))
        return list
    }

    fun getAllLong(node: String): List<Long> {
        val list = mutableListOf<Long>()
        for (config in plugin.enableGroups.values) {
            if (config != null && config.isLong(node)) {
                list.add(config.getLong(node))
            }
        }
        list.add(generalConfig.getLong(node))
        return list
    }
}