package top.alazeprt.aqqbot.profile

import java.net.InetSocketAddress

interface APlayer: AOfflinePlayer, ASender {
    fun kick(reason: String)

    fun getIP(): InetSocketAddress?
}