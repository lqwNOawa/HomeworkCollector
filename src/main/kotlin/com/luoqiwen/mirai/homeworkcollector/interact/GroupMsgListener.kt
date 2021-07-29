package com.luoqiwen.mirai.homeworkcollector.interact

import com.luoqiwen.mirai.homeworkcollector.Plugin
import com.luoqiwen.mirai.homeworkcollector.data.Config
import kotlinx.coroutines.launch
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.ListenerHost
import net.mamoe.mirai.event.events.GroupMessageEvent

object GroupMsgListener : ListenerHost {
    @EventHandler
    suspend fun GroupMessageEvent.onMsg() {
        Plugin.launch {
            if (group.id == Config.group && Config.include.contains(sender.id)) {
                Plugin.collector.processMsg(message, sender, true)
            }
        }
    }
}