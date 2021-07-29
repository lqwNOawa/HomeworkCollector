package com.luoqiwen.mirai.homeworkcollector.data

import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.Face
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain

object Lang: ReadOnlyPluginConfig("lang") {
    val Help: List<String> by value(listOf(
        "",
        "",
        ""
    ))
    val Cmd_invalid: String by value("指令 $0 无法识别")
    val Cmd_argInvalid: String by value("参数 $0 无法识别")
    val Img_upload_success: String by value("图片上传成功: \n$0 \nid: $1")
    val Img_upload_timeoutWarn: String by value("图片上传还有 $0 秒超时")
    val Exam_task: List<String> by value(listOf(
        "==============[ExamTsk]==============",
        "用户 $0(QQ$1)提交的作业:",
        "$2",
        "id:$3",
        "使用 #exam pass <id> 以设置此作业为通过",
        "使用 #exam fail <id> <reason>  以设置此作业为不通过"
    ))
    val Exam_noUploaded by value("当前无需要审核的作业")

    fun applyPlaceHolderList(orig: List<String>, vararg placeholders: String) : MessageChain {
        val builder = StringBuilder()
        orig.forEach {
            builder.append(it).append("\n")
        }
        var built = builder.deleteCharAt(builder.length-1).deleteCharAt(builder.length-2).toString()
        built = applyPlaceHolder(built, placeholders = placeholders)
        return built.deserializeMiraiCode()
    }

    fun applyPlaceHolder(orig: String, vararg placeholders: String) : String {
        var result = orig
        for ((index, elem) in placeholders.withIndex()) {
            result = result.replace("$$index", elem)
        }
        return result
    }

    private const val imgCodeTemplate = "[mirai:image:id]"
    fun imgToMiraiCode(vararg images: Image) : String {
        var result = ""
        for (image in images) {
            result += imgCodeTemplate.replace("id", image.imageId)
            if (image != images.last())
                result += "\n"
        }

        return result
    }


    fun atToMiraiCode(member: Member) : String {
        return "[mirai:at:${member.id}]"
    }

    fun atAllToMiraiCode() : String {
        return "[mirai:atall]"
    }

    fun faceToMiraiCode(face: Face) : String {
        return "[mirai:face:${face.id}]"
    }

    fun fileToMiraiCode(file: FileMessage) : String {
        return "[mirai:file:${file.id},${file.internalId},${file.name},${file.size}]"
    }
}