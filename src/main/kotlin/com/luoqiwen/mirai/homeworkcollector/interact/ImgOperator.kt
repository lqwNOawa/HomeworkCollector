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

package com.luoqiwen.mirai.homeworkcollector.interact

import com.luoqiwen.mirai.homeworkcollector.Plugin
import com.luoqiwen.mirai.homeworkcollector.workinstance.CollectionTask
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files

object ImgOperator {
    fun saveImg(img: Image, task: CollectionTask, member: Member) {
        Plugin.launch {
            val imgFile = File(task.getDataFolder(member), img.imageId)
            httpDownload(img.queryUrl(), imgFile)
        }
    }

    fun saveImg(file: FileMessage, task: CollectionTask, member: Member) {
        Plugin.launch {
            httpDownload(file.toRemoteFile(member.group)?.getDownloadInfo()?.url ?: "0.0", task.getDataFolder(member))
        }
    }

    private fun httpDownload(url: String, destFile: File) {
        Plugin.launch {
            if (!destFile.exists()) {
                Files.createFile(destFile.toPath())
                val client = OkHttpClient()
                val req = Request.Builder().get().url(url).build()
                val resp = client.newCall(req).execute()
                val ins = resp.body!!.byteStream()
                var fos : BufferedOutputStream? = null
                try {
                    fos = BufferedOutputStream(FileOutputStream(destFile, false))
                    fos.write(ins.readBytes())
                } catch (e: IOException) {
                    Plugin.logger.warning(e)
                } finally {
                    fos?.flush()
                    fos?.close()
                }
            }
        }
    }
}