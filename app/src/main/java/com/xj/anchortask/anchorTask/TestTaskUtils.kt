package com.xj.anchortask.anchorTask

import android.content.Context
import com.xj.anchortask.MyApplication
import com.xj.anchortask.library.AnchorProject
import com.xj.anchortask.library.OnProjectExecuteListener
import com.xj.anchortask.library.TaskExecutorManager
import com.xj.anchortask.library.log.LogUtils
import com.xj.anchortask.library.monitor.OnGetMonitorRecordCallback

/**
 * Created by jun xu on 2/9/21.
 */
object TestTaskUtils {

    fun executeTask(
        context: Context,
        projectExecuteListener: OnProjectExecuteListener? = null,
        onGetMonitorRecordCallback: OnGetMonitorRecordCallback? = null
    ) {
        val project =
            AnchorProject.Builder().setContext(context).setLogLevel(LogUtils.LogLevel.DEBUG)
                .setAnchorTaskCreator(ApplicationAnchorTaskCreator())
                .addTask(TASK_NAME_ZERO)
                .addTask(TASK_NAME_ONE)
                .addTask(TASK_NAME_TWO)
                .addTask(TASK_NAME_THREE).afterTask(
                    TASK_NAME_ZERO,
                    TASK_NAME_ONE
                )
                .addTask(TASK_NAME_FOUR).afterTask(
                    TASK_NAME_ONE,
                    TASK_NAME_TWO
                )
                .addTask(TASK_NAME_FIVE).afterTask(
                    TASK_NAME_THREE,
                    TASK_NAME_FOUR
                )
                .setThreadPoolExecutor(TaskExecutorManager.instance.cpuThreadPoolExecutor)
                .build()
        project.addListener(object : OnProjectExecuteListener {
            override fun onProjectStart() {
                com.xj.anchortask.LogUtils.i(
                    MyApplication.TAG,
                    "onProjectStart "
                )
            }

            override fun onTaskFinish(taskName: String) {
                com.xj.anchortask.LogUtils.i(
                    MyApplication.TAG,
                    "onTaskFinish, taskName is $taskName"
                )
            }

            override fun onProjectFinish() {
                com.xj.anchortask.LogUtils.i(
                    MyApplication.TAG,
                    "onProjectFinish "
                )
            }

        })
        projectExecuteListener?.let {
            project.addListener(it)
        }
        project.onGetMonitorRecordCallback = object : OnGetMonitorRecordCallback {
            override fun onGetTaskExecuteRecord(result: Map<String?, Long?>?) {
                onGetMonitorRecordCallback?.onGetTaskExecuteRecord(result)
            }

            override fun onGetProjectExecuteTime(costTime: Long) {
                onGetMonitorRecordCallback?.onGetProjectExecuteTime(costTime)
            }

        }

        project.start(context).await(1000)
    }

    fun executeTask2(
        context: Context,
        projectExecuteListener: OnProjectExecuteListener? = null,
        onGetMonitorRecordCallback: OnGetMonitorRecordCallback? = null
    ) {

        val anchorTaskZero = AnchorTaskZero()
        val anchorTaskOne = AnchorTaskOne()
        val anchorTaskTwo = AnchorTaskTwo()
        val anchorTaskThree = AnchorTaskThree()
        val anchorTaskFour = AnchorTaskFour()
        val anchorTaskFive = AnchorTaskFive()
        val project =
            AnchorProject.Builder().setContext(context).setLogLevel(LogUtils.LogLevel.DEBUG)
                .setAnchorTaskCreator(ApplicationAnchorTaskCreator())
                .addTask(anchorTaskZero)
                .addTask(anchorTaskOne)
                .addTask(anchorTaskTwo)
                .addTask(anchorTaskThree).afterTask(
                    anchorTaskZero,
                    anchorTaskOne
                )
                .addTask(TASK_NAME_FOUR).afterTask(
                    anchorTaskOne,
                    anchorTaskTwo
                )
                .addTask(TASK_NAME_FIVE).afterTask(
                    anchorTaskThree,
                    anchorTaskFive
                )
                .setThreadPoolExecutor(TaskExecutorManager.instance.cpuThreadPoolExecutor)
                .build()
        project.addListener(object : OnProjectExecuteListener {
            override fun onProjectStart() {
                com.xj.anchortask.LogUtils.i(
                    MyApplication.TAG,
                    "onProjectStart "
                )
            }

            override fun onTaskFinish(taskName: String) {
                com.xj.anchortask.LogUtils.i(
                    MyApplication.TAG,
                    "onTaskFinish, taskName is $taskName"
                )
            }

            override fun onProjectFinish() {
                com.xj.anchortask.LogUtils.i(
                    MyApplication.TAG,
                    "onProjectFinish "
                )
            }

        })
        projectExecuteListener?.let {
            project.addListener(it)
        }
        project.onGetMonitorRecordCallback = object : OnGetMonitorRecordCallback {
            override fun onGetTaskExecuteRecord(result: Map<String?, Long?>?) {
                onGetMonitorRecordCallback?.onGetTaskExecuteRecord(result)
            }

            override fun onGetProjectExecuteTime(costTime: Long) {
                onGetMonitorRecordCallback?.onGetProjectExecuteTime(costTime)
            }

        }

        project.start(context).await(1000)
    }
}