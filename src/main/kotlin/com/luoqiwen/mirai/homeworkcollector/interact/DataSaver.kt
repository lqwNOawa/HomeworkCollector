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
import com.luoqiwen.mirai.homeworkcollector.workinstance.ExamTask
import com.luoqiwen.mirai.homeworkcollector.workinstance.WorkUploadTask
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import net.mamoe.mirai.contact.getMemberOrFail
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.message.data.Image
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * I KNOW THIS IS NOT GRACEFUL!!!!!!!!!!!!!!!!!
 * BUT IT IS ANNOYINNNNNNNNNNNNNNNNNNNNNNG ME!!!!!
 */
object DataSaver {
    fun saveUploadingMap() {
        val serialized = serializeUploadingMap(Plugin.collector.uploadingMap)
        Plugin.debug("Writing uploadingMap, serialized json: ${Json.encodeToString(serialized)}")
        jsonWrite(serialized, File(Plugin.dataFolder, "uploadingMap.json"))
        Plugin.debug("UploadingMap has been written successfully")
    }

    fun loadUploadingMap() : MutableMap<Long, WorkUploadTask> {
        Plugin.debug("Reading uploadingMap...")
        val json = jsonRead(File(Plugin.dataFolder, "uploadingMap.json"))
        Plugin.debug("UploadingMap has been read successfully, deserialized json: ${Json.encodeToString(json)}")
        return deserializeUploadMap(json)
    }

    fun saveExamTasks() {
        val serialized = serializeExamTasks(Plugin.collector.examTasks)
        Plugin.debug("Writing examTasks, serialized json: ${Json.encodeToString(serialized)}")
        jsonWrite(serialized, File(Plugin.dataFolder, "examTasks.json"))
        Plugin.debug("ExamTasks has been written successfully")
    }

    fun loadExamTasks() : MutableMap<Long, MutableSet<ExamTask>> {
        Plugin.debug("Reading examTasks...")
        val json = jsonRead(File(Plugin.dataFolder, "examTasks.json"))
        Plugin.debug("ExamTasks has been read successfully, deserialized json: ${Json.encodeToString(json)}")
        return deserializeExamTasks(json)
    }

    fun saveCollectionTaskMap() {
        val serialized = serializeCollectionTaskMap(Plugin.collector.collectionTaskMap)
        Plugin.debug("Writing CollectionTaskMap, serialized json: ${Json.encodeToString(serialized)}")
        jsonWrite(serialized, File(Plugin.dataFolder, "collectionTaskMap.json"))
        Plugin.debug("CollectionTaskMap has been written successfully")
    }

    fun loadCollectionTaskMap() : MutableMap<String, CollectionTask> {
        Plugin.debug("Reading CollectionTaskMap...")
        val json = jsonRead(File(Plugin.dataFolder, "collectionTaskMap.json"))
        Plugin.debug("CollectionTaskMap has been read successfully, deserialized json: ${Json.encodeToString(json)}")
        return deserializeCollectionTaskMap(json)
    }

    fun saveDelayMap() {
        val serialized = serializeDelayMap(Plugin.collector.delayMap)
        Plugin.debug("Writing DelayMap, serialized json: ${Json.encodeToString(serialized)}")
        jsonWrite(serialized, File(Plugin.dataFolder, "delayMap.json"))
        Plugin.debug("DelayMap has been written successfully")
    }

    fun loadDelayMap() : MutableMap<Pair<Long, String>, Int> {
        Plugin.debug("Reading DelayMap...")
        val json = jsonRead(File(Plugin.dataFolder, "delayMap.json"))
        Plugin.debug("DelayMap has been read successfully, deserialized json: ${Json.encodeToString(json)}")
        return deserializeDelayMap(json)
    }

