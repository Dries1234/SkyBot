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

import com.afollestad.ason.Ason
import com.jagrosh.jdautilities.oauth2.OAuth2Client
import com.jagrosh.jdautilities.oauth2.Scope
import com.jagrosh.jdautilities.oauth2.session.Session
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.SkyBot
import ml.duncte123.skybot.objects.WebVariables
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.AirUtils.CONFIG
import ml.duncte123.skybot.utils.AudioUtils
import ml.duncte123.skybot.utils.GuildSettingsUtils
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Guild
import spark.ModelAndView
import spark.Request
import spark.Response
import spark.Spark.path
import spark.kotlin.*
import spark.template.jtwig.JtwigTemplateEngine

class WebServer {

    private val engine = JtwigTemplateEngine("views")
    private val oAuth2Client = OAuth2Client.Builder()
            .setClientId(CONFIG.getLong("discord.oauth.clientId", 210363111729790977))
            .setClientSecret(CONFIG.getString("discord.oauth.clientSecret", "aaa"))
            .build()

    fun activate() {
        //Port has to be 2000 because of the apache proxy on the vps
        port(2000)

        staticFiles.location("/public")

        get("/", WebVariables().put("title", "Home"), "home.twig")

        get("/commands", WebVariables().put("title", "List of commands").put("prefix", Settings.PREFIX)
                .put("commands", AirUtils.COMMAND_MANAGER.sortedCommands), "commands.twig")


        path("/dashboard") {

            /*before ("") {
                if(!request.session().attributes().contains("sessionId"))*//* {
                oAuth2Client.sessionController.getSession(request.session().attribute("sessionId"))
            } else *//*{
                    val url = oAuth2Client.generateAuthorizationURL(
                            CONFIG.getString("discord.oauth.redirUrl", "http://localhost:2000/callback"),
                            Scope.IDENTIFY, Scope.GUILDS
                    )
                    request.session(true).attribute("sessionId", "session_${System.currentTimeMillis()}")
                    response.redirect(url)
                }
            }*/

            get("", WebVariables().put("title", "Dashboard"), "dashboard.twig")
        }


        path("/server/:guildid") {

            before("*") {
                if(!request.session().attributes().contains("sessionId")) {
                    response.redirect("/dashboard")
                }
                val guild = getGuildFromRequest(request, response)
                if (guild == null && !request.uri().contains("invalid")) {
                    response.redirect("/server/${request.params(":guildid*")}/invalid")
                } else if (guild != null && request.uri().contains("invalid")) {
                    response.redirect("/server/${request.params(":guildid*")}")
                }
            }

            //overview and editing
            get("") {
                val guild = getGuildFromRequest(request, response)
                if (guild != null) {
                    val settings = GuildSettingsUtils.getGuild(guild)
                    return@get """<p>Guild prefix: ${settings.customPrefix}</p>
                    |<p>Join Message: ${settings.customJoinMessage}</p>
                    """.trimMargin()
                } else { }
            }
            //audio stuff
            get("/music") {
                val guild = getGuildFromRequest(request, response)
                if (guild != null) {
                    val mng = AudioUtils.ins.getMusicManager(guild, false)

                    if (mng != null) {
                        return@get """<p>Audio player details:</p>
                            |<p>Currently playing: <b>${if (mng.player.playingTrack != null) mng.player.playingTrack.info.title else "nothing"}</b></p>
                            |<p>Total tracks in queue: <b>${mng.scheduler.queue.size}</b></p>
                        """.trimMargin()
                    } else {
                        return@get "The audio player does not seem to be active"
                    }
                } else { }
            }

            //when the guild is not found
            get("/invalid") {
                response.status(404)
                "DuncteBot is not in the requested server, why don't you <a href=\"#\">invite it</a>?"
            }
        }

        get("/callback") {
            oAuth2Client.startSession(
                    request.queryParams("code"),
                    request.queryParams("state"),
                    request.session().attribute("sessionId")
            ).complete()
            response.redirect("/dashboard")
            /*.queue ({
                //request.session().attribute("session", it)
                return@queue response.redirect("/dashboard")
            }, {
                halt(it.message + "")
            })*/
        }

        get("/api/servers") {
            response.type("application/json")
            return@get Ason()
                    .put("status", "success")
                    .put("server_count", SkyBot.getInstance().shardManager.guildCache.size())
                    .put("code", response.status())
        }

        path("/api") {
            get("/getUserGuilds") {
                response.type("application/json")
                return@get Ason()
                        .put("status", "success")
                        .put("guilds", oAuth2Client.getGuilds(getSession(request)).complete().filter {
                            it.hasPermission(Permission.ADMINISTRATOR) || it.hasPermission(Permission.MANAGE_SERVER) }
                                .toCollection(arrayListOf()))
                        .put("code", response.status())
            }
        }

        notFound {
            engine.render(ModelAndView(WebVariables().put("title", "404").put("path", request.pathInfo()).map,
                    "errors/404.twig"))
        }

        /*after("*") {
            response.body()
        }*/
    }

    private fun get(path: String, map: WebVariables, model: String) {
        get(path, DEFAULT_ACCEPT, engine) {
            ModelAndView(map.map, model)
        }
    }

    private fun getGuildFromRequest(request: Request, response: Response): Guild? {

        val guildId = if (!request.params(":guildid").isNullOrEmpty())
            request.params(":guildid")
        else request.params(":guildid*")

        val guild = SkyBot.getInstance()
                .shardManager.getGuildById(guildId)

        if (guild == null) {
            response.body("DuncteBot is not in this server")
            return null
        }

        return guild
    }

    private fun getSession(request: Request) : Session {
        return oAuth2Client.sessionController.getSession(request.session().attribute("sessionId"))
    }
}