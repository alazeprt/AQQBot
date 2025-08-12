package top.alazeprt.aqqbot.util

import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.bot.BotProvider
import java.io.File
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import kotlin.io.path.exists
import kotlin.io.path.isDirectory


class AWeb(val path: File, val width: Int, val height: Int, val delay: Long = 2000, val placeholders: Map<String, String>) {
    fun render(plugin: AQQBot): ByteArray? {
        val tmpsFolder = Files.createTempDirectory("aqqbot-web").toFile()
        path.copyRecursively(tmpsFolder, true)
        val image = plugin.webDriver.convertToImage(tmpsFolder, width, height, delay)
        tmpsFolder.deleteRecursively()
        return image
    }

    fun sendToGroup(groupId: Long, plugin: AQQBot) {
        val image = render(plugin)
        if (image != null) {
            BotProvider.getBot()?.action(SendGroupMessage(groupId,
                "[CQ:image,file=base64://${Base64.getEncoder().encodeToString(image)}]"))
        }
    }

    companion object {
        fun generateTmpsFolder(path: File): File {
            val suffix = UUID.randomUUID().toString().substring(0, 8)
            return path.resolve("tmps_$suffix")
        }

        fun Path.copyRecursivelyTo(target: Path, overwrite: Boolean = true) {
            require(this.isDirectory()) { "Source must be a directory" }

            Files.walkFileTree(this, object : SimpleFileVisitor<Path>() {
                override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                    // 在目标位置创建对应的子目录
                    val relative = this@copyRecursivelyTo.relativize(dir)
                    val destDir = target.resolve(relative)
                    Files.createDirectories(destDir)
                    return FileVisitResult.CONTINUE
                }

                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    // 复制文件
                    val relative = this@copyRecursivelyTo.relativize(file)
                    val destFile = target.resolve(relative)

                    when {
                        overwrite && destFile.exists() -> Files.copy(file, destFile, StandardCopyOption.REPLACE_EXISTING)
                        !destFile.exists() -> Files.copy(file, destFile)
                        else -> println("Skipping existing file: $destFile")
                    }
                    return FileVisitResult.CONTINUE
                }
            })
        }
    }
}