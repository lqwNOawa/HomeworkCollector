package com.luoqiwen.mirai.homeworkcollector.workinstance

import com.luoqiwen.mirai.homeworkcollector.Plugin
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.PlainText

class ExamTask  {
    val member: Member
    var uploadedImages = mutableSetOf<Image>()
    var uploadedFiles = mutableSetOf<FileMessage>()

    constructor(member: Member) {
        this.member = member
    }
    constructor(uploadTask: WorkUploadTask) {
        member = uploadTask.member
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