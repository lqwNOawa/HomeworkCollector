package com.luoqiwen.mirai.homeworkcollector.command

import com.luoqiwen.mirai.homeworkcollector.Plugin
import com.luoqiwen.mirai.homeworkcollector.data.Lang
import com.luoqiwen.mirai.homeworkcollector.interact.UserNotifier
import com.luoqiwen.mirai.homeworkcollector.workinstance.ExamTask
import com.luoqiwen.mirai.homeworkcollector.workinstance.WorkUploadTask
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.MessageChain.Companion.deserializeFromMiraiCode

object SubmitCmdExecutor : CommandExecutor {
    override suspend fun execute(cmd: List<String>, sender: Member, inGroup: Boolean) {
        when (cmd[1]) {
            "start" -> {
                if (isUploading(sender)) {
                    UserNotifier.notifyUser(Lang.Submit_doing.deserializeMiraiCode(), sender, inGroup)
                    return
                }
                val colTsk = Plugin.collector.collectionTaskMap[cmd[2]]
                if (colTsk == null) {
                    UserNotifier.notifyUser(Lang.applyPlaceHolder(
                        Lang.Cmd_argInvalid, cmd[2]
                    ).deserializeMiraiCode(), sender, inGroup)
                    return
                }

                val task = WorkUploadTask(sender, colTsk)
                Plugin.collector.uploadingMap.put(sender.id, task)
                UserNotifier.notifyUser(Lang.applyPlaceHolderList(
                    Lang.Submit_started, Plugin.getAlias(sender), sender.id.toString()
                ), sender, inGroup)
            }
            "over" -> {
                if (!isUploading(sender)) {
                    UserNotifier.notifyUser(Lang.Submit_notDoing.deserializeMiraiCode(), sender, inGroup)
                    return
                }

                Plugin.collector.uploadingMap[sender.id]?.terminate()
            }
            "delay" -> {
                if (isUploading(sender)) {
                    UserNotifier.notifyUser(Lang.Submit_doing.deserializeMiraiCode(), sender, inGroup)
                    return
                }

                val task = Plugin.collector.collectionTaskMap[cmd[2]]
                val day = cmd[3].toIntOrNull()
                val reason = cmd[4]

                if (task == null) {
                    UserNotifier.notifyUser(Lang.applyPlaceHolder(
                        Lang.Cmd_argInvalid, cmd[2]
                    ).deserializeMiraiCode(), sender, inGroup)
                    return
                }
                if (day == null) {
                    UserNotifier.notifyUser(Lang.applyPlaceHolder(
                        Lang.Cmd_argInvalid, cmd[3]
                    ).deserializeMiraiCode(), sender, inGroup)
                    return
                }

                Plugin.collector.delayMap[Pair(sender.id, task)] = day
            }
            "cancel" -> {
                if (cmd.size == 2) {
                    if (isUploading(sender)) {
                        Plugin.collector.uploadingMap[sender.id]?.cancel()
                        UserNotifier.notifyUser(Lang.applyPlaceHolderList(
                            Lang.Submit_cancelled_while, Plugin.getAlias(sender), sender.id.toString()
                        ), sender, inGroup)
                    } else {
                        UserNotifier.notifyUser(Lang.Submit_notDoing.deserializeMiraiCode(), sender, inGroup)
                    }
                } else {
                    assert(cmd.size == 3)
                    val id = cmd[2]
                    val task = ExamTask.getExamTask(id)
                    if (task == null) {
                        UserNotifier.notifyUser(Lang.applyPlaceHolder(
                            Lang.Cmd_argInvalid, id
                        ).deserializeMiraiCode(), sender, inGroup)
                        return
                    }

                    task.cancel()
                    UserNotifier.notifyUser(Lang.applyPlaceHolderList(
                        Lang.Submit_cancelled, Plugin.getAlias(sender), sender.id.toString(), task.task.name
                    ), sender, inGroup)
                }
            }
        }
    }

    override fun getMinArgLength(): Int {
        return 2
    }

    private val arg1List = listOf("start", "over", "delay", "cancel")
    override fun isValid(cmd: List<String>, sender: Member): Boolean {
        if (cmd.size < getMinArgLength() || cmd[0] != "submit" || !arg1List.contains(cmd[1]))
            return false

        return when (cmd[1]) {
            "start" -> cmd.size == 3
            "over" -> cmd.size == 2
            "delay" -> cmd.size == 5
            "cancel" -> cmd.size >= 2
            else -> false
        }
    }

    private fun isUploading(sender: Member) : Boolean {
        return Plugin.collector.uploadingMap.containsKey(sender.id)
    }
}