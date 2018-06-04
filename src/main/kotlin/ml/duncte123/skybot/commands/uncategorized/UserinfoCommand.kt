/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.commands.uncategorized

import me.duncte123.weebJava.types.StatusType
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.discord.user.Profile
import ml.duncte123.skybot.unstable.utils.ComparatingUtils
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.EmbedUtils
import ml.duncte123.skybot.utils.MessageUtils
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.utils.MiscUtil
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.stream.Collectors

class UserinfoCommand : Command() {

    val folderName = "user_avatars"

    init {
        val imgDir = File(folderName)

        //create the dir
        if (!imgDir.exists())
            imgDir.mkdir()

        //clean the folder
        FileUtils.cleanDirectory(imgDir)
    }


    override fun executeCommand(invoke: String, args: Array<String>, event: GuildMessageReceivedEvent) {
        var u: User
        var m: Member? //this can be lateinit var m: Member //Nope, check line 61

        if (args.isEmpty()) {
            u = event.author
            m = event.guild.getMemberById(u.id)
        } else {
            val mentioned = event.message.mentionedUsers
            if (!mentioned.isEmpty()) {
                m = event.guild.getMember(mentioned[0])
            } else {
                val name = StringUtils.join(args, " ")

                var members = event.guild.getMembersByName(name, true)
                if (members.isEmpty()) {
                    members = event.guild.getMembersByNickname(name, true)
                }
                m = if (members.isEmpty()) null else members[0]
                if (m == null) {
                    try {
                        val memberId = MiscUtil.parseSnowflake(name)
                        m = event.guild.getMemberById(memberId)
                    } catch (ignored: NumberFormatException) { /* ignored */
                    }
                }
            }
        }

        if (m == null) {
            MessageUtils.sendMsg(event, "This user could not be found.")
            return
        }

        u = m.user

        if (invoke == "avatar") {
            MessageUtils.sendMsg(event, "**${String.format("%#s", u)}'s** avatar:\n ${u.effectiveAvatarUrl}?size=2048")
            return
        }
        //A feature that will be implemented soon
        /*AirUtils.getUserProfile(u.id).async ({
            renderEmbed(event, m, it)
        },{
            renderEmbed(event, m, null)
        })*/
        renderEmbed(event, m, null)
    }

    private fun renderEmbed(event: GuildMessageReceivedEvent, m: Member, p: Profile?) {
        var badgesString = ""
        if (p != null) {
            badgesString = "**Badges:** " + p.badges.joinToString()
        }

        val u = m.user
        val joinOrder = StringBuilder()
        val joins = event.guild.memberCache.stream().sorted(
                Comparator.comparing<Member, OffsetDateTime> { it.joinDate }
        ).collect(Collectors.toList())
        var index = joins.indexOf(m)
        index -= 3
        if (index < 0)
            index = 0
        joinOrder.append("\n")
        if (joins[index] == m)
            joinOrder.append("[${joins[index].effectiveName}](https://bot.duncte123.me/)")
        else
            joinOrder.append(joins[index].effectiveName)
        for (i in index + 1 until index + 7) {
            if (i >= joins.size)
                break
            val usr = joins[i]
            var usrName = usr.effectiveName
            if (usr == m)
                usrName = "[$usrName](https://bot.duncte123.me/)"
            joinOrder.append(" > ").append(usrName)
        }

        val embed = EmbedUtils.defaultEmbed()
                .setColor(m.color)
                .setThumbnail(u.effectiveAvatarUrl)
                .setDescription("""User info for ${m.asMention}
                        |
                        |**Username + Discriminator:** ${String.format("%#s", u)}
                        |**User Id:** ${u.id}
                        |**Status:** ${AirUtils.gameToString(m.game)}
                        |**Display Name:** ${m.effectiveName}
                        |**Account Created:** ${u.creationTime.format(DateTimeFormatter.RFC_1123_DATE_TIME)}
                        |**Joined Server:** ${m.joinDate.format(DateTimeFormatter.RFC_1123_DATE_TIME)}
                        |**Join Order:** $joinOrder
                        |**Online Status:** ${AirUtils.convertStatus(m.onlineStatus)} ${m.onlineStatus.name.toLowerCase().replace("_".toRegex(), " ")}
                        |**Bot Account?** ${if (u.isBot) "Yes" else "No"}
                        |$badgesString
                        |
                        |_Use `${PREFIX}avatar [user]` to get a user's avatar_
                    """.trimMargin())

        if (event.guild.selfMember.hasPermission(event.channel, Permission.MESSAGE_ATTACH_FILES) &&
                AirUtils.CONFIG.getString("apis.weeb\\.sh.wolketoken", "INSERT_WEEB_WOLKETOKEN") != "INSERT_WEEB_WOLKETOKEN") {
            AirUtils.WEEB_API.generateDiscordStatus(toWeebshStatus(m),
                    u.effectiveAvatarUrl.replace("gif", "png") + "?size=256").async {

                val targetFile = File("$folderName/user-avatar-${u.id}-${System.currentTimeMillis()}.png")

                Files.copy(it, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)

                event.channel.sendFile(targetFile, "stat.png",
                        MessageBuilder().setEmbed(embed.setThumbnail("attachment://stat.png").build()).build()
                ).queue({}, {
                    ComparatingUtils.execCheck(it)
                    MessageUtils.sendEmbed(event, embed.setThumbnail(u.effectiveAvatarUrl).build())
                })
            }
        } else {
            MessageUtils.sendEmbed(event, embed.build())
        }
    }

    override fun help() = "Get information from yourself or from another user.\nUsage: `$PREFIX$name [username]`"

    override fun getName() = "userinfo"

    override fun getAliases() = arrayOf("user", "i", "avatar")

    private fun toWeebshStatus(member: Member): StatusType {
        if (member.game != null && member.game.type == Game.GameType.STREAMING)
            return StatusType.STREAMING
        return when (member.onlineStatus) {
            OnlineStatus.ONLINE -> StatusType.ONLINE
            OnlineStatus.OFFLINE -> StatusType.OFFLINE
            OnlineStatus.DO_NOT_DISTURB -> StatusType.DND
            OnlineStatus.IDLE -> StatusType.IDLE
            OnlineStatus.INVISIBLE -> StatusType.OFFLINE
            else -> StatusType.ONLINE
        }
    }

}