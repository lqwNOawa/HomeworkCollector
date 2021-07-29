package com.luoqiwen.mirai.homeworkcollector.workinstance

import com.luoqiwen.mirai.homeworkcollector.Plugin
import com.luoqiwen.mirai.homeworkcollector.data.Lang
import com.luoqiwen.mirai.homeworkcollector.interact.ImgOperator
import com.luoqiwen.mirai.homeworkcollector.interact.UserNotifier
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.message.data.Image
import java.io.File
import java.nio.file.Files

class ExamTask  {
    val member: Member
    val task: CollectionTask
    var uploadedImages = mutableSetOf<Image>()
    var uploadedFiles = mutableSetOf<FileMessage>()

    constructor(member: Member, task: CollectionTask) {
        this.member = member
        this.task = task
    }
    constructor(uploadTask: WorkUploadTask) {
        member = uploadTask.member
        task = uploadTask.task
        uploadedImages = uploadTask.uploadedImages
        uploadedFiles = uploadTask.uploadedFiles
    }

    fun addImg(image: Image) : Int {
        uploadedImages.add(image)
        return uploadedImages.indexOf(image)
    }

    fun removeImg(image: Image) {
        uploadedImages.remove(image)
    }

    fun addImg(file: FileMessage) : Int {
        uploadedFiles.add(file)
        return uploadedFiles.indexOf(file)
    }

    fun removeImg(file: FileMessage) {
        uploadedFiles.remove(file)
    }

    fun generateId() : String {
        val list = Plugin.collector.examTasks[member.id] ?: return "N/A"
        val index = list.indexOf(this)
        return Companion.generateId(member, index)
    }

    fun cancel() {
        Plugin.collector.examTasks[member.id]?.remove(this)
    }

    suspend fun examSuccess(examiner: Member, remark: String = "") {
        UserNotifier.notifyUser(Lang.applyPlaceHolderList(
            Lang.Exam_success,
            Plugin.getAlias(member),
            member.id.toString(),
            generateId(),
            task.name,
            Plugin.getAlias(examiner),
            examiner.id.toString(),
            remark
        ), member, true)
        Plugin.collector.examTasks[member.id]?.remove(this)
        Files.createFile(File(task.getDataFolder(member), ".finished").toPath())

        uploadedImages.forEach {
            ImgOperator.saveImg(it, task, member)
        }
        uploadedFiles.forEach {
            ImgOperator.saveImg(it, task, member)
        }
    }

    suspend fun examFail(examiner: Member, remark: String = "") {
        UserNotifier.notifyUser(Lang.applyPlaceHolderList(
            Lang.Exam_failed,
            Plugin.getAlias(member),
            member.id.toString(),
            generateId(),
            task.name,
            Plugin.getAlias(examiner),
            examiner.id.toString(),
            remark
        ), member, true)
        Plugin.collector.examTasks[member.id]?.remove(this)
    }

    companion object {
        fun getExamTask(id: String) : ExamTask? {
            if (!id.contains("[") || !id.endsWith("]"))
                return null

            val spilt = id.substring(0, id.length-1).split("[")
            return Plugin.collector.examTasks[spilt[0].toLong()]?.elementAtOrNull(spilt[1].toInt())
        }

        fun generateId(member: Member, index: Int) : String {
            return "${member.id}[$index]"
        }
    }
}