package top.alazeprt.aqqbot.api.event.qq.reason

enum class BindCancelReason {
    ALREADY_BIND, // BIND_LIMIT_REACHED
    VERIFY_CODE_NOT_EXISTS,
    INVALID_NAME,
    ALREADY_EXISTS_NAME,
    CANCEL_BY_PLUGIN,
}