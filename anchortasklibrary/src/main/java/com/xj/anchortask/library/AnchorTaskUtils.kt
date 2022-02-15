package com.xj.anchortask.library

import android.app.ActivityManager
import android.content.Context
import android.database.Cursor
import android.os.Process
import android.text.TextUtils
import android.util.Log
import com.xj.anchortask.library.log.LogUtils
import java.io.Closeable
import java.io.FileInputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.set

/**
 * Created by jun xu on 2/1/21.
 */
object AnchorTaskUtils {

    @JvmStatic
    fun getSortResult(
        list: MutableList<AnchorTask>, taskMap: MutableMap<String, AnchorTask>,
        taskChildMap: MutableMap<String, ArrayList<AnchorTask>?>
    ): MutableList<AnchorTask> {
        val result = ArrayList<AnchorTask>()
        // 入度为 0 的队列
        val queue = ArrayDeque<AnchorTask>()
        val taskIntegerHashMap = HashMap<String, Int>()

        // 建立每个 task 的入度关系
        list.forEach { anchorTask: AnchorTask ->
            val taskName = anchorTask.getTaskName()
            if (taskIntegerHashMap.containsKey(taskName)) {
                throw AnchorTaskException("anchorTask is repeat, anchorTask is $anchorTask, list is $list")
            }

            val size = anchorTask.getDependsTaskList()?.size ?: 0
            taskIntegerHashMap[taskName] = size
            taskMap[taskName] = anchorTask
            if (size == 0) {
                queue.offer(anchorTask)
            }
        }

        // 建立每个 task 的 children 关系
        list.forEach { anchorTask: AnchorTask ->
            anchorTask.getDependsTaskList()?.forEach { taskName: String ->
                var children = taskChildMap[taskName]
                if (children == null) {
                    children = ArrayList<AnchorTask>()
                }
                children.add(anchorTask)
                taskChildMap[taskName] = children
                //UI任务不依赖非UI线程任务,否则因为阻塞了ui线程导致anr
                val findTask = findTask(list, taskName)
                if (findTask != null) {
                    if (anchorTask.isRunOnMainThread() && !findTask.isRunOnMainThread()) {
                        throw AnchorTaskException("ui task:$anchorTask dependon non ui task: $findTask")
                    }
                }
            }
        }

        taskChildMap.entries.iterator().forEach {
            LogUtils.d("TAG","key is ${it.key}, value is ${it.value}")
        }


        // 使用 BFS 方法获得有向无环图的拓扑排序
        while (!queue.isEmpty()) {
            val anchorTask = queue.pop()
            result.add(anchorTask)
            val taskName = anchorTask.getTaskName()
            taskChildMap[taskName]?.forEach { // 遍历所有依赖这个顶点的顶点，移除该顶点之后，如果入度为 0，加入到改队列当中
                val key = it.getTaskName()
                var result = taskIntegerHashMap[key] ?: 0
                result--
                if (result == 0) {
                    queue.offer(it)
                }
                taskIntegerHashMap[key] = result
            }
        }

        // size 不相等，证明有环
        if (list.size != result.size) {
            throw AnchorTaskException("Ring appeared，Please check.list is $list, result is $result")
        }

        return result
    }


    @JvmStatic
    private fun findTask(list: MutableList<AnchorTask>, taskName: String): AnchorTask? {
        list.forEach {
            if (TextUtils.equals(it.getTaskName(), taskName)) {
                return it
            }
        }
        return null
    }

    /**
     * Close a [Closeable] object safely.
     *
     * @param closeable Object to close.
     * @return True close successfully, false otherwise.
     */

    @JvmStatic
    fun closeSafely(closeable: Closeable?): Boolean {
        if (closeable == null) {
            return false
        }
        var ret = false
        try {
            closeable.close()
            ret = true
        } catch (e: java.lang.Exception) {
            Log.w("TAG", e)
        }
        return ret
    }

    /**
     * Close a [Cursor] object safely.
     *
     * @param cursor to close cursor
     * [Cursor] is not a [Closeable] until 4.1.1, so we should supply this method to
     * close [Cursor] beside [.closeSafely]
     * @return true is close successfully, false otherwise.
     */

    @JvmStatic
    fun closeSafely(cursor: Cursor?): Boolean {
        if (cursor == null) {
            return false
        }
        var ret = false
        try {
            cursor.close()
            ret = true
        } catch (e: java.lang.Exception) {
            Log.w("TAG", e)
        }
        return ret
    }

    private var sProcessName: String? = null

    @JvmStatic
    fun getCurrProcessName(context: Context?): String? {
        var name: String? = getCurrentProcessNameViaLinuxFile()

        if (TextUtils.isEmpty(name) && context != null) {
            name = getCurrentProcessNameViaActivityManager(context)
        }

        return name
    }

    private fun getCurrentProcessNameViaActivityManager(context: Context?): String? {
        if (context == null) {
            return null
        }
        if (sProcessName != null) {
            return sProcessName
        }
        val pid = Process.myPid()
        val mActivityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            ?: return null
        val processes = mActivityManager.runningAppProcesses ?: return null
        for (appProcess in processes) {
            if (appProcess != null && appProcess.pid == pid) {
                sProcessName = appProcess.processName
                break
            }
        }
        return sProcessName
    }

    @JvmStatic
    fun isInMainProcess(context: Context): Boolean {
        val mainProcessName = context.packageName
        val currentProcessName = getCurrProcessName(context)
        return TextUtils.isEmpty(currentProcessName)    //避免获取进程失败.
                || (mainProcessName != null && mainProcessName.equals(
            currentProcessName,
            ignoreCase = true
        ))
    }

    @JvmStatic
    private fun getCurrentProcessNameViaLinuxFile(): String? {
        val pid = Process.myPid()
        val line = "/proc/$pid/cmdline"
        var fis: FileInputStream? = null
        var processName: String? = null
        val bytes = ByteArray(1024)
        var read = 0
        try {
            fis = FileInputStream(line)
            read = fis.read(bytes)
        } catch (e: Exception) {
            Log.w("TAG", e)
        } finally {
            closeSafely(fis)
        }
        if (read > 0) {
            processName = String(bytes, 0, read)
            processName = processName.trim { it <= ' ' }
        }
        return processName
    }
}