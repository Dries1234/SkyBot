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

package ml.duncte123.skybot.utils;

import com.jagrosh.jagtag.JagTag;
import com.jagrosh.jagtag.Parser;
import com.jagrosh.jagtag.libraries.Functional;
import ml.duncte123.skybot.objects.jagtag.Methods;

public class CustomCommandUtils {

    public static final Parser PARSER = JagTag.newDefaultBuilder()
            .addMethods(Methods.getMethods())
            .addMethods(Functional.getMethods())
            .build();
}