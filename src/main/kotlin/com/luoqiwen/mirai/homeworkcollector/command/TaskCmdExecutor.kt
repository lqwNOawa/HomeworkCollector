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
import com.luoqiwen.mirai.homeworkcollector.workinstance.CollectionTask
import com.luoqiwen.mirai.homeworkcollector.workinstance.TaskStatus
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode

object TaskCmdExecutor : CommandExecutor {
    override fun execute(cmd: List<String>, sender: Member, inGroup: Boolean) {
        Plugin.launch {
            if (cmd[1] == "init") {
                UserNotifier.notifyUsers(Lang.applyPlaceHolderList(
                    Lang.Report_init
                ), members = Config.include.toLongArray())
                UserNotifier.notifyUsers(Lang.applyPlaceHolderList(
                    Lang.Help
                ), members = Config.include.toLongArray())
            }
            else if (cmd[1] == "list") {
                if (Plugin.collector.collectionTaskMap.isEmpty()) {
                    UserNotifier.notifyUser(Lang.applyPlaceHolder(
                        Lang.Report_empty
                    ).deserializeMiraiCode(), sender, inGroup)
                } else {
                    Plugin.collector.collectionTaskMap.forEach { (name, task) ->
                        UserNotifier.notifyUser(Lang.applyPlaceHolderList(
                            Lang.Report_list,
                            name,
                            task.deadline.toString(),
                            task.getTaskStatus().lang
                        ), sender, inGroup)
                    }
                }
            } else if (cmd[1] == "remove") {
                val taskName = cmd[2]
                if (!Plugin.collector.collectionTaskMap.containsKey(taskName))
                    UserNotifier.notifyUser(Lang.applyPlaceHolder(
                        Lang.Cmd_argInvalid, cmd[2]
                    ).deserializeMiraiCode(), sender, inGroup)
                else {
                    val task = Plugin.collector.collectionTaskMap[taskName]!!
                    task.remove()
                    UserNotifier.notifyUser(Lang.applyPlaceHolder(
                        Lang.Report_remove_success, taskName
                    ).deserializeMiraiCode(), sender, inGroup)
                }

            } else {
                if (cmd[1] == "all" && cmd[2] != "all") {
                    val task = Plugin.collector.collectionTaskMap[cmd[2]]
                    if (task == null) {
                        UserNotifier.notifyUsers(Lang.applyPlaceHolder(
                            Lang.Cmd_argInvalid, cmd[2]
                        ).deserializeMiraiCode(), members = Config.include.toLongArray())
                        return@launch
                    }
                    Config.include.forEach {
                        targetedReport(it.toString(), task, sender, inGroup)
                    }
                } else if (cmd[1] != "all" && cmd[2] == "all") {
                    val target = Plugin.findTarget(cmd[1])
                    if (target == null) {
                        UserNotifier.notifyUsers(Lang.applyPlaceHolder(
                            Lang.Cmd_argInvalid, cmd[1]
                        ).deserializeMiraiCode(), members = Config.include.toLongArray())
                        return@launch
                    }
                    Plugin.collector.collectionTaskMap.values.forEach {
                        targetedReport(target, it, sender, inGroup)
                    }
                } else if (cmd[1] == "all" && cmd[2] == "all") {
                    if (Plugin.collector.collectionTaskMap.isEmpty()) {
                        UserNotifier.notifyUser(Lang.applyPlaceHolder(
                            Lang.Report_empty,
                        ).deserializeMiraiCode(), sender, inGroup)
                    }
                    Config.include.forEach { member ->
                        Plugin.collector.collectionTaskMap.values.forEach { task ->
                            targetedReport(member.toString(), task, sender, inGroup)
                        }
                    }
                }
            }
        }
    }

    private fun targetedReport(name: String, task: CollectionTask, sender: Member, inGroup: Boolean) {
        val member = Plugin.findTarget(name)
        if (member == null) {
            UserNotifier.notifyUser(Lang.applyPlaceHolder(
                Lang.Cmd_argInvalid,
                name
            ).deserializeMiraiCode(), sender, inGroup)
            return
        }

        return targetedReport(member, task, sender, inGroup)
    }

    private fun targetedReport(member: Member, task: CollectionTask, sender: Member, inGroup: Boolean) {
        val deadline = task.getRealDeadline(member)
        val finished = task.isFinished(member)
        val examTask = task.getExamTask(member)
        var deadlineDisplay = deadline.toString()
        val remaining = task.getRemainingDays()
        val status: TaskStatus
        var taskId = "N/A"

        if (!finished) {
            deadlineDisplay += "(剩余时间: $remaining 天)"
            if (task.isUnderExam(member)) {
                status = TaskStatus.UnderExam
                taskId = examTask!!.generateId()
            }
            else if (remaining < 0)
                status = TaskStatus.TimedOut
            else
                status = TaskStatus.Unfinished
        } else
            status = TaskStatus.Finished

        UserNotifier.notifyUser(Lang.applyPlaceHolderList(
            Lang.Report_status,
            Plugin.getAlias(member),
            member.id.toString(),
            taskId,
            task.name,
            deadlineDisplay,
            status.lang
        ), sender, inGroup)
    }


    override fun isValid(cmd: List<String>, sender: Member): Boolean {
        if (cmd.size < getMinArgLength() || cmd[0] != "task")
            return false

        return if (cmd[1] == "init" || cmd[1] == "list")
            cmd.size == 2
        else
            cmd.size == 3
    }

    override fun getMinArgLength(): Int {
        return 2
    }
}