package top.alazeprt.aqqbot.api.events

class BindEvent(val userId: Long, val executorId: Long, val groupId: Long, val playerName: String, val isHandle: Boolean): AQBEventInterface {
    override fun handle() {}
}