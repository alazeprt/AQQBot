package top.alazeprt.aqqbot.command.sender

import top.alazeprt.aqqbot.util.AFormatter

interface ASender {
    fun getFormatString(): String
    fun getRawString(): String
    fun execute(command: String)

    companion object {
        val formatter = AFormatter()
    }
}