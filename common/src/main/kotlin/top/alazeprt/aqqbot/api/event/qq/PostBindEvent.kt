package top.alazeprt.aqqbot.api.event.qq

import top.alazeprt.aqqbot.api.event.APIEvent
import top.alazeprt.aqqbot.api.event.qq.reason.BindCancelReason

class PostBindEvent(val groupId: Long, val operatorId: Long, val userId: Long, val playerName: String, val isCanceled: Boolean, val reason: BindCancelReason?):
    APIEvent {
    fun getReasonMsg(): String {
        return when (reason) {
            BindCancelReason.ALREADY_BIND -> "该QQ号已绑定其他账号"
            BindCancelReason.VERIFY_CODE_NOT_EXISTS -> "验证码不存在"
            BindCancelReason.INVALID_NAME -> "无效的名字"
            BindCancelReason.ALREADY_EXISTS_NAME -> "名字已被占用"
            BindCancelReason.CANCEL_BY_PLUGIN -> "插件取消"
            else -> "未知原因"
        }
    }
}