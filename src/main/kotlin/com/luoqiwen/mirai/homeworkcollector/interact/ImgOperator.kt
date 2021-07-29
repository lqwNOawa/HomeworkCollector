package com.luoqiwen.mirai.homeworkcollector.interact

import com.luoqiwen.mirai.homeworkcollector.Plugin
import com.luoqiwen.mirai.homeworkcollector.workinstance.CollectionTask
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.*
import java.nio.file.Files

object ImgOperator {
    fun saveImg(img: Image, task: CollectionTask, member: Member) {
        Plugin.launch {
            val url = img.queryUrl()
            val dest = File(Plugin.dataFolder, "${member.id}/${task.name}")
            dest.mkdirs()
            val destFile = File(dest, img.imageId+"png")
            if (!destFile.exists())
                Files.createFile(destFile.toPath())

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