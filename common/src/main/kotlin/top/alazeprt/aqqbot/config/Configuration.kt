package top.alazeprt.aqqbot.config

interface Configuration {

    fun <T> get(path: String, type: Class<T>): T

    fun getString(path: String): String?

    fun getInt(path: String): Int

    fun getBoolean(path: String): Boolean

    fun getLong(path: String): Long

    fun getFloat(path: String): Float

    fun getDouble(path: String): Double

    fun getStringList(path: String): List<String>

    fun saveToFile()
}