package top.alazeprt.aqqbot.profile

interface APlayer: AOfflinePlayer, ASender {
    fun kick(reason: String)
}