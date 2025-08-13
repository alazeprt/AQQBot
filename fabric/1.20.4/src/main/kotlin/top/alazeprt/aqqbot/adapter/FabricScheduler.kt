package top.alazeprt.aqqbot.adapter

import top.alazeprt.aqqbot.util.Cancelable

object FabricScheduler {

    private val taskList: MutableList<FabricCancelable> = mutableListOf()

    fun runTask(task: Runnable): Cancelable {
        task.run()
        return object : Cancelable {
            override fun cancel() {}
        }
    }

    fun runTaskAsync(task: Runnable): Cancelable {
        val thread = Thread(task)
        thread.start()
        val cancelable = FabricCancelable(thread)
        taskList.add(cancelable)
        return cancelable
    }

    fun runTaskLater(task: Runnable, delay: Long): Cancelable {
        Thread.sleep(50 * delay)
        task.run()
        return object : Cancelable {
            override fun cancel() {}
        }
    }

    fun runTaskLaterAsync(task: Runnable, delay: Long): Cancelable {
        val thread = Thread {
            Thread.sleep(50 * delay)
            task.run()
        }
        thread.start()
        val cancelable = FabricCancelable(thread)
        taskList.add(cancelable)
        return cancelable
    }

    fun runTaskTimer(task: Runnable, delay: Long, period: Long): Cancelable {
        Thread.sleep(50 * delay)
        while (true) {
            try {
                task.run()
                Thread.sleep(50 * period)
            } catch (e: InterruptedException) {
                break
            }
        }
        return object : Cancelable {
            override fun cancel() {}
        }
    }

    fun runTaskTimerAsync(task: Runnable, delay: Long, period: Long): Cancelable {
        val thread = Thread {
            Thread.sleep(50 * delay)
            while (!Thread.interrupted()) {
                try {
                    task.run()
                    Thread.sleep(50 * period)
                } catch (e: InterruptedException) {
                    break
                }
            }
        }
        thread.start()
        val cancelable = FabricCancelable(thread)
        taskList.add(cancelable)
        return cancelable
    }

    fun cancelAllTasks() {
        taskList.forEach { it.cancel() }
        taskList.clear()
    }
}