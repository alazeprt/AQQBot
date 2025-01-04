package top.alazeprt.aqqbot.command.sender

interface ASender {
    fun getFormatString(): String
    fun getRawString(): String
    fun execute(command: String)
}