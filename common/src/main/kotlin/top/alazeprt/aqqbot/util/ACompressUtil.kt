package top.alazeprt.aqqbot.util

import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.util.zip.ZipFile

object ACompressUtil {
    fun unzipJar(jarFile: File, outputDir: File) {
        require(jarFile.exists() && jarFile.isFile) { "Unknown jar file: ${jarFile.absolutePath}" }
        Files.createDirectories(outputDir.toPath())
        require(outputDir.isDirectory) { "Failed to create directory: ${outputDir.absolutePath}" }

        ZipFile(jarFile).use { zip ->
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