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
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.MessageChain
import kotlin.random.Random


object ExamCmdExecutor : CommandExecutor {
    override fun execute(cmd: List<String>, sender: Member, inGroup: Boolean) {
        Plugin.launch {
            when (cmd[1]) {
                // #exam push <arg>
                "pull" -> {
                    if (isExamTaskMapEmpty()) {
                        UserNotifier.notifyUser(Lang.Exam_noUploaded.deserializeMiraiCode(), sender, inGroup)
                        return@launch
                    }

                    // #exam push random
                    if (cmd[2] == "random") {
                        val random = Random(sender.id)
                        val keyIndex = random.nextInt(Plugin.collector.examTasks.size)
                        val key = Plugin.collector.examTasks.keys.elementAt(keyIndex)
                        val valueIndex = random.nextInt(Plugin.collector.examTasks[key]?.size ?: 0)

                        val msg = getTaskMessageChain(sender, ExamTask.generateId(sender, valueIndex))
                        UserNotifier.notifyUser(msg, sender, inGroup)
                    }

                    // #exam pull all
                    else if (cmd[2] == "all") {
                        Plugin.collector.examTasks.forEach { (member, list) ->
                            list.forEach {
                                Plugin.launch {
                                    val msg = getTaskMessageChain(member, it)
                                    UserNotifier.notifyUser(msg, sender, inGroup)
                                }
                            }
                        }
                    }

                    // #exam pull id
                    else {
                        val task = ExamTask.getExamTask(cmd[2])

                        if (task == null) {
                            UserNotifier.notifyUser(Lang.applyPlaceHolder(
                                Lang.Cmd_argInvalid, cmd[2]).deserializeMiraiCode(),
                                sender, inGroup)
                            return@launch
                        }

                        UserNotifier.notifyAdmins(getTaskMessageChain(sender, cmd[2]))
                    }
                }
                // #exam pass <id> <remark>
                "pass" -> {
                    val task = ExamTask.getExamTask(cmd[2])
                    if (task == null) {
                        UserNotifier.notifyUser(Lang.applyPlaceHolder(
                            Lang.Cmd_argInvalid, cmd[2]
                        ).deserializeMiraiCode(), sender, inGroup)
                        return@launch
                    }

                    val remark = cmd.getOrNull(3)
                    task.examSuccess(sender, remark, inGroup)
                    if (!inGroup)
                        task.examSuccess(sender, remark, true)
                }
                // #exam fail <id> <remark>
                "fail" -> {
                    val task = ExamTask.getExamTask(cmd[2])
                    if (task == null) {
                        UserNotifier.notifyUser(Lang.applyPlaceHolder(
                            Lang.Cmd_argInvalid, cmd[2]
                        ).deserializeMiraiCode(), sender, inGroup)
                        return@launch
                    }

                    task.examFail(sender, cmd[3], inGroup)
                    if (!inGroup)
                        task.examFail(sender, cmd[3], true)
                }
                // #exam sudopass <user> <task> <remark>
                "sudopass" -> {
                    val user = Plugin.findTarget(cmd[2])
                    val task = Plugin.collector.collectionTaskMap[cmd[3]]
                    if (user == null) {
                        UserNotifier.notifyUser(Lang.applyPlaceHolder(
                            Lang.Cmd_argInvalid, cmd[2]
                        ).deserializeMiraiCode(), sender, inGroup)
                        return@launch
                    }
                    if (task == null) {
                        UserNotifier.notifyUser(Lang.applyPlaceHolder(
                            Lang.Cmd_argInvalid, cmd[3]
                        ).deserializeMiraiCode(), sender, inGroup)
                        return@launch
                    }

                    val remark = cmd.getOrNull(4) ?: ""
                    task.finish(sender)
                    val msg = Lang.applyPlaceHolderList(
                        Lang.Exam_success,
                        Plugin.getAlias(user),
                        user.id.toString(),
                        TaskStatus.None.lang,
                        task.name,
                        Plugin.getAlias(sender),
                        sender.id.toString(),
                        remark,
                        TaskStatus.Finished.lang
                    )

                    UserNotifier.notifyUser(msg, user, inGroup)
                    if (!inGroup)
                        UserNotifier.notifyUsers(msg)
                }
            }
        }
    }

    private val arg1List = listOf("pull", "pass", "fail", "sudopass")
    override fun isValid(cmd: List<String>, sender: Member): Boolean {
        if (!Config.admins.contains(sender.id) || cmd.size < getMinArgLength() || cmd[0] != "exam" || !arg1List.contains(cmd[1]))
            return false

        return when (cmd[1]) {
            "pull" -> cmd.size == 3
            "pass" -> cmd.size == 3 || cmd.size == 4
            "fail" -> cmd.size == 4
            "sudopass" -> cmd.size == 5
            else -> false
        }
    }

    override fun getMinArgLength(): Int {
        return 3
    }

    private fun getTaskMessageChain(member: Member, id: String) : MessageChain {
        val task = ExamTask.getExamTask(id) ?: return "NoSuchMember:${member.id}".deserializeMiraiCode()

        return getTaskMessageChain(member, task)
    }

    private fun getTaskMessageChain(member: Member, task: ExamTask) : MessageChain {
        var resources = ""
        task.uploadedImages.forEach {
            val formatted = Lang.applyPlaceHolder(
                Lang.Exam_imgFormat,
                it.serializeToMiraiCode(),
                Lang.getMiraiCode(it)
            )
            resources += formatted
            resources += "\n\n"
        }
        task.uploadedFiles.forEach {
            val formatted = Lang.applyPlaceHolder(
                Lang.Exam_imgFormat,
                it.serializeToMiraiCode(),
                Lang.getMiraiCode(it)
            )
            resources += formatted
            resources += "\n\n"
        }
        if (resources.length > 2)
            resources = resources.substring(0, resources.length-2)

        return Lang.applyPlaceHolderList(
            Lang.Exam_task,
            Plugin.getAlias(member),
            member.id.toString(),
            resources,
            task.task.name,
            task.generateId()
        )
    }

    private fun getTaskMessageChain(member: Long, task: ExamTask) : MessageChain {
        val relMember = Plugin.group().getMember(member) ?: return "NoSuchMember:$member".deserializeMiraiCode()
        return getTaskMessageChain(relMember, task)
    }

    private fun isExamTaskMapEmpty() : Boolean {
        val map = Plugin.collector.examTasks
        return if (map.isEmpty())
            true
        else {
            var result = true
            map.values.forEach {
                if (it.isNotEmpty())
                    result = false
            }
            result
        }
    }
}