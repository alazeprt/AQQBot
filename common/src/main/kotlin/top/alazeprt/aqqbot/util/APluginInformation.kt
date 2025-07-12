package top.alazeprt.aqqbot.util

import com.google.common.io.Resources.getResource
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import top.alazeprt.aconfiguration.file.YamlConfiguration
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.bot.BotProvider
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64
import java.util.jar.Manifest

object APluginInformation {
    fun getVersion(): String {
        return this::class.java.`package`.implementationVersion ?: "Unknown"
    }

    fun getAuthor(): String {
        return this::class.java.`package`.implementationVendor ?: "Unknown"
    }

    fun getWebsite(): String {
        return getAttribute("Implementation-Website")
    }

    private fun getAttribute(name: String): String {
        return this::class.java.classLoader.getResourceAsStream("META-INF/MANIFEST.MF")?.use {
            Manifest(it).mainAttributes.getValue(name)
        }?: "Unknown"
    }

    fun getLatestVersion(): String {
        return try {
            val url = URL("https://api.github.com/repos/alazeprt/AQQBot/releases/latest")
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "AQQBot")
                connectTimeout = 5000
                readTimeout = 5000
            }
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return "Unknown"
            }
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = reader.use { it.readText() }
            val jsonObject = Gson().fromJson(response, JsonObject::class.java)
            jsonObject.get("tag_name").asString ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    fun getLatestCommit(): String {
        return try {
            val url = URL("https://api.github.com/repos/alazeprt/AQQBot/commits")
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "AQQBot")
                connectTimeout = 5000
                readTimeout = 5000
            }
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                "Unknown"
            }
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = reader.use { it.readText() }
            val jsonArray = Gson().fromJson(response, JsonArray::class.java)
            jsonArray[0].asJsonObject.get("sha").asString.substring(0, 7)
        } catch (e: Exception) {
            "Unknown"
        }
    }

    fun getCurrentConfigVersion(plugin: AQQBot): String {
        return plugin.generalConfig.getInt("version", null).toString()
    }

    fun getPluginConfigVersion(): String {
        try {
            val inputStream = javaClass.classLoader.getResourceAsStream("config.yml") ?: return "Unknown"
            val yaml = YamlConfiguration.loadConfiguration(inputStream)
            return yaml.getString("version") ?: "Unknown"
        } catch (e: Exception) {
            return "Unknown"
        }
    }

    fun getLatestConfigVersion(): String {
        return try {
            val url = URL("https://raw.githubusercontent.com/alazeprt/AQQBot/refactor/common/src/main/resources/config.yml")
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "AQQBot")
                connectTimeout = 5000
                readTimeout = 5000
            }
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                "Unknown"
            }
            val reader = InputStreamReader(connection.inputStream)
            val yaml = YamlConfiguration.loadConfiguration(reader)
            yaml.getString("version") ?: "Unknown"
        } catch (e: Exception) {
            e.printStackTrace()
            "Unknown"
        }
    }

    fun getWebsocketStatus(): String {
        return if (BotProvider.getBot()?.isConnected == true) {
            "§a正常"
        } else {
            "§c断开"
        }
    }
}