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

package ml.duncte123.skybot.commands.music

import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.objects.command.MusicCommand
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.json.JSONArray

import java.nio.charset.StandardCharsets.UTF_8

class SaveCommand : MusicCommand() {

    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {
        event.channel.sendFile(
                toByteArray(event.guild),
                "playlist.json",
                MessageBuilder()
                        .append(event.author)
                        .append(", here is the queue which can be re-imported")
                        .build()).queue()
    }

    private fun toByteArray(guild: Guild?): ByteArray {
        var array = JSONArray()
        var manager = getMusicManager(guild)

        var urls = manager.scheduler.queue
                .map { it.identifier }
                .toMutableList()

        urls.add(0, manager.player.playingTrack.identifier)

        for (x in urls)
            array.put(x)

        return array.toString(2).toByteArray(UTF_8)
    }

    override fun getName() = "save"

    override fun help() = "Saves a playlist into a file with can be loaded with ${Settings.PREFIX}load"
}