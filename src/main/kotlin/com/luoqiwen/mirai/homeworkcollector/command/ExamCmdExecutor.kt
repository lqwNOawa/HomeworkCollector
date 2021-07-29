package com.luoqiwen.mirai.homeworkcollector.command

import com.luoqiwen.mirai.homeworkcollector.Plugin
import com.luoqiwen.mirai.homeworkcollector.data.Lang
import com.luoqiwen.mirai.homeworkcollector.interact.UserNotifier
import com.luoqiwen.mirai.homeworkcollector.workinstance.ExamTask
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.MessageChain
import kotlin.random.Random

object ExamCmdExecutor : CommandExecutor {
    override suspend fun execute(cmd: List<String>, sender: Member, inGroup: Boolean) {
        when (cmd[1]) {
            "push" -> {
                if (Plugin.collector.examTasks.isEmpty()) {
                    UserNotifier.notifyUser(Lang.Exam_noUploaded.deserializeMiraiCode(), sender, inGroup)
                    return
                }

                if (cmd[2] == "random") {
                    val random = Random(sender.id)
                    val keyIndex = random.nextInt(Plugin.collector.examTasks.size)
                    val key = Plugin.collector.examTasks.keys.elementAt(keyIndex)
                    val valueIndex = random.nextInt(Plugin.collector.examTasks[key]?.size ?: 0)

                    val msg = getTaskMessageChain(sender, ExamTask.generateId(sender, valueIndex))
                    UserNotifier.notifyAdmins(msg)
                }
                else if (cmd[2] == "all") {
                    Plugin.collector.examTasks.forEach { (member, list) ->
                        list.forEach {
                            Plugin.launch {
                                val msg = getTaskMessageChain(member, it)
                                UserNotifier.notifyAdmins(msg)
                            }
                        }
                    }
                }
                else {
                    val task = ExamTask.getExamTask(cmd[2])

                    if (task == null) {
                        UserNotifier.notifyUser(Lang.applyPlaceHolder(
                            Lang.Cmd_argInvalid, cmd[2]).deserializeMiraiCode(),
                            sender, inGroup)
                        return
                    }

                    UserNotifier.notifyAdmins(getTaskMessageChain(sender, cmd[2]))
                }
            }
            "pass" -> {

            }
            "fail" -> {

            }
        }
    }

    override fun isValid(cmd: List<String>): Boolean {
        if (cmd.size < getMinArgLength())
            return false

        return cmd[0] == "exam" && arg1List.contains(cmd[1])
    }

    override fun getMinArgLength(): Int {
        return 3
    }

    private val arg1List = listOf("push", "pass", "fail")

    private fun getTaskMessageChain(member: Member, id: String) : MessageChain {
        val task = ExamTask.getExamTask(id) ?: return "NoSuchMember:${member.id}".deserializeMiraiCode()

        return getTaskMessageChain(member, task)
    }

    private fun getTaskMessageChain(member: Member, task: ExamTask) : MessageChain {
        var resources = ""
        task.uploadedImages.forEach {
            resources += it.serializeToMiraiCode()
            resources += "\n"
        }
        task.uploadedFiles.forEach {
            resources += it.serializeToMiraiCode()
            resources += "\n"
        }

        return Lang.applyPlaceHolderList(
            Lang.Exam_task,
            Plugin.aliasMap[member.id] ?: member.nick,
            resources,
            task.generateId()
        )
    }

    private fun getTaskMessageChain(member: Long, task: ExamTask) : MessageChain {
        val relMember = Plugin.group.getMember(member) ?: return "NoSuchMember:$member".deserializeMiraiCode()
        return getTaskMessageChain(relMember, task)
    }
}