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

package com.luoqiwen.mirai.homeworkcollector

import com.luoqiwen.mirai.homeworkcollector.data.Config
import com.luoqiwen.mirai.homeworkcollector.data.Lang
import com.luoqiwen.mirai.homeworkcollector.interact.FriendMsgListener
import com.luoqiwen.mirai.homeworkcollector.interact.GroupMsgListener
import com.luoqiwen.mirai.homeworkcollector.interact.GroupTempMsgListener
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.globalEventChannel

object Plugin : KotlinPlugin(
    JvmPluginDescription(
        id = "com.luoqiwen.mirai.HomeworkCollector",
        name = "HomeworkCollector",
        version = "1.0-SNAPSHOT",
    ) {
        author("luoqiwen")
    }
) {
    val collector = HomeworkCollector()
    private val aliasMap = mutableMapOf<Long, String?>()

    fun bot() : Bot {
        return Bot.getInstance(Config.bot)
    }

    fun group() : Group {
        return bot().getGroupOrFail(Config.group)
    }

    override fun onEnable() {
        logger.info("Loading plugin...")

        logger.info("Reading config...")
        //ReadData
        Config.reload()
        Lang.reload()

        this.globalEventChannel().subscribeAlways<BotOnlineEvent> {
            if (this.bot.id == Config.bot) {
                logger.info("Reloading data...")
                collector.loadData()
                logger.info("Successfully Reloaded data")
                loadAlias()
                bot.eventChannel.registerListenerHost(FriendMsgListener)
                bot.eventChannel.registerListenerHost(GroupMsgListener)
                bot.eventChannel.registerListenerHost(GroupTempMsgListener)
                collector.launchNotifyTimer()
                logger.info("Got configured bot, listeners are successfully registered")
            }
        }
    }

    override fun onDisable() {
        logger.info("Disabling plugin...")

        logger.info("Saving data...")
        //LoadData
        collector.saveData()
    }

    private fun loadAlias() {
        Config.include.forEach {
            aliasMap[it] = Config.alias.getOrDefault(it, group().getMember(it)?.nameCardOrNick)
        }
    }

    fun getAlias(member: Member) : String {
        return aliasMap[member.id] ?: member.nick
    }

    fun findTarget(name: String) : Member? {
        val id = name.toLongOrNull()
        if (id != null && Config.include.contains(id)) {
            return group().getMember(id)
        } else if (aliasMap.containsValue(name)) {
            return group().getMember(aliasMap.keys.elementAt(aliasMap.values.indexOf(name)))
        } else {
            group().members.forEach {
                if (it.nameCard == name)
                    return it
            }
            return null
        }
    }

    fun debug(msg: String?) {
        if (Config.debug) {
            logger.info("[Debug]: $msg")
        }

    }
}