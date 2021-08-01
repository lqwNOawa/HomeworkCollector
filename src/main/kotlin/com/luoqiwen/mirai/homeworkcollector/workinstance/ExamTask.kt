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

package com.luoqiwen.mirai.homeworkcollector.workinstance

import com.luoqiwen.mirai.homeworkcollector.Plugin
import com.luoqiwen.mirai.homeworkcollector.data.Lang
import com.luoqiwen.mirai.homeworkcollector.interact.ImgOperator
import com.luoqiwen.mirai.homeworkcollector.interact.UserNotifier
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.message.data.Image

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

    fun addImg(file: FileMessage) : Int {
        uploadedFiles.add(file)
        return uploadedFiles.indexOf(file)
    }

    fun generateId() : String {
        val list = Plugin.collector.examTasks[member.id] ?: return "N/A"
        val index = list.indexOf(this)
        return Companion.generateId(member, index)
    }

    fun cancel() {
        Plugin.collector.examTasks[member.id]?.remove(this)
    }

    fun examSuccess(examiner: Member, Remark: String?, inGroup: Boolean) {
        Plugin.launch {
            val remark = Remark ?: ""
            UserNotifier.notifyUser(Lang.applyPlaceHolderList(
                Lang.Exam_success,
                Plugin.getAlias(member),
                member.id.toString(),
                generateId(),
                task.name,
                Plugin.getAlias(examiner),
                examiner.id.toString(),
                remark,
                TaskStatus.Finished.lang
            ), member, inGroup)
            Plugin.collector.examTasks.getOrPut(member.id) { mutableSetOf() } .remove(this@ExamTask)

            task.finish(member)

            uploadedImages.forEach {
                ImgOperator.saveImg(it, task, member)
            }
            uploadedFiles.forEach {
                ImgOperator.saveImg(it, task, member)
            }
        }
    }

    fun examFail(examiner: Member, remark: String = "", inGroup: Boolean) {
        Plugin.launch {
            UserNotifier.notifyUser(Lang.applyPlaceHolderList(
                Lang.Exam_failed,
                Plugin.getAlias(member),
                member.id.toString(),
                generateId(),
                task.name,
                Plugin.getAlias(examiner),
                examiner.id.toString(),
                remark,
                TaskStatus.Unfinished.lang
            ), member, inGroup)
            Plugin.collector.examTasks.getOrPut(member.id) { mutableSetOf() } .remove(this@ExamTask)
        }
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