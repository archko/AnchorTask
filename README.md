### fork from https://github.com/gdutxiaoxu/AnchorTask
# 在原来的gdutxiaoxu/AnchorTask修改的地方:
> 去除了多余的测试类,升级了gradle,去除旧的Kotlin库.<br>
> task添加内部的初始化状态,已经初始化过的task不再初始化了,此修改可以在延迟初始化中使用.<br>
> 内部的task,如果是ui线程task依赖了非ui线程的task,异步线程执行时间过长会发生anr.程序内部不允许这种依赖,抛出异常,getSortResult()方法修改.<br>
> 对于进程添加判断,如果当前进程不是目标进程模式,不作初始化操作.<br>

# 已知bug
> 由于调度是在ui线程,所以当非ui线程任务依赖了ui线程任务是正常的,而ui线程的任务,依赖了非ui线程的任务,会有anr的风险.比如在app.oncreate中初始化一部分,另一部分是在ui的Activity同意协议后再作的初始化,这时ui任务依赖了非ui任务,用户操作了ui就会有anr的风险.<br>
> 延迟初始化中,两个非ui任务有依赖,目前这个状态无法处理<br>

使用注意事项:初始化以ui任务为主,异步线程的任务不依赖ui任务.而异步任务间尽量不依赖.ui任务不依赖异步任务.<br>


### ===================================

#  AnchorTask

锚点任务，可以用来解决多线程加载任务依赖的问题。实现原理是使用有向无环图，常见的，比如 Android 启动优化，通常会进行多线程异步加载。

# 基本使用

## 0.1.0 版本

0.1.0 版本使用说明见这里 [AnchorTask 0.1.0 版本使用说明](https://github.com/gdutxiaoxu/AnchorTask/wiki/AnchorTask-0.1.0-%E7%89%88%E6%9C%AC%E4%BD%BF%E7%94%A8%E8%AF%B4%E6%98%8E)， 

0.1.0 版本实现借鉴了 [android-startup](https://github.com/idisfkj/android-startup)，[AppStartFaster](https://github.com/NoEndToLF/AppStartFaster)，[AnchorTask 0.1.0 原理
](https://github.com/gdutxiaoxu/AnchorTask/wiki/AnchorTask-0.1.0-%E5%8E%9F%E7%90%86)

##  1.0.0 版本

[AnchorTask 1.0.0 版本使用说明](https://github.com/gdutxiaoxu/AnchorTask/wiki/AnchorTask-1.0.0-%E7%89%88%E6%9C%AC%E4%BD%BF%E7%94%A8%E8%AF%B4%E6%98%8E)，参考了阿里 [alpha](https://github.com/alibaba/alpha)

[AnchorTask-1.0.0-原理说明](https://github.com/gdutxiaoxu/AnchorTask/wiki/AnchorTask-1.0.0-%E5%8E%9F%E7%90%86%E8%AF%B4%E6%98%8E)

## 两个版本之间区别


1. 之前的 0.1.0 版本 配置前置依赖任务，是通过 `AnchorTask getDependsTaskList` 的方式，这种方式不太直观，1.0.0 放弃了这种方式，参考阿里 `Alpha` 的方式，通过 `addTask(TASK_NAME_THREE).afterTask(TASK_NAME_ZERO, TASK_NAME_ONE)`
2. 1.0.0 版本新增了 Project 类，并增加 `OnProjectExecuteListener` 监听
3. 1.0.0 版本新增 `OnGetMonitorRecordCallback` 监听，方便统计各个任务的耗时


# 实现原理

AnchorTask 的原理不复杂，本质是有向无环图与多线程知识的结合。

1. 根据 BFS 构建出有向无环图，并得到它的拓扑排序
2.  在多线程执行过程中，我们是通过任务的子任务关系和 CounDownLatch 确保先后执行关系的
    1. 前置任务没有执行完毕的话，等待，执行完毕的话，往下走
    2. 执行任务
    3.  通知子任务，当前任务执行完毕了，相应的计数器（入度数）要减一。
    

[Android 启动优化（一） - 有向无环图
](https://juejin.cn/post/6926794003794903048)

[Android 启动优化（二） - 拓扑排序的原理以及解题思路](https://juejin.cn/post/6930805971673415694)



# 特别鸣谢

在实现这个开源框架的时候，借鉴了以下开源框架的思想。AppStartFaster 主要是通过 ClassName 找到相应的 Task，而阿里 alpha 是通过 taskName 找到相应的 Task，并且需要指定 ITaskCreator。两种方式各有优缺点，没有优劣之说，具体看使用场景。

[android-startup](https://github.com/idisfkj/android-startup)

[alpha](https://github.com/alibaba/alpha)

[AppStartFaster](https://github.com/NoEndToLF/AppStartFaster)
