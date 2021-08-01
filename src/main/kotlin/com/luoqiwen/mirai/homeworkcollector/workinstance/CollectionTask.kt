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
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Member
import java.io.File
import java.nio.file.Files
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class CollectionTask (val name: String, val deadline: LocalDate) {
    init {
        getDataFolder().mkdirs()
    }

    fun getDataFolder(member: Member) : File {
        return File(getDataFolder(), member.id.toString())
    }

    private fun getDataFolder() : File {
        return File(Plugin.dataFolder, name)
    }

    fun getTaskStatus() : TaskStatus {
        return if (LocalDate.now().isAfter(deadline))
            TaskStatus.Finished
        else
            TaskStatus.Operating
    }

    fun finish(member: Member) {
        Plugin.launch {
            val file = File(getDataFolder(member), ".finished")
            if (!file.exists()) {
                file.parentFile.mkdirs()
                Files.createFile(file.toPath())
            }
        }
    }

    fun isFinished(member: Member) : Boolean {
        return File(getDataFolder(member), ".finished").exists()
    }

    fun isUnderExam(member: Member) : Boolean {
        Plugin.collector.examTasks[member.id]?.forEach {
            if (it.task == this)
                return true
        } ?: return false
        return false
    }

    fun getRemainingDays() : Long {
        return ChronoUnit.DAYS.between(LocalDate.now(), deadline)
    }

    fun isExpired(member: Member?) : Boolean {
        return LocalDate.now().isBefore(getRealDeadline(member))
    }

    fun isExpiringIn(expireDay: Int, member: Member?) : Boolean {
        return LocalDate.now().plusDays(expireDay.toLong()).isEqual(getRealDeadline(member))
    }

    fun getDelay(member: Member?) : Int {
        if (member == null)
            return 0
        return Plugin.collector.delayMap.getOrDefault(Pair(member.id, this.name), 0)
    }

    fun getRealDeadline(member: Member?) : LocalDate {
        return deadline.plusDays(getDelay(member).toLong())
    }

    fun getExamTask(member: Member) : ExamTask? {
        Plugin.collector.examTasks[member.id]?.forEach {
            if (it.task == this)
                return it
        } ?: return null
        return null
    }

    fun remove() {
        Plugin.collector.collectionTaskMap.remove(name)
        getDataFolder().delete()
    }
}