    private fun jsonWrite(json: JsonElement, target: File) {
        target.delete()
        var writer: BufferedWriter? = null
        try {
            writer = Files.newBufferedWriter(target.toPath(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE)
            writer.write(Json.encodeToString(json))
        } catch (e: IOException) {
            Plugin.logger.warning(e)
        } finally {
            writer?.flush()
            writer?.close()
        }
    }

    private fun jsonRead(target: File) : JsonElement? {
        if (!target.exists())
            return null
        val reader: BufferedReader
        return try {
            reader = Files.newBufferedReader(target.toPath(), StandardCharsets.UTF_8)
            val text = reader.readText()
            reader.close()
            Json.parseToJsonElement(text)
        } catch (e: Exception) {
            Plugin.logger.warning(e)
            null
        }
    }

    private fun serializeDelayMap(delayMap: MutableMap<Pair<Long, String>, Int>) : JsonElement {
        return buildJsonObject {
            delayMap.forEach { (pair, delay) ->
                this.put(serializePair(pair), delay)
            }
        }
    }

    private fun deserializeDelayMap(json: JsonElement?) : MutableMap<Pair<Long, String>, Int> {
        val map = mutableMapOf<Pair<Long, String>, Int>()
        json?.jsonObject?.forEach { (pair, delay) ->
            map[deserializePair(pair)] = delay.jsonPrimitive.int
        }
        return map
    }

    private fun serializeCollectionTaskMap(collectionTaskMap: MutableMap<String, CollectionTask>) : JsonElement {
        return buildJsonObject {
            collectionTaskMap.forEach { (name, task) ->
                this.put(name, serializeCollectionTask(task))
            }
        }
    }

    private fun deserializeCollectionTaskMap(json: JsonElement?) : MutableMap<String, CollectionTask> {
        val map = mutableMapOf<String, CollectionTask>()
        json?.jsonObject?.forEach { (name, task) ->
            map[name] = deserializeCollectionTask(task)
        }
        return map
    }

    private fun serializeExamTasks(examTasks: MutableMap<Long, MutableSet<ExamTask>>) : JsonElement {
        return buildJsonObject {
            examTasks.forEach { (id, set) ->
                this.put(id.toString(), serializeExamTaskSet(set))
            }
        }
    }

    private fun deserializeExamTasks(json: JsonElement?) : MutableMap<Long, MutableSet<ExamTask>> {
        val map = mutableMapOf<Long, MutableSet<ExamTask>>()
        json?.jsonObject?.forEach { (id, set) ->
            val member = id.toLong()
            val taskSet = deserializeExamTaskSet(set)
            map[member] = taskSet
        }
        return map
    }

    private fun serializeUploadingMap(uploadingMap: MutableMap<Long, WorkUploadTask>) : JsonElement {
        return buildJsonObject {
            uploadingMap.forEach { (id, workUploadTask) ->
                this.put(id.toString(), serializeWorkUploadTask(workUploadTask))
            }
        }
    }

    private fun deserializeUploadMap(json: JsonElement?) : MutableMap<Long, WorkUploadTask> {
        val map = mutableMapOf<Long, WorkUploadTask>()
        json?.jsonObject?.forEach { key, jsonElem ->
            map[key.toLong()] = deserializeWorkUploadTask(jsonElem)
        }
        return map
    }

    private fun serializeExamTaskSet(set: Set<ExamTask>) : JsonElement {
        return buildJsonArray {
            set.forEach { this.add(serializeExamTask(it)) }
        }
    }

    private fun deserializeExamTaskSet(json: JsonElement?) : MutableSet<ExamTask> {
        val set = mutableSetOf<ExamTask>()
        json?.jsonArray?.forEach { set.add(deserializeExamTask(it)) }
        return set
    }

    private fun serializeExamTask(examTask: ExamTask) : JsonElement {
        return buildJsonObject {
            put("member", examTask.member.id)
            put("task", serializeCollectionTask(examTask.task))
            put("uploadedImages", serializeUploadedImageSet(examTask.uploadedImages))
            put("uploadedFiles", serializeUploadedFileSet(examTask.uploadedFiles))
        }
    }

    private fun deserializeExamTask(json: JsonElement) : ExamTask {
        val member = json.jsonObject["member"]!!.jsonPrimitive.long
        val task = deserializeCollectionTask(json.jsonObject["task"]!!.jsonObject)
        val uploadedImages = deserializeUploadedImageSet(json.jsonObject["uploadedImages"]!!)
        val uploadedFiles = deserializeUploadedFileSet(json.jsonObject["uploadedFiles"]!!)
        val examTask = ExamTask(Plugin.group().getMemberOrFail(member), task)

        uploadedImages.forEach {
            examTask.addImg(it)
        }
        uploadedFiles.forEach {
            examTask.addImg(it)
        }

        return ExamTask(Plugin.group().getMemberOrFail(member), task)
    }

    private fun serializeWorkUploadTask(workUploadTask: WorkUploadTask) : JsonElement {
        return buildJsonObject {
            put("member", workUploadTask.member.id)
            put("task", serializeCollectionTask(workUploadTask.task))
            put("uploadedImages", serializeUploadedImageSet(workUploadTask.uploadedImages))
            put("uploadedFiles", serializeUploadedFileSet(workUploadTask.uploadedFiles))
        }
    }

    private fun deserializeWorkUploadTask(json: JsonElement) : WorkUploadTask {
        val member = json.jsonObject["member"]!!.jsonPrimitive.long
        val task = deserializeCollectionTask(json.jsonObject["task"]!!.jsonObject)
        val uploadedImages = deserializeUploadedImageSet(json.jsonObject["uploadedImages"]!!)
        val uploadedFiles = deserializeUploadedFileSet(json.jsonObject["uploadedFiles"]!!)
        val workUploadTask = WorkUploadTask(Plugin.group().getMemberOrFail(member), task)

        uploadedImages.forEach {
            workUploadTask.addImg(it)
        }
        uploadedFiles.forEach {
            workUploadTask.addImg(it)
        }

        return workUploadTask
    }

    private fun serializeCollectionTask(task: CollectionTask) : JsonElement {
        return buildJsonObject {
            put("name", task.name)
            put("deadline", task.deadline.format(DateTimeFormatter.ISO_LOCAL_DATE))
        }
    }

    private fun deserializeCollectionTask(json: JsonElement) : CollectionTask {
        val name = json.jsonObject["name"]!!.jsonPrimitive.content
        val deadline = json.jsonObject["deadline"]!!.jsonPrimitive.content
        return CollectionTask(name, LocalDate.parse(deadline, DateTimeFormatter.ISO_LOCAL_DATE))
    }

    private fun serializeUploadedImageSet(uploadedImages: MutableSet<Image>) : JsonElement {
        //return Json.encodeToJsonElement(uploadedImages)
        return buildJsonArray {
            uploadedImages.forEach {
                this.add(serializeImage(it))
            }
        }
    }

    private fun deserializeUploadedImageSet(json: JsonElement) : MutableSet<Image> {
        //return Json.decodeFromJsonElement(json)
        val result = mutableSetOf<Image>()
        json.jsonArray.forEach {
            result.add(deserializeImage(it))
        }
        return result
    }

    private fun serializeUploadedFileSet(uploadedFiles: MutableSet<FileMessage>) : JsonElement {
        //return Json.encodeToJsonElement(uploadedFiles)
        return buildJsonArray {
            uploadedFiles.forEach {
                this.add(serializeFile(it))
            }
        }
    }

    private fun deserializeUploadedFileSet(json: JsonElement) : MutableSet<FileMessage> {
        //return Json.decodeFromJsonElement(json)
        val result = mutableSetOf<FileMessage>()
        json.jsonArray.forEach {
            result.add(deserializeFile(it))
        }
        return result
    }

    private fun serializePair(pair: Pair<Long, String>) : String {
        return "${pair.first}|${pair.second}"
    }

    private fun deserializePair(serialized: String) : Pair<Long, String> {
        val index = serialized.indexOf("|")
        return Pair(serialized.substring(0, index).toLong(), serialized.substring(index+1))
    }

    private fun serializeImage(image: Image) : JsonElement {
        return buildJsonObject {
            put("id", image.imageId)
        }
    }

    private fun deserializeImage(json: JsonElement) : Image {
        return Image(json.jsonObject["id"]!!.jsonPrimitive.content)
    }

    private fun serializeFile(fileMessage: FileMessage) : JsonElement {
        return buildJsonObject {
            put("id", fileMessage.id)
            put("internalId", fileMessage.internalId)
            put("name", fileMessage.name)
            put("size", fileMessage.size)
        }
    }

    private fun deserializeFile(json: JsonElement) : FileMessage {
        val read = json.jsonObject
        return FileMessage(
            read["id"]!!.jsonPrimitive.content,
            read["internalId"]!!.jsonPrimitive.int,
            read["name"]!!.jsonPrimitive.content,
            read["size"]!!.jsonPrimitive.long)
    }
}