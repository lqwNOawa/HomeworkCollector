package com.luoqiwen.mirai.homeworkcollector.command

import com.luoqiwen.mirai.homeworkcollector.Plugin
import com.luoqiwen.mirai.homeworkcollector.data.Config
import com.luoqiwen.mirai.homeworkcollector.data.Lang
import com.luoqiwen.mirai.homeworkcollector.interact.UserNotifier
import com.luoqiwen.mirai.homeworkcollector.workinstance.CollectionTask
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object OpenCmdExecutor : CommandExecutor {
    override suspend fun execute(cmd: List<String>, sender: Member, inGroup: Boolean) {
        val name = cmd[1]
        val deadline = LocalDate.parse(cmd[2], DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        if (deadline == null) {
            UserNotifier.notifyUser(Lang.applyPlaceHolder(
                Lang.Cmd_argInvalid, cmd[2]
            ).deserializeMiraiCode(), sender, inGroup)
            return
        }

        val task = CollectionTask(name, deadline)
        task.getDataFolder().mkdirs()
        Plugin.collector.collectionTaskMap[name] = task
        UserNotifier.notifyUser(Lang.applyPlaceHolderList(
            Lang.Task_opened, name, deadline.toString()
        ), sender, inGroup)
    }

    override fun isValid(cmd: List<String>, sender: Member): Boolean {
        return Config.admins.contains(sender.id) && cmd.size >= getMinArgLength() && cmd[0] == "open"
    }

    override fun getMinArgLength(): Int {
        return 3
    }
}