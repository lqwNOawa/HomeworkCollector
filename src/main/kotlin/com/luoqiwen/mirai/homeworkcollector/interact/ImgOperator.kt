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
import java.io.*

object ImgOperator {
    suspend fun saveImg(img: Image, task: CollectionTask, member: Member) {
        httpDownload(img.queryUrl(), task.getDataFolder(member))
    }

    suspend fun saveImg(file: FileMessage, task: CollectionTask, member: Member) {
        httpDownload(file.toRemoteFile(member.group)?.getDownloadInfo()?.url ?: "0.0", task.getDataFolder(member))
    }

    private fun httpDownload(url: String, destFile: File) {
        Plugin.launch {
            val client = OkHttpClient()
            val req = Request.Builder().get().url(url).build()
            val resp = client.newCall(req).execute()
            val ins = resp.body!!.byteStream()
            var fos : BufferedOutputStream
            try {
                fos = BufferedOutputStream(FileOutputStream(destFile, false))
                fos.write(ins.readBytes())
                fos.flush()
                fos.close()
            } catch (e: IOException) {
                Plugin.logger.warning(e)
            }
        }
    }
}