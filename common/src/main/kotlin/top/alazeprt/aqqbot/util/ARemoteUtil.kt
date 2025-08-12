package top.alazeprt.aqqbot.util

import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object ARemoteUtil {
    fun downloadToFile(url: String, outputFile: File): File {
        var file = outputFile.apply {
            parentFile?.mkdirs() // 确保目录存在
        }

        if (file.isDirectory) {
            file = File(file, url.split("/").last())
        }

        val connection = createConnection(url)
        connection.inputStream.use { inputStream ->
            validateResponse(connection)
            BufferedInputStream(inputStream).use { bis ->
                FileOutputStream(file).use { fos ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (bis.read(buffer).also { bytesRead = it } != -1) {
                        fos.write(buffer, 0, bytesRead)
                    }
                    fos.flush()
                }
            }
        }

        return file
    }

    // 创建并配置HTTP连接
    private fun createConnection(url: String): HttpURLConnection {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.apply {
            requestMethod = "GET"
            if (url.contains("api.github.com")) setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "Mozilla/5.0")
            connectTimeout = 15_000
            readTimeout = 30_000
            instanceFollowRedirects = true // 跟随重定向
        }
        return connection
    }

    // 验证HTTP响应
    private fun validateResponse(connection: HttpURLConnection) {
        val responseCode = connection.responseCode
        if (responseCode !in 200..299) {
            val errorStream = connection.errorStream?.use {
                it.bufferedReader().readText()
            } ?: "Unknown error"
            throw Exception("HTTP Error $responseCode: ${connection.responseMessage}\n$errorStream")
        }
    }
}