package top.alazeprt.aqqbot.api.webhook

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import top.alazeprt.aconfiguration.file.YamlConfiguration
import top.alazeprt.aonebot.action.GetGroupMemberList
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.bot.BotProvider
import top.alazeprt.aqqbot.util.AFormatter.Companion.validateName
import top.alazeprt.aqqbot.util.APluginInformation
import top.alazeprt.aqqbot.util.LogLevel
import java.io.File
import java.math.BigDecimal
import java.net.InetSocketAddress

class AQQBotWebhookServer(val plugin: AQQBot, private val ip: InetSocketAddress) : WebSocketServer(ip) {
    override fun onOpen(p0: WebSocket?, p1: ClientHandshake?) {
        plugin.log(LogLevel.INFO, "[Webhook] A new webhook connection has been opened: ${p0?.remoteSocketAddress}")
        if (p1?.getFieldValue("Authorization")?.startsWith("Bearer ") != true) {
            p0?.close(1008, "Unauthorized")
            plugin.log(LogLevel.WARN, "[Webhook] The connection ${p0?.remoteSocketAddress} was closed because it isn't authorized.")
            return
        }
        val token = p1.getFieldValue("Authorization")?.removePrefix("Bearer ")
        if (token != plugin.generalConfig.getString("webhook.token", null)) {
            p0?.close(1008, "Unauthorized")
            plugin.log(LogLevel.WARN, "[Webhook] The connection ${p0?.remoteSocketAddress} was closed because it isn't authorized.")
            return
        }
    }

    override fun onClose(p0: WebSocket?, p1: Int, p2: String?, p3: Boolean) {
        plugin.log(LogLevel.INFO, "[Webhook] A webhook connection has been closed: ${p0?.remoteSocketAddress}")
    }

