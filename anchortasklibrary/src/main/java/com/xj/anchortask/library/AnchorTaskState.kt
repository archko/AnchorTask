package com.xj.anchortask.library

/**
 * 任务的状态,可以避免重复初始化
 * Created by archko on 2/15/22.
 */
class AnchorTaskState {

    internal object LazyHolder {
        var instance = AnchorTaskState()
    }

    enum class State {
        INIT, RUNNING, END, ERROR
    }

    private val taskStateMap: MutableMap<String, State?> = HashMap()

    @Synchronized
    fun setState(name: String, state: State) {
        taskStateMap[name] = state
    }

    @Synchronized
    fun getTaskState(name: String): State? {
        return taskStateMap[name]
    }

    @Synchronized
    fun isTaskEnd(name: String): Boolean {
        val state = taskStateMap[name]
        return state != null && state == State.END
    }

    companion object {
        val instance: AnchorTaskState
            get() = LazyHolder.instance
    }
}