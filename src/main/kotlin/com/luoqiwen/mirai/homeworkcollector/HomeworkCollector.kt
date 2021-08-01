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

package com.luoqiwen.mirai.homeworkcollector

import com.luoqiwen.mirai.homeworkcollector.command.Commands
import com.luoqiwen.mirai.homeworkcollector.data.Config
import com.luoqiwen.mirai.homeworkcollector.data.Lang
import com.luoqiwen.mirai.homeworkcollector.interact.DataSaver
import com.luoqiwen.mirai.homeworkcollector.interact.UserNotifier
import com.luoqiwen.mirai.homeworkcollector.workinstance.CollectionTask
import com.luoqiwen.mirai.homeworkcollector.workinstance.ExamTask
import com.luoqiwen.mirai.homeworkcollector.workinstance.WorkUploadTask
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

class HomeworkCollector {
    var uploadingMap: MutableMap<Long, WorkUploadTask> = mutableMapOf()//uploading状态
    var examTasks: MutableMap<Long, MutableSet<ExamTask>> = mutableMapOf()
    var collectionTaskMap: MutableMap<String, CollectionTask> = mutableMapOf()
    var delayMap: MutableMap<Pair<Long, String>, Int> = mutableMapOf()
    private val timer = Timer("JobTimer")

    fun loadData() {
        uploadingMap = DataSaver.loadUploadingMap()
        examTasks = DataSaver.loadExamTasks()
        collectionTaskMap = DataSaver.loadCollectionTaskMap()
        delayMap = DataSaver.loadDelayMap()
    }

    fun saveData() {
        Plugin.debug("Saving uploadingMap...")
        DataSaver.saveUploadingMap()

        Plugin.debug("Saving ExamTasks")
        DataSaver.saveExamTasks()

        Plugin.debug("Saving CollectionTaskMap")
        DataSaver.saveCollectionTaskMap()

        Plugin.debug("Saving DelayMap")
        DataSaver.saveDelayMap()
    }

    fun launchNotifyTimer() {
        val task = object: TimerTask() {
            override fun run() {
                Plugin.launch {
                    UserNotifier.notifyUsers(Lang.applyPlaceHolder(
                        Lang.Notify_start, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                    ).deserializeMiraiCode())
                    collectionTaskMap.values.forEach { task ->
                        if (task.isExpiringIn(Config.expiringIn, null)) {
                            var deadlineDisplay = task.deadline.toString()
                            val remaining = task.getRemainingDays()
                            deadlineDisplay += if (remaining > 0)
                                "(剩余时间 $remaining 天)"
                            else
                                "(已超时 ${-remaining} 天)"
                            var unfinishedMsg = ""

                            Config.include.forEach { id ->
                                val member = Plugin.group().getMember(id)
                                if (member != null) {
                                    if (task.isExpiringIn(Config.expiringIn, member)) {
                                        unfinishedMsg += "\n"
                                        val delay = task.getDelay(member)
                                        var memberDisplay = At(member).serializeToMiraiCode()
                                        if (delay > 0)
                                            memberDisplay += "(延迟 $delay 天)"
                                        unfinishedMsg += member
                                    }
                                }
                            }

                            UserNotifier.notifyUsers(Lang.applyPlaceHolderList(
                                Lang.Notify_format,
                                task.name,
                                deadlineDisplay,
                                unfinishedMsg
                            ))

                            delay(1000*60*60)

                            UserNotifier.notifyUsers(Lang.applyPlaceHolder(
                                Lang.Notify_expiring,
                                task.name,
                                Config.expiringIn.toString()
                            ).deserializeMiraiCode())
                        } else if (LocalDate.now().isEqual(task.deadline)) {
                            val deadlineDisplay = task.deadline.toString() + "(今日过期)"
                            var unfinishedMsg = ""

                            Config.include.forEach { id ->
                                val member = Plugin.group().getMember(id)
                                if (member != null) {
                                    if (task.isExpired(member)) {
                                        unfinishedMsg += "\n"
                                        val delay = task.getDelay(member)
                                        var memberDisplay = At(member).serializeToMiraiCode()
                                        if (delay > 0)
                                            memberDisplay += "(延迟 $delay 天)"
                                        unfinishedMsg += member
                                    }
                                }
                            }

                            UserNotifier.notifyUsers(Lang.applyPlaceHolderList(
                                Lang.Notify_format,
                                task.name,
                                deadlineDisplay,
                                unfinishedMsg
                            ))
                        }
                    }
                }
            }
        }
        val localDate = LocalDate.now()
        val fixedTime = LocalDateTime.of(localDate.year, localDate.month, localDate.dayOfMonth, 20, 0, 0)
        val fixedDate = Date.from(fixedTime.toInstant(ZoneOffset.UTC))
        timer.schedule(task, fixedDate, 24*60*60*1000)
    }

