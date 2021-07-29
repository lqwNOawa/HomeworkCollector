package com.luoqiwen.mirai.homeworkcollector

import com.luoqiwen.mirai.homeworkcollector.data.Config
import com.luoqiwen.mirai.homeworkcollector.data.Lang
import com.luoqiwen.mirai.homeworkcollector.interact.UserNotifier
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.disable
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.utils.info

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
    val aliasMap = mutableMapOf<Long, String?>()
    lateinit var bot: Bot
    lateinit var group: Group

    override fun onEnable() {
        logger.info("Loading plugin...")

        logger.info("Reading data...")
        //ReadData
        collector.loadData(dataFolder)
        Config.reload()
        Lang.reload()

        val bot0 = Bot.getInstanceOrNull((Config.bot))
        val group0 = bot0?.getGroup(Config.group)
        if (bot0 == null)
        {
            logger.warning("bot ${Config.bot} has not logger in yet!!!")
            return
        }
        if (group0 == null)
        {
            logger.warning("bot ${Config.bot} has not joint group ${Config.group} yet!!!")
            return
        }
        bot = bot0
        group = group0

        loadAlias()
    }

    override fun onDisable() {
        logger.info("Disabling plugin...")

        logger.info("Saving data...")
        //LoadData
        collector.saveData(dataFolder)
    }

    private fun loadAlias() {
        Config.include.forEach {
            aliasMap[it] = Config.alias.getOrDefault(it, group.getMember(it)?.nameCardOrNick)
        }
    }

    fun getAlias(member: Member) : String {
        return aliasMap[member.id] ?: member.nick
    }
}