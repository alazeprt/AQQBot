package top.alazeprt.aqqbot.adapter

import top.alazeprt.aqqbot.profile.AOfflinePlayer
import java.nio.charset.Charset
import java.util.*

class BungeeOfflinePlayer(val playerName: String) : AOfflinePlayer {
    override fun getName(): String {
        return playerName
    }

    override fun getUUID(): UUID {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:$playerName").toByteArray(Charset.forName("UTF-8")));
    }
}