    fun processMsg(msg: MessageChain, member: Member, inGroup: Boolean = false) {
        //start
        Plugin.debug("MessageChain received, msg: ${msg.serializeToMiraiCode()}, " +
                "member: $member, inGroup: $inGroup")

        if (inGroup)
            if (!isAtBot(msg))
                return

        val mid = member.id

        val imgList = mutableListOf<Image>()
        val fileList = mutableListOf<FileMessage>()
        val textList = mutableListOf<String>()

        if (uploadingMap.containsKey(mid))
        {
            for (element in msg) {
               when (element) {
                   is PlainText -> textList.add(element.content)
                   is Image -> imgList.add(element)
                   is FileMessage -> fileList.add(element)
               }
            }
        }

        else {
            for (element in msg) {
                if (element is PlainText)
                    textList.add(element.content)
            }
        }

        imgList.forEach {
            processImg(it, member, inGroup)
        }

        fileList.forEach {
            processImg(it, member, inGroup)
        }

        textList.forEach {
            processCmd(it, member, inGroup)
        }
    }

    private fun processImg(img: Image, member: Member, inGroup: Boolean) {
        Plugin.launch {
            Plugin.debug("Image process task started: img: ${img.imageId}, member: $member, inGroup: $inGroup")

            uploadingMap[member.id]?.addImg(img)

            UserNotifier.notifyUser(Lang.applyPlaceHolder(
                Lang.Img_upload_success,
                img.serializeToMiraiCode()
            ).deserializeMiraiCode(), member, inGroup)
        }
    }

    private fun processImg(imgFile: FileMessage, member: Member, inGroup: Boolean) {
        Plugin.launch {
            Plugin.debug("Image file process task started: Img: ${imgFile.id}, member: $member, inGroup: $inGroup")

            uploadingMap[member.id]?.addImg(imgFile)

            UserNotifier.notifyUser(Lang.applyPlaceHolder(
                Lang.Img_upload_success,
                imgFile.serializeToMiraiCode()
            ).deserializeMiraiCode(),
                member, inGroup)
        }
    }

    private fun processCmd(cmd: String, member: Member, inGroup: Boolean) {
        Plugin.launch {
            Plugin.debug("Command process task started: Cmd: $cmd, member: $member, inGroup: $inGroup")
            if (cmd.startsWith("#")) {
                val args = cmd.substring(1).split(" ")
                Plugin.debug("Command process task: args: $args")
                if (args.isNotEmpty()) {
                    val executor = Commands.getCommand(args[0]).executor
                    Plugin.debug("Command process task: executor: ${Commands.getCommand(args[0]).name}")
                    if (executor.isValid(args, member)) {
                        Plugin.debug("Command process task: valid cmd, moving to cmd exec stage.")
                        executor.execute(args, member, inGroup)
                        return@launch
                    }
                }
            } else if (cmd.startsWith(" #")) {
                val args = cmd.substring(2).split(" ")
                if (args.isNotEmpty()) {
                    val executor = Commands.getCommand(args[0]).executor
                    if (executor.isValid(args, member)) {
                        executor.execute(args, member, inGroup)
                        return@launch
                    }
                }
            }

            UserNotifier.notifyUser(Lang.applyPlaceHolder(Lang.Cmd_invalid, cmd).deserializeMiraiCode(), member, inGroup)
            UserNotifier.notifyUser(Lang.applyPlaceHolderList(
                Lang.Help
            ), member, inGroup)
        }
    }

    private fun isAtBot(msg: MessageChain) : Boolean {
        for (at in msg.filterIsInstance<At>()) {
            if (at.target == Config.bot)
                return true
        }

        return false
    }
}