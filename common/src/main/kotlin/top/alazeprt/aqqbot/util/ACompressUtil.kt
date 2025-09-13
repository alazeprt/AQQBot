package top.alazeprt.aqqbot.util

import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.ZipFile

object ACompressUtil {
    fun unzip(file: File, outputDir: File) {
        require(file.exists() && file.isFile) { "Unknown jar file: ${file.absolutePath}" }
        Files.createDirectories(outputDir.toPath())
        require(outputDir.isDirectory) { "Failed to create directory: ${outputDir.absolutePath}" }

        val outputPath = outputDir.toPath().normalize()
        ZipFile(file).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                // 处理条目名称：移除开头的斜杠并替换分隔符
                val entryName = entry.name.replace(Regex("^/"), "").replace('/', File.separatorChar)
                val targetPath = outputPath.resolve(entryName).normalize()

                // 安全检查：确保目标路径在输出目录内
                require(targetPath.startsWith(outputPath)) { "Invalid entry name: ${entry.name}" }

                if (entry.isDirectory) {
                    Files.createDirectories(targetPath)
                } else {
                    Files.createDirectories(targetPath.parent)
                    zip.getInputStream(entry).use { input ->
                        Files.copy(input, targetPath, StandardCopyOption.REPLACE_EXISTING)
                    }
                }
            }
        }
    }
}