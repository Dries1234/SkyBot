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

package ml.duncte123.skybot.commands.guild.owner;

import ml.duncte123.skybot.objects.api.GuildSettings;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.custom.CustomCommand;
import ml.duncte123.skybot.objects.command.custom.CustomCommandImpl;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.MessageUtils;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static ml.duncte123.skybot.utils.MessageUtils.sendMsg;

public class CustomCommandCommand extends Command {

    private final List<String> systemInvokes = List.of("add", "new", "edit", "change", "delete");

    @Override
    public void executeCommand(@NotNull String invoke, @NotNull String[] args, @NotNull GuildMessageReceivedEvent event) {

        if (args.length < 1) {
            sendMsg(event, "Insufficient arguments use `db!help customcommand`");
            return;
        }

        switch (args.length) {
            case 1:
                invokeCustomCommand(args[0], event);
                break;

            case 2:
                deleteCustomCommand(args, event);
                //sendMsg(event, "Insufficient arguments");
                break;

            default:
                addOrEditCustomCommand(args, event);
                break;
        }
    }

    private void invokeCustomCommand(String arg, GuildMessageReceivedEvent event) {
        if (arg.equalsIgnoreCase("list")) {
            GuildSettings s = getSettings(event.getGuild());
            StringBuilder sb = new StringBuilder();
            AirUtils.COMMAND_MANAGER.getCustomCommands().stream()
                    .filter(c -> c.getGuildId().equals(event.getGuild().getId()))
                    .forEach(cmd -> {
                                sb.append(s.getCustomPrefix())
                                        .append(cmd.getName())
                                        .append("\n");
                            }
                    );

            sendMsg(event, new MessageBuilder().append("Custom Commands for this server").append('\n')
                    .appendCodeBlock(sb.toString(), "ldif").build());
        } else {
            //fetch a custom command
            CustomCommand cmd = AirUtils.COMMAND_MANAGER.getCustomCommand(arg, event.getGuild().getId());
            if (cmd != null)
                //Run the custom command?
                AirUtils.COMMAND_MANAGER.dispatchCommand((Command) cmd, arg, new String[0], event);
            else
                sendMsg(event, "Invalid arguments use `db!help customcommand`");
        }
    }

    private void deleteCustomCommand(String[] args, GuildMessageReceivedEvent event) {
        //Check for deleting
        if (!args[0].equalsIgnoreCase("delete")) {
            sendMsg(event, "Invalid arguments use `db!help customcommand`");
            return;
        }

        if (!isAdmin(event)) {
            sendMsg(event, "You need the \"Administrator\" permission to add or remove commands");
            return;
        }

        final String commandName = args[1];
        final String guildid = event.getGuild().getId();

        if (!commandExists(commandName, guildid)) {
            sendMsg(event, "No command was found for this name");
            return;
        }

        AirUtils.COMMAND_MANAGER.removeCustomCommand(event, commandName, guildid);
    }

    private void addOrEditCustomCommand(String[] args, GuildMessageReceivedEvent event) {
        if (args.length < 3 && !systemInvokes.contains(args[0])) {
            sendMsg(event, "Invalid arguments use `db!help customcommand`");
            return;
        }

        if (!isAdmin(event)) {
            sendMsg(event, "You need the \"Administrator\" permission to add or remove commands");
            return;
        }
        //new command
        String commandName = args[1];

        if (commandName.length() > 10) {
            MessageUtils.sendErrorWithMessage(event.getMessage(), "The maximum length of the command name is 10 characters");
            return;
        }

        String commandAction = StringUtils.join(Arrays.copyOfRange(args, 2, args.length), " ");
        String guildId = event.getGuild().getId();
        if (commandExists(commandName, guildId)) {
            if (!args[0].equalsIgnoreCase("edit") && !args[0].equalsIgnoreCase("change")) {
                sendMsg(event, "A command already exists for this server.");
            } else {
                editCustomCommand(event, AirUtils.COMMAND_MANAGER.getCustomCommand(commandName, guildId), commandAction);
            }
            return;
        }
        registerCustomCommand(event, commandName, commandAction, guildId);

    }

    private boolean commandExists(String name, String guild) {
        return AirUtils.COMMAND_MANAGER.getCustomCommand(name, guild) != null;
    }

    private void registerCustomCommand(GuildMessageReceivedEvent event, String name, String action, String guildId) {
        AirUtils.COMMAND_MANAGER.addCustomCommand(event, new CustomCommandImpl(name, action, guildId));
    }

    private void editCustomCommand(GuildMessageReceivedEvent event, CustomCommand customCommand, String newMessage) {
        CustomCommand cmd = new CustomCommandImpl(customCommand.getName(), newMessage, customCommand.getGuildId());
        AirUtils.COMMAND_MANAGER.editCustomCommand(event, cmd);
    }

    @Override
    public String help() {
        return "Create, run and delete custom commands\n" +
                "`" + PREFIX + getName() + " list` => Shows a list of all the custom commands\n" +
                "`" + PREFIX + getName() + " new <name> <text>` creates a new custom command\n" +
                "`" + PREFIX + getName() + " delete <name>` => deletes a custom command";
    }

    @Override
    public String getName() {
        return "customcommand";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"cc", "customcommands"};
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isAdmin(GuildMessageReceivedEvent event) {
        return event.getMember().hasPermission(event.getChannel(), Permission.ADMINISTRATOR);
    }
}
