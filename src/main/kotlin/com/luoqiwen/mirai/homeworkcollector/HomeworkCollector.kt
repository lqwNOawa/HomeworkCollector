package com.luoqiwen.mirai.homeworkcollector

import com.luoqiwen.mirai.homeworkcollector.command.Commands
import com.luoqiwen.mirai.homeworkcollector.data.Lang
import com.luoqiwen.mirai.homeworkcollector.interact.UserNotifier
import com.luoqiwen.mirai.homeworkcollector.workinstance.CollectionTask
import com.luoqiwen.mirai.homeworkcollector.workinstance.ExamTask
import com.luoqiwen.mirai.homeworkcollector.workinstance.WorkUploadTask
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.*
import java.io.*
import java.time.LocalDate

class HomeworkCollector {
    var uploadingMap: MutableMap<Long, WorkUploadTask> = mutableMapOf()//uploading状态
    var examTasks: MutableMap<Long, MutableSet<ExamTask>> = mutableMapOf()
    var collectionTaskMap: MutableMap<String, CollectionTask> = mutableMapOf()//TODO: read and write
    var delayMap: MutableMap<Pair<Long, CollectionTask>, Int> = mutableMapOf()

    fun loadData(folder: File) {
        //TODO
        var uploadingMap_oos: ObjectInputStream? = null
        var examtasks_oos: ObjectInputStream? = null

        try  {
            uploadingMap_oos = ObjectInputStream(
                BufferedInputStream(
                    FileInputStream(
                        File(folder, "uploadings.dat"))))
            examtasks_oos = ObjectInputStream(
                BufferedInputStream(
                    FileInputStream(
                        File(folder, "examtasks.dat"))))

            uploadingMap = uploadingMap_oos.readObject() as MutableMap<Long, WorkUploadTask>
            examTasks = examtasks_oos.readObject() as MutableMap<Long, MutableSet<ExamTask>>

        }
        catch (e: IOException) {
            e.printStackTrace()
        }
        catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
        finally {
            uploadingMap_oos?.close()
            examtasks_oos?.close()
        }
    }

    fun saveData(folder: File) {
        //TODO
        var uploadingMap_oos: ObjectOutputStream? = null
        var examtasks_oos: ObjectOutputStream? = null
        try  {
            uploadingMap_oos = ObjectOutputStream(
                BufferedOutputStream(
                    FileOutputStream(
                        File(folder, "uploadings.dat"), false)))
            examtasks_oos = ObjectOutputStream(
                BufferedOutputStream(
                    FileOutputStream(
                        File(folder, "examtasks.dat"), false)))

            uploadingMap_oos.writeObject(uploadingMap)
            examtasks_oos.writeObject(examTasks)

        }
        catch (e: IOException) {
            e.printStackTrace()
        }
        finally {
            uploadingMap_oos?.close()
            examtasks_oos?.close()
        }
    }

    suspend fun processMsg(msg: MessageChain, member: Member, inGroup: Boolean = false) {
        //start
        if (inGroup)
            if (!isAtMember(msg, member))
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

    fun getRealDeadline(member: Member, task: CollectionTask) : LocalDate {
        val delay = delayMap.getOrDefault(Pair(member.id, task), 0).toLong()
        return task.deadline.plusDays(delay)
    }

    private suspend fun processImg(img: Image, member: Member, inGroup: Boolean) {
        val id = uploadingMap[member.id]?.addImg(img)

        UserNotifier.notifyUser(Lang.applyPlaceHolder(Lang.Img_upload_success, Lang.imgToMiraiCode(img), id.toString()).deserializeMiraiCode(),
            member, inGroup)
    }

    private suspend fun processImg(imgFile: FileMessage, member: Member, inGroup: Boolean) {
       val id = uploadingMap[member.id]?.addImg(imgFile)

       UserNotifier.notifyUser(Lang.applyPlaceHolder(Lang.Img_upload_success, Lang.fileToMiraiCode(imgFile), id.toString()).deserializeMiraiCode(),
            member, inGroup)
    }

    private suspend fun processCmd(cmd: String, member: Member, inGroup: Boolean) {
        if (cmd.startsWith("#")) {
            val args = cmd.substring(1).split(" ")
            if (args.isNotEmpty()) {
                val executor = Commands.valueOf(args[0]).executor
                if (executor.isValid(args, member)) {
                    executor.execute(args, member, inGroup)
                    return
                }
            }
        }

        UserNotifier.notifyUser(Lang.applyPlaceHolder(Lang.Cmd_invalid, cmd).deserializeMiraiCode(), member, inGroup)
        UserNotifier.notifyUser(Lang.applyPlaceHolderList(
            Lang.Help
        ), member, inGroup)
    }

    private fun isAtMember(msg: MessageChain, member: Member) : Boolean {
        for (at in msg.filterIsInstance<At>()) {
            if (at.target == member.id)
                return true
        }

        return false
    }
}