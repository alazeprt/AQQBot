package top.alazeprt.aqqbot.bot

import top.alazeprt.aonebot.action.GetGroupMemberInfo
import top.alazeprt.aonebot.action.GetGroupMemberList
import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aonebot.action.SendPrivateMessage

object BotManager {
    fun sendGroupMessage(groupId: Long, message: String) {
        BotProvider.getBot()?.action(SendGroupMessage(groupId, message))
    }

    fun sendPrivateMessage(userId: Long, message: String) {
        BotProvider.getBot()?.action(SendPrivateMessage(userId, message))
    }

    fun getGroupMemberList(groupId: Long, function: Any) {
        BotProvider.getBot()?.action(GetGroupMemberList(groupId)) {
            val list = it.map { it.member.userId }
            try {
                val callMethod = function.javaClass.getMethod("call", Any::class.java, Array<Any>::class.java)
                callMethod.invoke(function, null, arrayOf(list.toLongArray()))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getGroupMemberInfo(groupId: Long, userId: Long, function: Any) {
        BotProvider.getBot()?.action(GetGroupMemberInfo(groupId, userId)) {
            try {
                val callMethod = function.javaClass.getMethod("call", Any::class.java, Array<Any>::class.java)
                callMethod.invoke(function, null, it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}