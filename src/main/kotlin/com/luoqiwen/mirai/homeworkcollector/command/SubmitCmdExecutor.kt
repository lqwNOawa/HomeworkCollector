/*
 * HomeworkCollector - Mirai 平台的以自动收集、分发作业以检查为功能的插件(软件)
 *
 * 版权所有（C） 2021-2022 罗棨文
 * 　　本程序为自由软件，在自由软件联盟发布的GNU通用公共许可协议的约束下，你可以对其进行再发布及修改。协议版本为第三版或（随你）更新的版本。
 * 　　我们希望发布的这款程序有用，但不保证，甚至不保证它有经济价值和适合特定用途。详情参见GNU通用公共许可协议。
 * 　　你理当已收到一份GNU通用公共许可协议的副本，如果没有，请查阅<http://www.gnu.org/licenses/>
 *
 * 　　Email: Aluoqiwen@163.com
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * https://github.com/lqwNOawa/HomeworkCollector/blob/master/LICENSE
 */

package com.luoqiwen.mirai.homeworkcollector.command

import com.luoqiwen.mirai.homeworkcollector.Plugin
import com.luoqiwen.mirai.homeworkcollector.data.Config
import com.luoqiwen.mirai.homeworkcollector.data.Lang
import com.luoqiwen.mirai.homeworkcollector.interact.UserNotifier
import com.luoqiwen.mirai.homeworkcollector.workinstance.ExamTask
import com.luoqiwen.mirai.homeworkcollector.workinstance.TaskStatus
import com.luoqiwen.mirai.homeworkcollector.workinstance.WorkUploadTask
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode

object SubmitCmdExecutor : CommandExecutor {
    override fun execute(cmd: List<String>, sender: Member, inGroup: Boolean) {
        Plugin.launch {
            Plugin.debug("Submit cmd exec task started: ${cmd.joinToString(" ")} by ${sender.id}")
            when (cmd[0]) {
                "开始提交" -> taskStartHandler(sender, cmd[1], inGroup)
                "结束提交" -> taskOverHandler(sender, inGroup)
                "犯拖延症" -> taskDelayHandler(sender, cmd[1], cmd[2], cmd[3], inGroup)
                "取消提交" -> taskCancelHandler(sender, cmd.getOrNull(1), inGroup)
                else -> when (cmd[1]) {
                    "start" -> taskStartHandler(sender, cmd[2], inGroup)
                    "over" -> taskOverHandler(sender, inGroup)
                    "delay" -> taskDelayHandler(sender, cmd[2], cmd[3], cmd[4], inGroup)
                    "cancel" -> taskCancelHandler(sender, cmd.getOrNull(2), inGroup)
                }
            }
        }
    }

    private fun taskStartHandler(sender: Member, taskName: String, inGroup: Boolean) {
        Plugin.debug("Submit cmd exec task: START, sender: $sender, task name: $taskName, inGroup: $inGroup")
        if (isUploading(sender)) {
            UserNotifier.notifyUser(Lang.Submit_doing.deserializeMiraiCode(), sender, inGroup)
            return
        }
        val colTsk = Plugin.collector.collectionTaskMap[taskName]
        if (colTsk == null) {
            UserNotifier.notifyUser(Lang.applyPlaceHolder(
                Lang.Cmd_argInvalid, taskName
            ).deserializeMiraiCode(), sender, inGroup)
            return
        }
        if (colTsk.isFinished(sender)) {
            UserNotifier.notifyUser(Lang.applyPlaceHolder(
                Lang.Submit_alreadyFinished, colTsk.name
            ).deserializeMiraiCode(), sender, inGroup)
            return
        }
        if (colTsk.isUnderExam(sender)) {
            UserNotifier.notifyUser(Lang.applyPlaceHolder(
                Lang.Submit_examining
            ).deserializeMiraiCode(), sender, inGroup)
            return
        }

        val task = WorkUploadTask(sender, colTsk)
        Plugin.collector.uploadingMap[sender.id] = task
        task.launchCountDownJob(inGroup)
        UserNotifier.notifyUser(Lang.applyPlaceHolderList(
            Lang.Submit_started,
            Plugin.getAlias(sender),
            sender.id.toString(),
            colTsk.name,
            TaskStatus.None.lang
        ), sender, inGroup)
    }

