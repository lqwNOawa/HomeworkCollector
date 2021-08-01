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
import java.time.DateTimeException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object OpenCmdExecutor : CommandExecutor {
    override fun execute(cmd: List<String>, sender: Member, inGroup: Boolean) {
        Plugin.launch {
            Plugin.debug("Executing open cmd: taskName: ${cmd[1]}, date: ${cmd[2]}")
            val name = cmd[1]

            val deadline: LocalDate?

            try {
                Plugin.debug("Open cmd exec: parsing date ${cmd[2]}")
                deadline = LocalDate.parse(cmd[2], DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (e: DateTimeException) {
                Plugin.logger.warning(e)
                Plugin.debug("Parse failed")
                UserNotifier.notifyUser(Lang.applyPlaceHolder(
                    Lang.Cmd_argInvalid, cmd[2]
                ).deserializeMiraiCode(), sender, inGroup)
                return@launch
            }

            Plugin.debug("Parsed successfully")
            val task = CollectionTask(name, deadline)
            Plugin.collector.collectionTaskMap[name] = task
            UserNotifier.notifyUser(Lang.applyPlaceHolderList(
                Lang.Task_opened,
                name,
                deadline.toString(),
                TaskStatus.Operating.lang
            ), sender, inGroup)
            UserNotifier.notifyUsers(Lang.applyPlaceHolderList(
                Lang.Task_opened, name, deadline.toString()
            ), members = Config.include.toLongArray())
        }
    }

    override fun isValid(cmd: List<String>, sender: Member): Boolean {
        return Config.admins.contains(sender.id) && cmd.size >= getMinArgLength() && cmd[0] == "open"
    }

    override fun getMinArgLength(): Int {
        return 3
    }
}