package top.alazeprt.aqqbot.plugins

import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.api.AQQBotAPI
import top.alazeprt.aqqbot.api.event.SubscribeAQQBotEvent
import top.alazeprt.aqqbot.api.event.game.PlayerDeathEvent
import top.alazeprt.aqqbot.api.event.game.PlayerQuitEvent
import top.alazeprt.aqqbot.api.event.game.PluginStartEvent
import top.alazeprt.aqqbot.api.event.game.PluginStopEvent
import top.alazeprt.aqqbot.api.event.game.PostPlayerChatEvent
import top.alazeprt.aqqbot.api.event.game.PostPlayerJoinEvent
import top.alazeprt.aqqbot.api.event.game.PrePlayerChatEvent
import top.alazeprt.aqqbot.api.event.game.PrePlayerJoinEvent
import top.alazeprt.aqqbot.api.event.qq.AGroupRequestEvent
import top.alazeprt.aqqbot.api.event.qq.AMemberJoinEvent
import top.alazeprt.aqqbot.api.event.qq.AMemberLeaveEvent
import top.alazeprt.aqqbot.api.event.qq.PostBindEvent
import top.alazeprt.aqqbot.api.event.qq.PostInformationEvent
import top.alazeprt.aqqbot.api.event.qq.PostRemoteCommandEvent
import top.alazeprt.aqqbot.api.event.qq.PostUnbindEvent
import top.alazeprt.aqqbot.api.event.qq.PreBindEvent
import top.alazeprt.aqqbot.api.event.qq.PreInformationEvent
import top.alazeprt.aqqbot.api.event.qq.PreRemoteCommandEvent
import top.alazeprt.aqqbot.api.event.qq.PreUnbindEvent
import top.alazeprt.aqqbot.api.event.qq.ReceiveMessageEvent
import top.alazeprt.aqqbot.util.LogLevel
import java.lang.reflect.Method

class JSEventManager(val plugin: AQQBot) {
    private val listeners = mutableMapOf<String, MutableList<Any>>()

    fun register(eventName: String, function: Any) {
        listeners.getOrPut(eventName) { mutableListOf() }.add(function)
        plugin.log(LogLevel.INFO, "Registered handler for $eventName")
    }

    fun unregister(eventName: String, function: Any) {
        listeners[eventName]?.remove(function)
    }

    private fun callEvent(eventName: String, instance: Any) {

        val handlers = listeners[eventName] ?: return

        handlers.forEach { function ->
            try {
                val callMethod = function.javaClass.getMethod("call", Any::class.java, Array<Any>::class.java)
                callMethod.invoke(function, null, arrayOf(instance))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    internal fun load() {
        AQQBotAPI.registerEvent(this)
    }

    internal fun unload() {
        AQQBotAPI.unregisterEvent(this)
        listeners.clear()
        plugin.log(LogLevel.INFO, "JS event manager unloaded")
    }

    @SubscribeAQQBotEvent
    fun onPostBind(event: PostBindEvent) = callEvent("PostBindEvent", event)

    @SubscribeAQQBotEvent
    fun onPostUnbind(event: PostUnbindEvent) = callEvent("PostUnbindEvent", event)

    @SubscribeAQQBotEvent
    fun onPostInformation(event: PostInformationEvent) = callEvent("PostInformationEvent", event)

    @SubscribeAQQBotEvent
    fun onPostRemoteCommand(event: PostRemoteCommandEvent) = callEvent("PostRemoteCommandEvent", event)

    @SubscribeAQQBotEvent
    fun onPreBind(event: PreBindEvent) = callEvent("PreBindEvent", event)

    @SubscribeAQQBotEvent
    fun onPreUnbind(event: PreUnbindEvent) = callEvent("PreUnbindEvent", event)

    @SubscribeAQQBotEvent
    fun onPreInformation(event: PreInformationEvent) = callEvent("PreInformationEvent", event)

    @SubscribeAQQBotEvent
    fun onPreRemoteCommand(event: PreRemoteCommandEvent) = callEvent("PreRemoteCommandEvent", event)

    @SubscribeAQQBotEvent
    fun onReceiveMessage(event: ReceiveMessageEvent) = callEvent("ReceiveMessageEvent", event)

    @SubscribeAQQBotEvent
    fun onPrePlayerJoin(event: PrePlayerJoinEvent) = callEvent("PrePlayerJoinEvent", event)

    @SubscribeAQQBotEvent
    fun onPostPlayerJoin(event: PostPlayerJoinEvent) = callEvent("PostPlayerJoinEvent", event)

    @SubscribeAQQBotEvent
    fun onPrePlayerChat(event: PrePlayerChatEvent) = callEvent("PrePlayerChatEvent", event)

    @SubscribeAQQBotEvent
    fun onPostPlayerChat(event: PostPlayerChatEvent) = callEvent("PostPlayerChatEvent", event)

    @SubscribeAQQBotEvent
    fun onPlayerQuit(event: PlayerQuitEvent) = callEvent("PlayerQuitEvent", event)

    @SubscribeAQQBotEvent
    fun onPlayerDeath(event: PlayerDeathEvent) = callEvent("PlayerDeathEvent", event)

    @SubscribeAQQBotEvent
    fun onPluginStart(event: PluginStartEvent) = callEvent("PluginStartEvent", event)

    @SubscribeAQQBotEvent
    fun onPluginStop(event: PluginStopEvent) = callEvent("PluginStopEvent", event)

    @SubscribeAQQBotEvent
    fun onGroupRequest(event: AGroupRequestEvent) = callEvent("GroupRequestEvent", event)

    @SubscribeAQQBotEvent
    fun onMemberIncrease(event: AMemberJoinEvent) = callEvent("GroupMemberIncreaseEvent", event)

    @SubscribeAQQBotEvent
    fun onMemberDecrease(event: AMemberLeaveEvent) = callEvent("GroupMemberDecreaseEvent", event)
}