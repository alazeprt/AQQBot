package top.alazeprt.aqqbot.util

interface Cancelable {
    fun cancel(mayInterruptIfRunning: Boolean)
}