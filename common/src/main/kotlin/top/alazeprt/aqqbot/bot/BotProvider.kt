package top.alazeprt.aqqbot.bot

import top.alazeprt.aonebot.client.websocket.WebsocketBotClient
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.listener.AQBListener
import java.net.URI

object BotProvider {

    private var botClient: WebsocketBotClient? = null

    private var aqbListener: AQBListener? = null

    fun loadBot(plugin: AQQBot, uri: URI) {
        if (aqbListener == null) {
            aqbListener = AQBListener(plugin)
        }
        try {
            val client = WebsocketBotClient(uri)
            client.connect()
            botClient = client
            if (botClient?.eventList?.contains(aqbListener) == false) {
                botClient?.registerEvent(aqbListener)
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to connect to OneBot's websocket server!", e)
        }
    }

    fun loadBot(plugin: AQQBot, uri: URI, token: String) {
        if (aqbListener == null) {
            aqbListener = AQBListener(plugin)
        }
        try {
            val client = WebsocketBotClient(uri, token)
            client.connect()
            botClient = client
            if (botClient?.eventList?.contains(aqbListener) == false) {
                botClient?.registerEvent(aqbListener)
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to connect to OneBot's websocket server!", e)
        }
    }

    fun unloadBot() {
        if (botClient != null && botClient!!.isConnected) {
            botClient!!.disconnect()
            botClient!!.unregisterEvent(aqbListener)
            botClient = null
        }
    }

    fun getBot(): WebsocketBotClient? {
        return botClient
    }
}