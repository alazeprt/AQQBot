package top.alazeprt.aqqbot.util

import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.bot.BotProvider
import top.alazeprt.aqqbot.profile.AOfflinePlayer
import java.io.File
import java.io.IOException
import java.nio.file.*
import java.util.*


class AWeb(val path: File, val width: Int, val height: Int, val delay: Long = 2000, val placeholders: MutableMap<String, String>) {
    fun render(plugin: AQQBot, map: MutableMap<String, String>): ByteArray? {
        try {
            val tmpsFolder = Files.createTempDirectory("aqqbot-web").toFile()
            path.copyRecursively(tmpsFolder, true, map)
            val image = plugin.webDriver.convertToImage(tmpsFolder, width, height, delay)
            tmpsFolder.deleteRecursively()
            return image
        } catch (ignored: Exception) {
            return null
        }
    }

    fun sendToGroup(player: AOfflinePlayer?, groupId: Long, plugin: AQQBot, map: MutableMap<String, String>) {
        val image = render(plugin, map)
        if (image != null) {
            BotProvider.getBot()?.action(SendGroupMessage(groupId,
                "[CQ:image,file=base64://${Base64.getEncoder().encodeToString(image)}]"))
        }
    }

    companion object {
        fun File.copyRecursively(
            target: File,
            overwrite: Boolean = false,
            map: Map<String, String>,
            onError: (File, Throwable) -> OnErrorAction = { f, e -> OnErrorAction.TERMINATE }
        ): Boolean {
            if (!exists()) {
                return onError(this, NoSuchFileException(file = this, reason = "The source file doesn't exist.")) !=
                        OnErrorAction.TERMINATE
            }
            try {
                // We cannot break for loop from inside a lambda, so we have to use an exception here
                for (src in walkTopDown().onFail { f, e -> if (onError(f, e) == OnErrorAction.TERMINATE) throw RuntimeException("Walk failed") }) {
                    if (!src.exists()) {
                        if (onError(src, NoSuchFileException(file = src, reason = "The source file doesn't exist.")) ==
                            OnErrorAction.TERMINATE)
                            return false
                    } else {
                        val relPath = src.toRelativeString(this)
                        val dstFile = File(target, relPath)
                        if (dstFile.exists() && !(src.isDirectory && dstFile.isDirectory)) {
                            val stillExists = if (!overwrite) true else {
                                if (dstFile.isDirectory)
                                    !dstFile.deleteRecursively()
                                else
                                    !dstFile.delete()
                            }

                            if (stillExists) {
                                if (onError(dstFile, FileAlreadyExistsException(file = src,
                                        other = dstFile,
                                        reason = "The destination file already exists.")) == OnErrorAction.TERMINATE)
                                    return false

                                continue
                            }
                        }

                        if (src.isDirectory) {
                            dstFile.mkdirs()
                        } else {
                            if (src.copyTo(dstFile, overwrite).length() != src.length()) {
                                if (onError(src, IOException("Source file wasn't copied completely, length of destination file differs.")) == OnErrorAction.TERMINATE)
                                    return false
                            }
                            var content = dstFile.readText()
                            for ((key, value) in map) {
                                content = content.replace("\${$key}", value)
                            }
                            dstFile.writeText(content)
                        }
                    }
                }
                return true
            } catch (_: Exception) {
                return false
            }
        }
    }
}