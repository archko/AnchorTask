package com.xj.anchortask.library

import android.os.Process
import android.os.SystemClock
import android.util.Log

/**
 * Created by jun xu on 2/2/21.
 *
 */
class AnchorTaskRunnable(
    private val anchorProject: AnchorProject,
    private val anchorTask: AnchorTask
) : Runnable {

    override fun run() {
        Process.setThreadPriority(anchorTask.priority())
        //  前置任务没有执行完毕的话，等待，执行完毕的话，往下走
        anchorTask.await()

        val taskState = AnchorTaskState.instance.getTaskState(anchorTask.getTaskName())
        Log.d("TAG", "task state $taskState")
        when (taskState) {
            null,
            AnchorTaskState.State.INIT -> doRun()
            else -> {
                // 通知子任务，当前任务执行完毕了，这里可能出现bug,异步的任务重复初始化,又被依赖了,会有bug.
                anchorProject.setNotifyChildren(anchorTask)
            }
        }
    }

    private fun doRun() {
        // 执行任务
        val startTime = SystemClock.elapsedRealtime()
        AnchorTaskState.instance.setState(anchorTask.getTaskName(), AnchorTaskState.State.RUNNING)
        anchorTask.onStart()
        try {
            anchorTask.run()
            AnchorTaskState.instance.setState(anchorTask.getTaskName(), AnchorTaskState.State.END)
        } catch (e: Exception) {
            AnchorTaskState.instance.setState(
                anchorTask.getTaskName(),
                AnchorTaskState.State.ERROR
            )
        }
        val executeTime = SystemClock.elapsedRealtime() - startTime
        anchorProject.record(anchorTask.getTaskName(), executeTime)
        anchorTask.onFinish()
        // 通知子任务，当前任务执行完毕了，相应的计数器要减一。
        anchorProject.setNotifyChildren(anchorTask)
    }
}