package com.luoqiwen.mirai.homeworkcollector.data

import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import net.mamoe.mirai.console.data.value

object Config : ReadOnlyPluginConfig("config") {
    val bot: Long by value(1927241971L)
    val admins: List<Long> by value(listOf(1927241971))
    val group: Long by value(904654326L)
    val uploadTimeout: Int by value(300)
    val include: List<Long> by value(mutableListOf(1750917676L))
    val alias: Map<Long, String> by value(
        mutableMapOf(Pair(1750917676L, "蔡天培"))
    )
}