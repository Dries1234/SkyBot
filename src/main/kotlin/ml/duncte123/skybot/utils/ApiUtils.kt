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

package ml.duncte123.skybot.utils

import com.mongodb.client.model.Filters
import ml.duncte123.skybot.objects.api.KpopObject
import ml.duncte123.skybot.objects.api.LlamaObject
import ml.duncte123.skybot.objects.api.WarnObject
import ml.duncte123.skybot.objects.api.Warning
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.util.*

object ApiUtils {


    @JvmStatic
    fun getRandomLlama(): LlamaObject {

        val conn = AirUtils.DB.getConnManager().connection

        val resultSet = conn.createStatement()
                .executeQuery("SELECT * FROM animal_apis ORDER BY RAND() LIMIT 1")
        resultSet.next()
        val obj = LlamaObject(resultSet.getInt("id"), resultSet.getString("file"))
        conn.close()

        return obj
    }

    @JvmStatic
    fun getRandomKpopMember(search: String = ""): KpopObject {

        val conn = AirUtils.DB.getConnManager().connection

        lateinit var resultSet: ResultSet
        resultSet = if (!search.isEmpty()) {
            val stmt = conn.prepareStatement("SELECT * FROM kpop WHERE name LIKE ? OR id=? LIMIT 1")
            stmt.setString(1, "%$search%")
            stmt.setString(2, search)
            stmt.executeQuery()
        } else {
            conn.createStatement().executeQuery("SELECT * FROM kpop ORDER BY RAND() LIMIT 1")
        }
        resultSet.next()
        val obj = KpopObject(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("band"),
                resultSet.getString("img")
        )
        conn.close()

        return obj
    }

    @JvmStatic
    fun getWarnsForUser(userId: String, guildId: String): WarnObject {

        val warnings = ArrayList<Warning>()

        AirUtils.MONGO_SYNC_WARNINGS.find(
                Filters.and(
                        Filters.eq("user_id", userId.toLong()),
                        Filters.eq("guild_id"),
                        Filters.gte("expire_date", OffsetDateTime.now().toEpochSecond())
            )).forEach { warnings.add(it) }


        return WarnObject(userId, warnings)
    }

}