    private fun taskOverHandler(sender: Member, inGroup: Boolean) {
        Plugin.debug("Submit task exec: OVER, sender: $sender, inGroup: $inGroup")
        if (!isUploading(sender)) {
            UserNotifier.notifyUser(Lang.Submit_notDoing.deserializeMiraiCode(), sender, inGroup)
            return
        }

        val oldTask = Plugin.collector.uploadingMap[sender.id]!!
        val newTask = oldTask.terminate()

        UserNotifier.notifyUser(Lang.applyPlaceHolderList(
            Lang.Submit_success,
            Plugin.getAlias(sender),
            sender.id.toString(),
            newTask.generateId(),
            newTask.task.name,
            TaskStatus.UnderExam.lang
        ), sender, inGroup)
    }

    private fun taskDelayHandler(sender: Member, taskName: String, delay: String, reason: String, inGroup: Boolean) {
        Plugin.debug("Submit cmd exec: DELAY, sender: ${sender.id}, task name: $taskName, delay: $delay, reason: $reason, inGroup: $inGroup")
        if (isUploading(sender)) {
            UserNotifier.notifyUser(Lang.Submit_doing.deserializeMiraiCode(), sender, inGroup)
            return
        }

        val task = Plugin.collector.collectionTaskMap[taskName]
        val requestedDay = delay.toIntOrNull()

        if (task == null) {
            UserNotifier.notifyUser(Lang.applyPlaceHolder(
                Lang.Cmd_argInvalid, taskName
            ).deserializeMiraiCode(), sender, inGroup)
            return
        }
        if (requestedDay == null) {
            UserNotifier.notifyUser(Lang.applyPlaceHolder(
                Lang.Cmd_argInvalid, delay
            ).deserializeMiraiCode(), sender, inGroup)
            return
        }

        val delayedDay = task.getDelay(sender) + requestedDay
        if (delayedDay > Config.maxDelay) {
            val msg = Lang.applyPlaceHolderList(
                Lang.Submit_delay_tooLong,
                Plugin.getAlias(sender),
                sender.id.toString(),
                delayedDay.toString(),
                task.name,
                reason,
                Config.maxDelay.toString()
            )

            UserNotifier.notifyUsers(msg)
            if (!inGroup)
                UserNotifier.notifyUser(msg, sender, false)
        } else {
            Plugin.collector.delayMap[Pair(sender.id, task.name)] = delayedDay
            val msg = Lang.applyPlaceHolderList(
                Lang.Submit_delay_success,
                Plugin.getAlias(sender),
                sender.id.toString(),
                delayedDay.toString(),
                task.name,
                reason
            )

            UserNotifier.notifyUsers(msg)
            if (!inGroup)
                UserNotifier.notifyUser(msg, sender, false)
        }
    }

    private fun taskCancelHandler(sender: Member, id: String?, inGroup: Boolean) {
        Plugin.debug("Submit cmd exec: CANCEL, sender: $sender, id: $id, inGroup: $inGroup")
        if (id == null) {
            if (isUploading(sender)) {
                val task = Plugin.collector.uploadingMap[sender.id]!!
                task.cancel()
                UserNotifier.notifyUser(Lang.applyPlaceHolderList(
                    Lang.Submit_cancelled_while,
                    Plugin.getAlias(sender),
                    sender.id.toString(),
                    task.task.name,
                    TaskStatus.None.lang
                ), sender, inGroup)
            } else {
                UserNotifier.notifyUser(Lang.Submit_notDoing.deserializeMiraiCode(), sender, inGroup)
            }
        } else {
            val task = ExamTask.getExamTask(id)
            if (task == null) {
                UserNotifier.notifyUser(Lang.applyPlaceHolder(
                    Lang.Cmd_argInvalid, id
                ).deserializeMiraiCode(), sender, inGroup)
                return
            }

            task.cancel()
            UserNotifier.notifyUser(Lang.applyPlaceHolderList(
                Lang.Submit_cancelled,
                Plugin.getAlias(sender),
                id,
                sender.id.toString(),
                task.task.name,
                TaskStatus.None.lang
            ), sender, inGroup)
        }
    }

    override fun getMinArgLength(): Int {
        return 1
    }

    override fun isValid(cmd: List<String>, sender: Member): Boolean {
        if (cmd.size < getMinArgLength())
            return false

        return when (cmd[0]) {
            "开始提交" -> cmd.size == 2
            "结束提交" -> cmd.size == 1
            "犯拖延症" -> cmd.size == 4
            "取消提交" -> cmd.size <= 2
            else -> when (cmd[0] + " " + cmd[1]) {
                "submit start" -> cmd.size == 3
                "submit over" -> cmd.size == 2
                "submit delay" -> cmd.size == 5
                "submit cancel" -> cmd.size == 2 || cmd.size == 3
                else -> false
            }
        }
    }

    private fun isUploading(sender: Member) : Boolean {
        return Plugin.collector.uploadingMap.containsKey(sender.id)
    }
}