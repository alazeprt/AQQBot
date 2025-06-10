package top.alazeprt.aqqbot.adapter

import top.alazeprt.aqqbot.util.Cancelable
import java.util.concurrent.Future

class RunnableTaskCancelable(val task: Future<*>) : Cancelable {
    override fun cancel(mayInterruptIfRunning: Boolean) {
        task.cancel(mayInterruptIfRunning)
    }
}