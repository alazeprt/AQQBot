package top.alazeprt.aqqbot.util

import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.util.zip.ZipFile

object ACompressUtil {
    fun unzip(file: File, outputDir: File) {
        require(file.exists() && file.isFile) { "Unknown jar file: ${file.absolutePath}" }
        Files.createDirectories(outputDir.toPath())
        require(outputDir.isDirectory) { "Failed to create directory: ${outputDir.absolutePath}" }

        ZipFile(file).use { zip ->
            for (entry in zip.entries()) {
                val targetFile = outputDir.resolve(entry.name).normalize().absoluteFile
                require(targetFile.toPath().startsWith(outputDir.toPath().normalize())) {
                    "Invalid entry name: ${entry.name}"
                }

                if (entry.isDirectory) {
                    targetFile.mkdirs()
                } else {
                    targetFile.parentFile?.mkdirs()

                    zip.getInputStream(entry).use { input ->
                        FileOutputStream(targetFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        }
    }
}