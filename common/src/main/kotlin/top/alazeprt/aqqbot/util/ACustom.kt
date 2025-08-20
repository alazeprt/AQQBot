package top.alazeprt.aqqbot.util

import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.node.NodeType
import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.bot.BotProvider
import top.alazeprt.aqqbot.profile.AOfflinePlayer

abstract class ACustom(val plugin: AQQBot, val name: String, var command: List<String>, var execute: List<String>,
                       var unbind_execute: List<String>, var output: List<String>, var unbind_output: List<String>,
                       var image: AImage?, var unbind_image: AImage?, var web: AWeb?, var unbind_web: AWeb?,
                       var format: Boolean, var account: Int, var enable: Boolean, var permission: String) {
    fun handle(input: String, userId: String, groupId: String): Boolean {
        if (!enable) return false
        val map = matches(input)?: return false
        val player: List<AOfflinePlayer> = plugin.getPlayerByQQ(userId.toLong())
        if (player.isEmpty()) {
            if (permission.isBlank()) return false
            handleUnbind(userId, groupId, map)
        } else {
            return handleBind(userId, groupId, map, player)
        }
        return true
    }

    private fun handleBind(userId: String, groupId: String, map: Map<String, String>, player: List<AOfflinePlayer>): Boolean {
        val choice = player[if (player.size < account) 0 else account - 1]
        val playerName = choice.getName()
        if (plugin.luckperms && permission.isNotBlank()) {
            val user = LuckPermsProvider.get().userManager.loadUser(choice.getUUID()).get()
            val permissions = user.nodes.filter { NodeType.PERMISSION.matches(it) }.filter { it.value == true }.map { it.key }
            if (!permissions.contains(permission)) return false
        }
        var outputString = mapFormat(output.joinToString("\n"), map)
        val imageMap = mutableMapOf<AImageElement, String>()
        image?.elements?.forEach {
            if (it is AImageText) {
                imageMap[it] = mapFormat(it.data, map)
            }
        }
        val executeMap = mutableMapOf<Int, String?>()
        matchesCommand(outputString).forEach {
            executeMap[it] = null
        }
        matchesCommand(imageMap.values.joinToString("\n")).forEach {
            executeMap[it] = null
        }
        var finished = true
        if (execute.isNotEmpty() && execute[0].isNotEmpty()) {
            finished = false
            plugin.submit {
                for (i in execute.indices) {
                    var str = execute[i]
                    if (plugin.getPlayerByQQ(userId.toLong()).isNotEmpty()) {
                        str = str.replace("\$player", plugin.getPlayerByQQ(userId.toLong())[0].getName())
                    }
                    val result = plugin.submitCommand(mapFormat(str, map), groupId.toLong())
                    if (executeMap.containsKey(i+1)) {
                        if (format) {
                            executeMap[i+1] = result.get().getFormattedString(groupId.toLong())
                        } else {
                            executeMap[i+1] = result.get().getRawString()
                        }
                    }
                }
                finished = true
            }
        }
        plugin.submitAsync {
            while (!finished) {
                Thread.sleep(500)
            }
            executeMap.forEach {
                outputString = outputString.replace("\$executes[${it.key}]", it.value?: "")
                imageMap.replaceAll { _, value ->
                    value.replace("\$executes[${it.key}]", it.value?: "")
                }
            }
            outputString = setPlaceholders(plugin.adapter!!.getOfflinePlayer(playerName), outputString)
            if (format) {
                outputString = AFormatter.pluginClear(outputString)
                outputString = AFormatter.chatClear(outputString)
            }
            if (outputString.contains("\$random\n")) {
                val optionsOutput: List<String> = outputString.split("\$random\n")
                val outputList = optionsOutput.random()
                BotProvider.getBot()?.action(SendGroupMessage(groupId.toLong(), outputList))
            } else if (outputString.isNotBlank()) {
                BotProvider.getBot()?.action(SendGroupMessage(groupId.toLong(), outputString))
            }
            imageMap.replaceAll { _, value ->
                var processedValue = setPlaceholders(plugin.adapter!!.getOfflinePlayer(playerName), value)
                if (format) {
                    processedValue = AFormatter.chatClear(processedValue)
                    processedValue = AFormatter.pluginClear(processedValue)
                }
                processedValue
            }
            val map = mutableMapOf<String, String>()
            web?.placeholders?.forEach { key, value ->
                val valueStr = setPlaceholders(plugin.adapter!!.getOfflinePlayer(playerName), value)
                map[key] = valueStr
            }
            web?.sendToGroup(plugin.adapter!!.getOfflinePlayer(playerName), groupId.toLong(), plugin, map)
            var base64 = AImageUtil.getImageBase64(image?.path?: return@submitAsync)
            imageMap.forEach { t, u ->
                if (t is AImageText) {
                    base64 = AImageUtil.addTextToImage(base64, u, t.x, t.y, t.size, t.font, t.color, t.bold, t.italic, plugin)
                }
            }
            BotProvider.getBot()?.action(SendGroupMessage(groupId.toLong(), "[CQ:image,file=base64://$base64]"))
        }
        return true
    }

    fun handleUnbind(userId: String, groupId: String, map: Map<String, String>) {
        var outputString = mapFormat(unbind_output.joinToString("\n"), map)
        val imageMap = mutableMapOf<AImageElement, String>()
        unbind_image?.elements?.forEach {
            if (it is AImageText) {
                imageMap[it] = mapFormat(it.data, map)
            }
        }
        val executeMap = mutableMapOf<Int, String?>()
        matchesCommand(outputString).forEach {
            executeMap[it] = null
        }
        matchesCommand(imageMap.values.joinToString("\n")).forEach {
            executeMap[it] = null
        }
        var finished = true
        if (unbind_execute.isNotEmpty() && unbind_execute[0].isNotEmpty()) {
            finished = false
            plugin.submit {
                for (i in unbind_execute.indices) {
                    var str = unbind_execute[i]
                    if (plugin.getPlayerByQQ(userId.toLong()).isNotEmpty()) {
                        str = str.replace("\$player", plugin.getPlayerByQQ(userId.toLong())[0].getName())
                    }
                    val result = plugin.submitCommand(mapFormat(str, map), groupId.toLong())
                    if (executeMap.containsKey(i+1)) {
                        if (format) {
                            executeMap[i+1] = result.get().getFormattedString(groupId.toLong())
                        } else {
                            executeMap[i+1] = result.get().getRawString()
                        }
                    }
                }
                finished = true
            }
        }
        plugin.submitAsync {
            while (!finished) {
                Thread.sleep(500)
            }
            executeMap.forEach {
                outputString = outputString.replace("\$executes[${it.key}]", it.value?: "")
                imageMap.replaceAll { _, value ->
                    value.replace("\$executes[${it.key}]", it.value?: "")
                }
            }
            outputString = setPlaceholders(null, outputString)
            if (format) {
                outputString = AFormatter.pluginClear(outputString)
                outputString = AFormatter.chatClear(outputString)
            }
            if (outputString.contains("\$random\n")) {
                val optionsOutput: List<String> = outputString.split("\$random\n")
                val outputList = optionsOutput.random()
                BotProvider.getBot()?.action(SendGroupMessage(groupId.toLong(), outputList))
            } else if (outputString.isNotBlank()) {
                BotProvider.getBot()?.action(SendGroupMessage(groupId.toLong(), outputString))
            }
            imageMap.replaceAll { _, value ->
                var processedValue = setPlaceholders(null, value)
                if (format) {
                    processedValue = AFormatter.chatClear(processedValue)
                    processedValue = AFormatter.pluginClear(processedValue)
                }
                processedValue
            }
            val map = mutableMapOf<String, String>()
            unbind_web?.placeholders?.forEach { key, value ->
                val valueStr = setPlaceholders(null, value)
                map[key] = valueStr
            }
            unbind_web?.sendToGroup(null, groupId.toLong(), plugin, map)
            var base64 = AImageUtil.getImageBase64(unbind_image?.path?: return@submitAsync)
            imageMap.forEach { t, u ->
                if (t is AImageText) {
                    base64 = AImageUtil.addTextToImage(base64, u, t.x, t.y, t.size, t.font, t.color, t.bold, t.italic, plugin)
                }
            }
            BotProvider.getBot()?.action(SendGroupMessage(groupId.toLong(), "[CQ:image,file=base64://$base64]"))
        }
    }

    private fun mapFormat(input: String, map: Map<String, String>): String {
        return input.replace(Regex("\\$\\{([^}]+)}")) { match ->
            val key = match.groupValues[1]
            map[key] ?: ""
        }
    }

    private fun matchesCommand(string: String): List<Int> {
        val regex = "\\\$executes\\[([0-9]\\d*)]".toRegex()
        return regex.findAll(string)
            .mapNotNull { it.groupValues[1].toIntOrNull() }
            .toList()
    }

    private fun matches(string: String, commandPattern: String): Map<String, String>? {
        var argsIndex = 0
        val args = mutableMapOf<String, String>()
        commandPattern.split(" ").forEach {
            if (it.startsWith("\${")) {
                if (it.contains("?:")) {
                    args.put(it.split("?:")[0].substring(2), string.split(" ")
                        .getOrElse(argsIndex) { _ -> it.split("?:")[1].substring(0, it.split("?:")[1].length - 1) })
                } else if (it.endsWith("?}")) {
                    args.put(it.substring(2, it.length - 1), string.split(" ")
                        .getOrElse(argsIndex) { _ -> "" })
                } else if (string.split(" ").getOrElse(argsIndex) { _ -> null } != null) {
                    args.put(it.substring(2, it.length - 1), string.split(" ")[argsIndex])
                } else {
                    return null
                }
            } else if (it.startsWith("\$regex:")) {
                val regex = it.substring(8, it.length - 1)
                if (string.split(" ").getOrElse(argsIndex) { _ -> null } != null && string.split(" ")[argsIndex].matches(Regex(regex))) {
                    args.put(it.substring(2, it.length - 1), string.split(" ")[argsIndex])
                } else {
                    return null
                }
            } else if (it == string.split(" ")[argsIndex]) {
                args.put(it, it)
            } else {
                return null
            }
            argsIndex++
        }
        return if (args.size >= string.split(" ").size) {
            args
        } else {
            null
        }
    }

    fun matches(string: String): Map<String, String>? {
        for (commandPattern in command) {
            val matches = matches(string, commandPattern)
            if (matches != null) return matches
        }
        return null
    }

    abstract fun setPlaceholders(player: AOfflinePlayer?, text: String): String
}