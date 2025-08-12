package top.alazeprt.aqqbot.adapter

import top.alazeprt.aqqbot.profile.AOfflinePlayer
import java.util.*

class FabricOfflinePlayer(private val name: String, private val uuid: UUID) : AOfflinePlayer {
    override fun getName(): String {
        return name
    }

    override fun getUUID(): UUID {
        return uuid
    }

    companion object {
        fun from(name: String): FabricOfflinePlayer {
            return FabricOfflinePlayer(name, generateOfflineUuid(name))
        }

        private fun generateOfflineUuid(username: String?): UUID {
            val name = "OfflinePlayer:$username"
            return UUID.nameUUIDFromBytes(name.toByteArray(Charsets.UTF_8))
        }
    }
}