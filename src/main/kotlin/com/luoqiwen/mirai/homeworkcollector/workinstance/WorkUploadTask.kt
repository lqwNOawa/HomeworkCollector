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
import com.luoqiwen.mirai.homeworkcollector.data.Config
import com.luoqiwen.mirai.homeworkcollector.data.Lang
import com.luoqiwen.mirai.homeworkcollector.interact.UserNotifier
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.message.data.Image

class WorkUploadTask(val member: Member, val task: CollectionTask) {
    val uploadedImages = mutableSetOf<Image>()
    val uploadedFiles = mutableSetOf<FileMessage>()
    private var countDownJob : Job? = null

    fun launchCountDownJob(inGroup: Boolean) {
        countDownJob = Plugin.launch {
            for (i in Config.uploadTimeout downTo 1) {
                if (i == 60 || i == 30 || i == 10 || i == 5)
                    UserNotifier.notifyUser(Lang.applyPlaceHolder(
                        Lang.Img_upload_timeoutWarn, i.toString()
                    ).deserializeMiraiCode(), member, inGroup)
                delay(1000)
            }
            UserNotifier.notifyUser(Lang.applyPlaceHolder(
                Lang.Img_upload_timedOut
            ).deserializeMiraiCode(), member, inGroup)
        }
    }

    fun addImg(img: Image) : Int {
        uploadedImages.add(img)
        return uploadedImages.indexOf(img)
    }

    fun addImg(file: FileMessage) : Int {
        uploadedFiles.add(file)
        return uploadedFiles.indexOf(file)
    }

    private fun toExamTask() : ExamTask {
        return ExamTask(this)
    }

    fun cancel() {
        Plugin.collector.uploadingMap.remove(member.id)
        countDownJob?.cancel("已结束")
    }

    fun terminate() : ExamTask {
        cancel()
        val examtask = toExamTask()
        (Plugin.collector.examTasks.getOrPut(member.id) { mutableSetOf() }).add(examtask)
        return examtask
    }
}