    override fun onMessage(p0: WebSocket?, p1: String?) {
        plugin.debugModule?.debugLogger?.log("[Webhook] Received a message from webhook connection ${p0?.remoteSocketAddress}: $p1")
        try {
            val content = Gson().fromJson(p1, JsonObject::class.java)
            val action = content.get("action").asString
            val echo: String? = if (content.get("echo") == null) null else content.get("echo").asString
            when (action) {
                "/api/v1/server/uuid" -> {
                    val uuid = plugin.serverUUID
                    p0?.send(Gson().toJson(JsonObject().apply {
                        addProperty("uuid", uuid.toString())
                        addProperty("echo", echo)
                    }))
                    return
                }
                "/api/v1/server/name" -> {
                    val name = plugin.generalConfig.getString("webhook.name", null)
                    p0?.send(Gson().toJson(JsonObject().apply {
                        addProperty("name", name)
                        addProperty("echo", echo)
                    }))
                    return
                }
                "/api/v1/server/name/set" -> {
                    val newName = content.get("name").asString
                    plugin.generalConfig.set("webhook.name", newName)
                    plugin.generalConfig.generalConfig.save(File(plugin.getDataFolder(), "config.yml"))
                    p0?.send(Gson().toJson(JsonObject().apply {
                        addProperty("status", "success")
                        addProperty("message", "成功更新服务器名称!")
                        addProperty("echo", echo)
                    }))
                    return
                }
                "/api/v1/server/info" -> {
                    val name = plugin.getBrandName()
                    val version = plugin.getServerVersion()
                    p0?.send(Gson().toJson(JsonObject().apply {
                        addProperty("brand_name", name)
                        addProperty("version", version)
                        addProperty("echo", echo)
                    }))
                    return
                }
                "/api/v1/server/players" -> {
                    val jsonArray = JsonArray()
                    plugin.adapter.getPlayerList().forEach {
                        jsonArray.add(it.getName())
                    }
                    p0?.send(Gson().toJson(JsonObject().apply {
                        add("players", jsonArray)
                        addProperty("echo", echo)
                    }))
                    return
                }
                "/api/v1/server/player" -> {
                    val player = content.get("player").asString
                    val playerInstance = plugin.adapter.getOfflinePlayer(player)
                    val uuid = playerInstance.getUUID().toString()
                    val online = plugin.adapter.getOnlinePlayer(player) != null
                    val ip = if (online) plugin.adapter.getOnlinePlayer(player)!!.getIP() else null
                    p0?.send(Gson().toJson(JsonObject().apply {
                        addProperty("uuid", uuid)
                        addProperty("name", player)
                        addProperty("ip", ip.toString())
                        addProperty("online", online)
                    }))
                    return
                }
                "/api/v1/plugin/info" -> {
                    val pluginInfo = APluginInformation(plugin)
                    p0?.send(Gson().toJson(JsonObject().apply {
                        addProperty("version", pluginInfo.getVersion())
                        addProperty("latest_version", pluginInfo.getLatestVersion())
                        addProperty("latest_commit", pluginInfo.getLatestCommit())
                        addProperty("config_version", pluginInfo.getCurrentConfigVersion())
                        addProperty("plugin_config_version", pluginInfo.getPluginConfigVersion())
                        addProperty("latest_config_version", pluginInfo.getLatestConfigVersion())
                        addProperty("echo", echo)
                    }))
                    return
                }
                "/api/v1/onebot/info" -> {
                    val host = plugin.botConfig.getString("ws.host")
                    val port = plugin.botConfig.getInt("ws.port")
                    val access_token = plugin.botConfig.getString("access_token")
                    p0?.send(Gson().toJson(JsonObject().apply {
                        addProperty("host", host)
                        addProperty("port", port)
                        addProperty("access_token", access_token)
                        addProperty("echo", echo)
                    }))
                    return
                }
                "/api/v1/onebot/set" -> {
                    val host = content.get("host").asString
                    val port = content.get("port").asInt
                    val access_token = content.get("access_token").asString
                    plugin.botConfig.set("ws.host", host)
                    plugin.botConfig.set("ws.port", port)
                    plugin.botConfig.set("access_token", access_token)
                    plugin.botConfig.save(File(plugin.getDataFolder(), "bot.yml"))
                    p0?.send(Gson().toJson(JsonObject().apply {
                        addProperty("status", "success")
                        addProperty("message", "成功更新 OneBot 配置, 重载插件后生效!")
                    }))
                    return
                }
                "/api/v1/onebot/status" -> {
                    p0?.send(Gson().toJson(JsonObject().apply {
                        addProperty("connected", BotProvider.getBot()?.isConnected)
                    }))
                    return
                }
                "/api/v1/enable_groups" -> {
                    val enableGroups = JsonArray()
                    plugin.enableGroups.keys.forEach {
                        enableGroups.add(it)
                    }
                    p0?.send(Gson().toJson(JsonObject().apply {
                        add("groups", enableGroups)
                    }))
                    return
                }
                "/api/v1/enable_groups/add" -> {
                    val newGroup = content.get("group_id").asString
                    if (plugin.enableGroups.containsKey(newGroup)) {
                        p0?.send(Gson().toJson(JsonObject().apply {
                            addProperty("status", "failed")
                            addProperty("message", "群号 $newGroup 已经存在于启用的群组列表中!")
                            addProperty("echo", echo)
                        }))
                    } else {
                        val file = File(plugin.getDataFolder(), "subconfig/$newGroup.yml")
                        if (file.exists()) {
                            plugin.enableGroups[newGroup] = YamlConfiguration.loadConfiguration(file)
                        } else {
                            plugin.enableGroups[newGroup] = null
                        }
                        plugin.botConfig.set("groups", plugin.enableGroups.keys.toList())
                        plugin.botConfig.save(File(plugin.getDataFolder(), "bot.yml"))
                        p0?.send(Gson().toJson(JsonObject().apply {
                            addProperty("status", "success")
                            addProperty("message", "成功添加群号 $newGroup 到启用的群组列表!")
                        }))
                    }
                    return
                }
                "/api/v1/enable_groups/remove" -> {
                    val removeGroup = content.get("group_id").asString
                    if (plugin.enableGroups.containsKey(removeGroup)) {
                        plugin.enableGroups.remove(removeGroup)
                        plugin.botConfig.set("groups", plugin.enableGroups.keys.toList())
                        plugin.botConfig.save(File(plugin.getDataFolder(), "bot.yml"))
                        p0?.send(Gson().toJson(JsonObject().apply {
                            addProperty("status", "success")
                            addProperty("message", "成功从启用的群组列表中移除群号 $removeGroup!")
                        }))
                    } else {
                        p0?.send(Gson().toJson(JsonObject().apply {
                            addProperty("status", "failed")
                            addProperty("message", "群号 $removeGroup 不存在于启用的群组列表中!")
                            addProperty("echo", echo)
                        }))
                    }
                    return
                }
                "/api/v1/enable_groups/users" -> {
                    val groupId = content.get("group_id").asLong
                    BotProvider.getBot()?.action(GetGroupMemberList(groupId)) {
                        val users = JsonObject()
                        it.forEach { user ->
                            users.addProperty(user.member.userId.toString(), user.card)
                        }
                        p0?.send(Gson().toJson(users))
                    }
                    return
                }
                "/api/v1/users" -> {
                    val requestQQ = content.get("qq")
                    val list = mutableListOf<String>()
                    if (requestQQ != null && requestQQ.isJsonArray) {
                        val jsonArray = requestQQ.asJsonArray
                        jsonArray.forEach {
                            list.add(it.asString)
                        }
                    } else if (requestQQ != null && requestQQ.isJsonPrimitive) {
                        list.add(requestQQ.asJsonPrimitive.asString)
                    }
                    val users = plugin.dataProvider.getAllData()
                    val jsonObject = JsonObject()
                    users.forEach { (qq, players) ->
                        if (!list.isEmpty() && !list.contains(qq.toString())) {
                            return@forEach
                        }
                        val jsonArray = JsonArray()
                        players.forEach {
                            jsonArray.add(it.getName())
                        }
                        jsonObject.add(qq.toString(), jsonArray)
                    }
                    jsonObject.addProperty("echo", echo)
                    p0?.send(Gson().toJson(jsonObject))
                    return
                }
                "/api/v1/users/bind" -> {
                    val userId = content.get("qq").asLong
                    val playerName = content.get("player").asString
                    if (plugin.getPlayerByQQ(userId).size >= plugin.generalConfig.getLong("whitelist.max_bind_count", null)) {
                        p0?.send(Gson().toJson(JsonObject().apply {
                            addProperty("status", "failed")
                            addProperty("message", "QQ $userId 已经达到最大绑定数量!")
                            addProperty("echo", echo)
                        }))
                        return
                    }
                    if (!validateName(plugin, playerName, null)) {
                        p0?.send(Gson().toJson(JsonObject().apply {
                            addProperty("status", "failed")
                            addProperty("message", "玩家的名称 $playerName 不合法!")
                            addProperty("echo", echo)
                        }))
                        return
                    }
                    if (plugin.hasPlayer(plugin.adapter.getOfflinePlayer(playerName))) {
                        val exists = plugin.getQQByPlayer(plugin.adapter.getOfflinePlayer(playerName))
                        p0?.send(Gson().toJson(JsonObject().apply {
                            addProperty("status", "failed")
                            addProperty("message", "玩家 $playerName 已经被绑定到 QQ $exists!")
                            addProperty("echo", echo)
                        }))
                        return
                    }
                    plugin.addPlayer(userId, plugin.adapter.getOfflinePlayer(playerName))
                    p0?.send(Gson().toJson(JsonObject().apply {
                        addProperty("status", "success")
                        addProperty("message", "成功绑定 QQ $userId 到玩家 $playerName!")
                        addProperty("echo", echo)
                    }))
                    return
                }
                "/api/v1/users/unbind" -> {
                    val userId = content.get("qq")
                    val playerName = content.get("player")
                    if (userId != null && playerName != null) {
                        if (!plugin.hasQQ(userId.asLong)) {
                            p0?.send(Gson().toJson(JsonObject().apply {
                                addProperty("status", "failed")
                                addProperty("message", "QQ $userId 尚未绑定过!")
                                addProperty("echo", echo)
                            }))
                            return
                        }
                        if ((plugin.getQQByPlayer(plugin.adapter.getOfflinePlayer(playerName.asString))?: -1L) != userId.asLong) {
                            p0?.send(Gson().toJson(JsonObject().apply {
                                addProperty("status", "failed")
                                addProperty("message", "玩家 $playerName 并非 QQ $userId 绑定的玩家!")
                                addProperty("echo", echo)
                            }))
                            return
                        }
                        plugin.removePlayer(userId.asLong, plugin.adapter.getOfflinePlayer(playerName.asString))
                        plugin.submit {
                            plugin.adapter.getPlayerList().forEach {
                                if (it.getName() == playerName.asString) {
                                    it.kick(plugin.messageManager.get("game.kick_when_unbind", null))
                                }
                            }
                        }
                        p0?.send(Gson().toJson(JsonObject().apply {
                            addProperty("status", "success")
                            addProperty("message", "成功解绑 QQ $userId 对应的玩家 $playerName!")
                            addProperty("echo", echo)
                        }))
                        return
                    } else if (playerName != null) {
                        if (plugin.getQQByPlayer(plugin.adapter.getOfflinePlayer(playerName.asString)) == null) {
                            p0?.send(Gson().toJson(JsonObject().apply {
                                addProperty("status", "failed")
                                addProperty("message", "玩家 $playerName 尚未绑定过任何 QQ!")
                                addProperty("echo", echo)
                            }))
                            return
                        }
                        plugin.removePlayer(plugin.adapter.getOfflinePlayer(playerName.asString))
                        plugin.submit {
                            plugin.adapter.getPlayerList().forEach {
                                if (it.getName() == playerName.asString) {
                                    it.kick(plugin.messageManager.get("game.kick_when_unbind", null))
                                }
                            }
                        }
                        p0?.send(Gson().toJson(JsonObject().apply {
                            addProperty("status", "success")
                            addProperty("message", "成功解绑玩家 $playerName!")
                            addProperty("echo", echo)
                        }))
                    } else if (userId != null) {
                        if (!plugin.hasQQ(userId.asLong)) {
                            p0?.send(Gson().toJson(JsonObject().apply {
                                addProperty("status", "failed")
                                addProperty("message", "QQ $userId 尚未绑定过任何玩家!")
                                addProperty("echo", echo)
                            }))
                            return
                        }
                        plugin.removePlayer(userId.asLong)
                        plugin.submit {
                            plugin.adapter.getPlayerList().forEach {
                                if (!plugin.hasPlayer(plugin.adapter.getOfflinePlayer(it.getName()))) {
                                    it.kick(plugin.messageManager.get("game.kick_when_unbind", null))
                                }
                            }
                        }
                        p0?.send(Gson().toJson(JsonObject().apply {
                            addProperty("status", "success")
                            addProperty("message", "成功解绑 QQ $userId 对应的所有玩家!")
                            addProperty("echo", echo)
                        }))
                    } else {
                        p0?.send(Gson().toJson(JsonObject().apply {
                            addProperty("status", "failed")
                            addProperty("message", "缺少参数!")
                            addProperty("echo", echo)
                        }))
                        return
                    }
                }
                "/api/v1/command/execute" -> {
                    val command = content.get("command").asString
                    plugin.submit {
                        plugin.submitCommand(command, plugin.enableGroups.keys.random().toLong()).thenAcceptAsync {
                            p0?.send(Gson().toJson(JsonObject().apply {
                                addProperty("result", it.getFormattedString(null))
                            }))
                        }
                    }
                    return
                }
                "/api/v1/config/get" -> {
                    val key = content.get("key").asString
                    val groupId = content.get("groupId")
                    if (groupId == null) {
                        p0?.send(Gson().toJson(JsonObject().apply {
                            add("value", plugin.generalConfig.getIntoJson(key, null))
                            addProperty("echo", echo)
                        }))
                    }
                    return
                }
                "/api/v1/config/set" -> {
                    val key = content.get("key").asString
                    val value = content.get("value")
                    val groupId = content.get("groupId")
                    val newValue: Any?
                    if (value.isJsonArray) {
                        val tmp = value.asJsonArray
                        if (tmp.size() == 0) {
                            newValue = mutableListOf<String>()
                        } else {
                            val temp = tmp[0].asJsonPrimitive
                            if (temp.isString) {
                                newValue = mutableListOf<String>()
                                tmp.forEach {
                                    newValue.add(it.asString)
                                }
                            } else if (temp.isNumber) {
                                newValue = mutableListOf<Long>()
                                tmp.forEach {
                                    newValue.add(it.asLong)
                                }
                            } else {
                                newValue = mutableListOf<String>()
                                tmp.forEach {
                                    newValue.add(it.asJsonPrimitive.toString())
                                }
                            }
                        }
                    } else if (value.asJsonPrimitive.isString) {
                        newValue = value.asString
                    } else if (value.asJsonPrimitive.isNumber) {
                        val temp = value.asBigDecimal
                        newValue = if (temp.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
                            temp.toDouble()
                        } else {
                            temp.toLong()
                        }
                    } else if (value.asJsonPrimitive.isBoolean) {
                        newValue = value.asBoolean
                    } else {
                        newValue = value.toString()
                    }
                    if (groupId == null) {
                        plugin.generalConfig.set(null, key, newValue)
                    } else {
                        plugin.generalConfig.set(groupId.asLong, key, newValue)
                    }
                    plugin.generalConfig.generalConfig.save(File(plugin.getDataFolder(), "config.yml"))
                    plugin.enableGroups.entries.forEach { (key, value) ->
                        value?.save(File(plugin.getDataFolder(), "subconfig/$key.yml"))
                    }
                    p0?.send(Gson().toJson(JsonObject().apply {
                        addProperty("status", "success")
                        addProperty("message", "成功更新配置项 $key!")
                        addProperty("echo", echo)
                    }))
                    return
                }
                "/api/v1/custom/get" -> {
                    if (content.get("key") == null) {
                        val customCommands = JsonObject()
                        plugin.customCommands.forEach {
                            val command = JsonArray()
                            val execute = JsonArray()
                            val unbindExecute = JsonArray()
                            val output = JsonArray()
                            val unbindOutput = JsonArray()
                            it.command.forEach { a -> command.add(a) }
                            it.execute.forEach { b -> command.add(b) }
                            it.unbind_execute.forEach { b -> command.add(b) }
                            it.output.forEach { c -> output.add(c) }
                            it.unbind_output.forEach { c -> unbindOutput.add(c) }
                            customCommands.add(it.name, JsonObject().apply {
                                addProperty("enable", it.enable)
                                add("command", command)
                                add("execute", execute)
                                add("unbind_execute", unbindExecute)
                                addProperty("choose_account", it.account)
                                add("output", output)
                                add("unbind_output", unbindOutput)
                                addProperty("format", it.format)
                            })
                        }
                        p0?.send(Gson().toJson(JsonObject().apply {
                            add("custom_commands", customCommands)
                            addProperty("echo", echo)
                        }))
                        return
                    }
                    val key = content.get("key").asString
                    var gotten = false
                    plugin.customCommands.forEach {
                        if (it.name == key) {
                            gotten = true
                            val command = JsonArray()
                            val execute = JsonArray()
                            val unbindExecute = JsonArray()
                            val output = JsonArray()
                            val unbindOutput = JsonArray()
                            it.command.forEach { a -> command.add(a) }
                            it.execute.forEach { b -> command.add(b) }
                            it.unbind_execute.forEach { b -> command.add(b) }
                            it.output.forEach { c -> output.add(c) }
                            it.unbind_output.forEach { c -> unbindOutput.add(c) }
                            p0?.send(Gson().toJson(JsonObject().apply {
                                addProperty("enable", it.enable)
                                add("command", command)
                                add("execute", execute)
                                add("unbind_execute", unbindExecute)
                                addProperty("choose_account", it.account)
                                add("output", output)
                                add("unbind_output", unbindOutput)
                                addProperty("format", it.format)
                                addProperty("echo", echo)
                            }))
                        }
                    }
                    if (!gotten) {
                        throw RuntimeException("Custom command $key not found!")
                    }
                    return
                }
                "/api/v1/custom/set" -> {
                    val key = content.get("key").asString
                    val enable = content.get("enable").asBoolean
                    val command = content.get("command").asJsonArray.map { it.asString }.toList()
                    val execute = content.get("execute").asJsonArray.map { it.asString }.toList()
                    val unbind_execute = content.get("unbind_execute").asJsonArray.map { it.asString }.toList()
                    val choose_account = content.get("choose_account").asInt
                    val output = content.get("output").asJsonArray.map { it.asString }.toList()
                    val unbind_output = content.get("unbind_output").asJsonArray.map { it.asString }.toList()
                    val format = content.get("format").asBoolean
                    plugin.customCommands.forEach {
                        if (it.name == key) {
                            it.command = command
                            it.execute = execute
                            it.unbind_execute = unbind_execute
                            it.output = output
                            it.unbind_output = unbind_output
                            it.account = choose_account
                            it.format = format
                        }
                        if (!enable) {
                            plugin.customCommands.remove(it)
                        }
                        return@forEach
                    }
                    plugin.customConfig.set("$key.enable", enable)
                    plugin.customConfig.set("$key.command", command)
                    plugin.customConfig.set("$key.execute", execute)
                    plugin.customConfig.set("$key.unbind_execute", unbind_execute)
                    plugin.customConfig.set("$key.choose_account", choose_account)
                    plugin.customConfig.set("$key.output", output)
                    plugin.customConfig.set("$key.unbind_output", unbind_output)
                    plugin.customConfig.set("$key.format", format)
                    plugin.customConfig.save(File(plugin.getDataFolder(), "custom.yml"))
                    p0?.send(Gson().toJson(JsonObject().apply {
                        addProperty("status", "success")
                        addProperty("message", "成功更新自定义命令 $key!")
                        addProperty("echo", echo)
                    }))
                    return
                }
            }
        } catch (e: JsonSyntaxException) {
            val error = JsonObject()
            error.addProperty("status", "error")
            error.addProperty("message", "无法解析请求的内容, 请求的内容的格式必须为 JSON!")
            p0?.send(Gson().toJson(error))
            return
        } catch (e: RuntimeException) {
            val error = JsonObject()
            error.addProperty("status", "failed")
            error.addProperty("message", e.message)
            p0?.send(Gson().toJson(error))
            return
        } catch (e: Exception) {
            val error = JsonObject()
            error.addProperty("status", "failed")
            error.addProperty("message", "出现了未知错误: ${e.message}")
            p0?.send(Gson().toJson(error))
            return
        }
    }

    override fun onError(p0: WebSocket?, p1: Exception?) {
        plugin.log(LogLevel.ERROR, "[Webhook] Cannot start webhook server: error occurred: $p1")
    }

    override fun onStart() {
        plugin.log(LogLevel.INFO, "[Webhook] Starting webhook server on $ip ...")
    }
}