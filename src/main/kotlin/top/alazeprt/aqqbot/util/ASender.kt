package top.alazeprt.aqqbot.util

interface ASender {
    fun getFormatString(): String
    fun getRawString(): String
    fun execute(command: String)
}