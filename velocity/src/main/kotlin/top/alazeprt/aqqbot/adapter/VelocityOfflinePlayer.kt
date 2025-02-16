package top.alazeprt.aqqbot.adapter

import com.velocitypowered.api.util.GameProfile
import top.alazeprt.aqqbot.profile.AOfflinePlayer
import java.util.*

class VelocityOfflinePlayer(val profile: GameProfile): AOfflinePlayer {
    override fun getName(): String {
        return profile.name
    }

    override fun getUUID(): UUID {
        return profile.id
    }
}