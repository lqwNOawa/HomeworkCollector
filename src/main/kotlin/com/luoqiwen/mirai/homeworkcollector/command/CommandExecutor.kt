package com.luoqiwen.mirai.homeworkcollector.command

import net.mamoe.mirai.contact.Member

interface CommandExecutor {
    suspend fun execute(cmd: List<String>, sender: Member, inGroup: Boolean)

    fun isValid(cmd: List<String>, sender: Member) : Boolean

    fun getMinArgLength() : Int
}