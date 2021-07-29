package com.luoqiwen.mirai.homeworkcollector.interact

import com.luoqiwen.mirai.homeworkcollector.Plugin
import com.luoqiwen.mirai.homeworkcollector.data.Config
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageChainBuilder

object UserNotifier {
    suspend fun notifyAdmins(msg: MessageChain) {
        val bot = Plugin.bot
        Config.admins.forEach {
            bot.getFriend(it)?.sendMessage(msg)
        }
    }

    suspend fun notifyUser(msg: MessageChain, member: Member, inGroup: Boolean) {
        val bot = Plugin.bot
        if (inGroup) {
            val group = Plugin.group
            val atMsgBuilder = MessageChainBuilder()
            atMsgBuilder.add(At(member.id))
            atMsgBuilder.addAll(msg)
            group.sendMessage(atMsgBuilder.build())
        }
        else {
            val receiver = bot.getFriend(member.id)
            val stranger = bot.getStranger(member.id)
            if (receiver != null)
                receiver.sendMessage(msg)
            else if (stranger != null)
                stranger.sendMessage(msg)
            else
                Plugin.logger.warning("$bot has no way to send message to user ${member.id} !!!!")
        }
    }
}