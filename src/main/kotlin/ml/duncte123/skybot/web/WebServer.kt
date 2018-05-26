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

package ml.duncte123.skybot.web

import ml.duncte123.skybot.SkyBot
import ml.duncte123.skybot.objects.StringMap
import ml.duncte123.skybot.utils.AudioUtils
import ml.duncte123.skybot.utils.GuildSettingsUtils
import net.dv8tion.jda.core.entities.Guild
import spark.ModelAndView
import spark.Request
import spark.Response
import spark.Spark.*
import spark.template.jtwig.JtwigTemplateEngine

/**
 * Notes:
 * duncte123: we're gonna use this for templating https://github.com/warhuhn/warhuhn-spark-template-jtwig
 * it's twig and it's easy to use
 */

class WebServer {

    fun activate() {
        //Port has to be 2000 because of the apache proxy on the vps
        port(2000)

        get("/", { request, response ->
            ModelAndView(StringMap().put("title", "Home").map, "base.twig")
        }, JtwigTemplateEngine())

        get("/api/servers") { request, response ->
            "Server count: ${SkyBot.getInstance().shardManager.guildCache.size()}"
        }

        path("/server/:guildid") {
            
            before("*") { request, response ->
                val guild = getGuildFromRequest(request, response)
                if(guild == null && !request.uri().contains("invalid")) {
                    response.redirect("/server/${request.params(":guildid*")}/invalid")
                } else if(guild != null && request.uri().contains("invalid")) {
                    response.redirect("/server/${request.params(":guildid*")}")
                }
            }
            
            //overview and editing
            get("") { request, response ->
                val guild = getGuildFromRequest(request, response)
                if (guild != null) {
                    val settings = GuildSettingsUtils.getGuild(guild)
                    """<p>Guild prefix: ${settings.customPrefix}</p>
                    |<p>Join Message: ${settings.customJoinMessage}</p>
                    """.trimMargin()
                } else { response.body() }
            }
            //audio stuff
            get("/music") { request, response ->
                val guild = getGuildFromRequest(request, response)
                if(guild != null) {
                    val mng = AudioUtils.ins.getMusicManager(guild, false)

                    if(mng != null) {
                        """<p>Audio player details:</p>
                            |<p>Currently playing: <b>${ if (mng.player.playingTrack != null)
                                                mng.player.playingTrack.info.title else "nothing" }</b></p>
                            |<p>Total tracks in queue: <b>${mng.scheduler.queue.size}</b></p>
                        """.trimMargin()
                    } else {
                        "The audio player does not seem to be active"
                    }
                } else { response.body() }
            }

            //when the guild is not found
            get("/invalid") { _, response ->
                response.status(404)
                "DuncteBot is not in the requested server, why don't you <a href=\"#\">invite it</a>?"
            }
        }

        notFound { _, _ ->
            "This page could not be found"
        }

        after("") { request, response ->
            response.body()
        }
    }


    private fun getGuildFromRequest(request: Request, response: Response): Guild? {

        val guildId = if(!request.params(":guildid").isNullOrEmpty())
            request.params(":guildid")
        else request.params(":guildid*")

        val guild = SkyBot.getInstance()
                .shardManager.getGuildById(guildId)

        if(guild == null) {
            response.body("DuncteBot is not in this server")
            return null
        }

        return guild
    }

}