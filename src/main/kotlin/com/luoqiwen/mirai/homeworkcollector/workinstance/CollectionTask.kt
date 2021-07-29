package com.luoqiwen.mirai.homeworkcollector.workinstance

import com.luoqiwen.mirai.homeworkcollector.Plugin
import net.mamoe.mirai.contact.Member
import java.io.File
import java.time.LocalDate

class CollectionTask (val name: String, val deadline: LocalDate) {
    fun getDataFolder(member: Member) : File {
        return File(getDataFolder(), member.id.toString())
    }

    fun getDataFolder() : File {
        return File(Plugin.dataFolder, name)
    }

    fun isFinished(member: Member) : Boolean {
        return File(getDataFolder(member), ".finished").exists()
    }
}