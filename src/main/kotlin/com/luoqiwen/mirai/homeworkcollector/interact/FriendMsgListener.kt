package com.luoqiwen.mirai.homeworkcollector.interact

import com.luoqiwen.mirai.homeworkcollector.Plugin
import com.luoqiwen.mirai.homeworkcollector.data.Config
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.ListenerHost
import net.mamoe.mirai.event.events.FriendMessageEvent

object FriendMsgListener : ListenerHost{
    @EventHandler
    suspend fun FriendMessageEvent.onMsg() {
        Plugin.launch {
            if (Config.include.contains(sender.id))
            {
                val member = Plugin.group.getMember(sender.id)
                if (member != null)
                    Plugin.collector.processMsg(message, member)
            }
        }
    }
}