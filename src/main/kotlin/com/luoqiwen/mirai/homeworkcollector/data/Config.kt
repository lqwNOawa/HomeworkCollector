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

package com.luoqiwen.mirai.homeworkcollector.data

import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import net.mamoe.mirai.console.data.value

object Config : ReadOnlyPluginConfig("config") {
    val debug: Boolean by value(false)
    val bot: Long by value(1927241971L)
    val admins: List<Long> by value(listOf(1927241971))
    val group: Long by value(904654326L)
    val uploadTimeout: Int by value(300)
    val maxDelay: Int by value(3)
    val expiringIn: Int by value(1)
    val include: List<Long> by value(mutableListOf(1750917676L))
    val alias: Map<Long, String> by value(
        mutableMapOf(Pair(1750917676L, "蔡天培"))
    )
}