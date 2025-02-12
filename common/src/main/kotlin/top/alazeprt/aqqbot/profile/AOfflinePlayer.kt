package top.alazeprt.aqqbot.profile

import java.util.*

interface AOfflinePlayer: ASender {
    fun getName(): String

    fun getUUID(): UUID
}