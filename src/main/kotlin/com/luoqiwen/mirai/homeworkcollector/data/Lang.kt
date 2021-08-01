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

package com.luoqiwen.mirai.homeworkcollector.data

import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.message.code.CodableMessage
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.MessageChain

object Lang: ReadOnlyPluginConfig("lang") {
    val Help: List<String> by value(listOf(
        "========[HomeworkCollector]========",
        "Author: luoqiwen (就是我!)",
        "Source code: https://github.com/lqwNOawa/HomeworkCollector",
        "开源许可: GPL v3.0",
        "指令列表: ",
        "#开始提交 <task> - 提交作业, 提交到指定的<task>内",
        "#结束提交 - 指示当前的提交任务结束,",
        "#取消提交 [id] - 取消提交的作业, 若不指定id, 则取消当前的提交任务",
        "#犯拖延症 <task> <delay> <reason> - 犯一犯拖延症, 拖延提交<task>的作业的时间, 拖延<delay>天, 原因是<reason>, 注意!你会被群嘲!",
        "#help - 再次获取帮助信息",
        "小贴士: ",
        "※机器人无聊天功能(不可抗力限制QAQ), 请勿调戏!",
        "※指令前务必加上指示符 # ",
        "※输入参数请将 <xxx> 替换为目标内容",
        "※提交作业请发送图片, 文件不一定可识别!",
        "※私聊我不需@我, 若在群内使用本机器人功能请@我!",
        "※遇Bug请报告或提交issue.....",
        "!!!!有机器人我就只审核了!!!!",
        "没有人工服务了!!!!!"
    ))

    val Report_init: List<String> by value(listOf(
        "汝辈受本机器人管辖!",
        "语文作业发给我!",
        "关于如何交作业!",
        "如下: "
    ))
    val Report_status: List<String> by value(listOf(
        "======[信息]=====",
        "用户: $0($1)",
        "作业id: $2",
        "隶属任务: $3",
        "截止日期: $4",
        "状态: $5"
    ))
    val Report_list: List<String> by value(listOf(
        "======[信息]=====",
        "任务: $0",
        "截止日期: $1",
        "状态: $2"
    ))
    val Report_empty: String by value("当前无任务")
    val Report_remove_success: String by value("任务 $0 移除成功")

    val Cmd_invalid: String by value("指令 $0 无法识别")
    val Cmd_argInvalid: String by value("参数 $0 无法识别")

    val Img_upload_success: String by value("图片上传成功: \n$0")
    val Img_upload_timeoutWarn: String by value("图片上传还有 $0 秒超时")
    val Img_upload_timedOut: String by value("图片上传已超时")

    val Exam_task: List<String> by value(listOf(
        "==============[ExamTask]==============",
        "用户: $0(QQ$1)",
        "提交的作业:",
        "",
        "$2",
        "",
        "任务: $3",
        "id: $4",
        "使用 #exam pass $4 [reason] 以设置此作业为通过",
        "使用 #exam fail $4 <reason>  以设置此作业为不通过"
    ))
    val Exam_imgFormat: String by value("$0\n↑($1)↑")
    val Exam_noUploaded: String by value("当前无需要审核的作业")
    val Exam_success: List<String> by value(listOf(
        "======[提示]=====",
        "用户: $0($1)",
        "作业id: $2",
        "隶属任务: $3",
        "审核管理员: $4($5)",
        "备注: $6",
        "状态(更新): 审核成功",
        "状态: $7"
    ))
    val Exam_failed: List<String> by value(listOf(
        "======[提示]=====",
        "用户: $0($1)",
        "作业id: $2",
        "隶属任务: $3",
        "审核管理员: $4($5)",
        "备注: $6",
        "状态(更新): 审核失败",
        "状态: $7"
    ))

    val Task_opened: List<String> by value(listOf(
        "======[提示]=====",
        "任务: $0",
        "截止日期: $1",
        "状态(更新): 已开启",
        "状态: $2"
    ))

    val Submit_doing: String by value("你正在进行提交任务!请先完成已挂起的提交任务")
    val Submit_notDoing: String by value("???这不没正在提交任务嘛")
    val Submit_examining: String by value("任务正在审核中!请等待!")
    val Submit_started: List<String> by value(listOf(
        "======[提示]=====",
        "用户: $0($1)",
        "隶属任务: $2",
        "状态(更新): 开启上传",
        "状态: $3"
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
    val Submit_success: List<String> by value(listOf(
        "======[提示]=====",
        "用户: $0($1)",
        "作业id: $2",
        "隶属任务: $3",
        "状态(更新): 上传成功",
        "状态: $4"
    ))
    val Submit_delay_tooLong: List<String> by value(listOf(
        "=====[警告]=====",
        "用户 $0($1) 竟然想!!!!",
        "延迟 $2 天交 $3 的作业!!!!",
        "他给的理由竟然是 $4 !!!!",
        "最 多 只 能 延 迟 $5 天 交 作 业!!!",
        "特此公示",
        "        ————机器人"
    ))
    val Submit_delay_success: List<String> by value(listOf(
        "====[消息]====",
        "用户: $0($1)",
        "申请延迟 $2 天交 $3 的作业!",
        "理由: $4",
        "批了, 大家记着!"
    ))
    val Submit_alreadyFinished: String by value("你已经完成了任务 $0")

    val Notify_start: String by value("现在是 $0, 将对逾期未完成者提示: ")
    val Notify_expiring: String by value("任务 $0 还有 $1 天结束!尽快提交喔")
    val Notify_format: List<String> by value(listOf(
        "--------------",
        "任务: $0",
        "截止日期: $1",
        "未完成者: $2",
        "--------------"
    ))

    val Status_Finished: String by value("已完成")
    val Status_UnderExam: String by value("等待审核")
    val Status_Unfinished: String by value("未完成")
    val Status_TimedOut: String by value("已超时")
    val Status_Operating: String by value("正在进行")
    val Status_None: String by value("N/A")

    fun applyPlaceHolderList(orig: List<String>, vararg placeholders: String) : MessageChain {
        val builder = StringBuilder()
        orig.forEach {
            builder.append(it).append("\n")
        }
        var built = builder.deleteCharAt(builder.length-1).toString()
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

    fun getMiraiCode(contact: CodableMessage) : String {
        return "\\" + contact.serializeToMiraiCode() + "\\"
    }
}