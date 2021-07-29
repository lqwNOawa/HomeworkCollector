package com.luoqiwen.mirai.homeworkcollector.workinstance

import com.luoqiwen.mirai.homeworkcollector.Plugin
import com.luoqiwen.mirai.homeworkcollector.data.Config
import com.luoqiwen.mirai.homeworkcollector.data.Lang
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.message.data.Image

class WorkUploadTask(val member: Member, val task: CollectionTask) {
    init {
        Plugin.launch {
            for (i in 1 until Config.uploadTimeout) {
                if (i.equals(60))
                    suspend {
                        member.sendMessage(Lang.applyPlaceHolder(Lang.Img_upload_timeoutWarn, i.toString()).deserializeMiraiCode())
                    }
                delay(1000)
            }
            cancel()
        }
    }
    val uploadedImages = mutableSetOf<Image>()
    val uploadedFiles = mutableSetOf<FileMessage>()

    fun addImg(img: Image) : Int {
        uploadedImages.add(img)
        return uploadedImages.indexOf(img)
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

    fun toExamTask() : ExamTask {
        return ExamTask(this)
    }

    fun cancel() {
        Plugin.collector.uploadingMap.remove(member.id)
    }

    fun terminate() : ExamTask {
        cancel()
        val examtask = toExamTask()
        (Plugin.collector.examTasks.getOrPut(member.id) { mutableSetOf()}).add(examtask)
        return examtask
    }
}