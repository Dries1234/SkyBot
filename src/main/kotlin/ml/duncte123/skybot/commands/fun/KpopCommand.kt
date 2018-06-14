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

package ml.duncte123.skybot.commands.`fun`

import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.utils.ApiUtils
import ml.duncte123.skybot.utils.EmbedUtils
import ml.duncte123.skybot.utils.MessageUtils
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import java.sql.SQLException

class KpopCommand : Command() {

    init {
        this.category = CommandCategory.FUN
    }

    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {

        try {
            val queryString = if (!args.isEmpty()) args.joinToString(separator = " ") else ""
            val member = ApiUtils.getRandomKpopMember(queryString)
            val eb = EmbedUtils.defaultEmbed()
                    .setDescription("Here is a kpop member from the group ${member.band}")
                    .addField("Name of the member", member.name, false)
                    .setImage(member.image)
                    .setFooter("Query id: ${member.id}", Settings.DEFAULT_ICON)
            MessageUtils.sendEmbed(event, eb.build())
        } catch (ignored: SQLException) {
            MessageUtils.sendMsg(event, "Nothing found")
        }
    }

    override fun help() = "Gives you a random kpop member, command suggestion by Exa\n" +
            "Usage: `$PREFIX$name [search term]`"

    override fun getName() = "kpop"
}