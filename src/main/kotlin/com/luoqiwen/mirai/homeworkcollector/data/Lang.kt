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
        "========[HomeworkCollector]========",
        "Author: luoqiwen (就是我!)",
        "Source code: https://github.com/lqwNOawa/HomeworkCollector",
        "许可证: GPL v3.0",
        "指令列表: ",
        "#exam push <id> - 审核指定 <id> 的作业, 仅管理员(对就是我)可用",
        "#exam push all - 审核所有已提交作业, 仅管理员(对就是我)可用",
        "#exam push random - 随机抽取一位幸运观众的已提交作业审核, 仅管理员(对就是我)可用",
        "#exam pass <id> <remark> - 审核通过指定 <id> 的作业, 留下备注 <remark>, 仅管理员(对就是我)可用",
        "#exam fail <id> <remark> - 审核不通过指定 <id> 的作业, 留下备注 <remark>, 仅管理员(对就是我)可用",
        "",
        "#open <name> <deadline> - 新开启指定 <name> 的作业审核任务, 结束日期为 <deadline>, 格式 yyyy-MM-dd, 仅管理员(我)可用",
        "",
        "#submit start <task> - 开始作业提交, 从发送指令开始对我发送的图片会被记录并储存, 所有人(包括我)可用",
        "#submit cancel <task> - 取消提交的作业, 所有人可用",
        "#submit cancel - 取消正在进行的提交任务, 所有人可用",
        "#submit over - 结束作业提交, 所有人(包括我)可用",
        "#submit delay <task> <day> <reason> - 拖延提交 <task> 对应作业的时间, 拖延 <day> 天!很好用!!!还可以帮您公开发表拖延原因!!!有一点点最多拖延限制! 所有人可用",
        "",
        "#report <name> <task> - 查询指定人在 <task> 的作业完成情况, <name>可为目标QQ或群名片或昵称(依顺序查找), 所有人可用",
        "#report <name> all - 查询指定人在所有已有任务的作业完成情况, <name>可为目标QQ或群名片或昵称(依顺序查找), 所有人可用",
        "",
        "#notify init - 发送机器人苏醒(!!!!)信息, 仅管理员可用",
        "#notify <name> <task> - 发送对于 <name> 指定人 对于 <task> 指定任务的剩余时间, 所有人可用",
        "#notify <name> all",
        "#notify all <task>",
        "#notify all all- 对以上就是排列组合，所有人可用, all代表[所有], 自行理解。。。" ,
        "",
        "#help - 再次获取帮助信息",
        "小贴士: ",
        "※机器人无聊天功能(不可抗力限制QAQ), 请勿调戏!",
        "※指令前务必加上指示符 # ",
        "※输入参数请将 <xxx> 替换为目标内容",
        "※提交作业请发送图片, 文件不一定可识别!",
        "※私聊我不需@我, 若在群内使用本机器人功能请@我!",
        "※遇Bug请报告或提交issue....."
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
    val Exam_noUploaded: String by value("当前无需要审核的作业")
    val Exam_success: List<String> by value(listOf(
        "======[提示]=====",
        "用户: $0($1)",
        "作业id: $2",
        "隶属任务: $3",
        "审核管理员: $4",
        "备注: $5",
        "状态(更新): 审核成功",
        "状态: 已完成"
    ))
    val Exam_failed: List<String> by value(listOf(
        "======[提示]=====",
        "用户: $0($1)",
        "作业id: $2",
        "隶属任务: $3",
        "审核管理员: $4($5)",
        "备注: $6",
        "状态(更新): 审核失败",
        "状态: 未完成"
    ))

    val Task_uploadSuccess: List<String> by value(listOf(
        "======[提示]=====",
        "用户: $0($1)",
        "作业id: $2",
        "隶属任务: $3",
        "状态(更新): 上传成功",
        "状态: 等待审核"
    ))
    val Task_opened: List<String> by value(listOf(
        "======[提示]=====",
        "任务: $0",
        "截止日期: $1",
        "状态(更新): 已开启",
        "状态: 正在进行"
    ))

    val Submit_doing: String by value("你正在进行提交任务!请先完成已挂起的提交任务")
    val Submit_notDoing: String by value("???这不没正在提交任务嘛")
    val Submit_started: List<String> by value(listOf(
        "======[提示]=====",
        "用户: $0($1)",
        "隶属任务: $2",
        "状态(更新): 开启上传",
        "状态: N/A"
    ))
    val Submit_cancelled_while: List<String> by value(listOf(
        "======[提示]=====",
        "用户: $0($1)",
        "隶属任务: $2",
        "状态(更新): 取消上传",
        "状态: N/A"
    ))
    val Submit_cancelled: List<String> by value(listOf(
        "======[提示]=====",
        "用户: $0($1)",
        "作业id: $2",
        "隶属任务: $3",
        "状态(更新): 取消提交",
        "状态: N/A"
    ))

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