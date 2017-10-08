package ml.duncte123.skybot.commands.animals;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.EmbedUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import java.net.URL;

public class KittyCommand extends Command {

    /**
     * This is the executeCommand of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {
        try {
            String apiKey = AirUtils.config.getString("apis.thecatapi", "");
            System.out.println(apiKey);
            Document raw = Jsoup.connect("http://thecatapi.com/api/images/get?" +
                    (!apiKey.isEmpty()? "api_key=" + apiKey + "&" : "") + "format=xml&results_per_page=1").get();
            Document doc = Jsoup.parse(raw.getAllElements().html(), "", Parser.xmlParser());
            event.getChannel().sendFile(new URL(doc.select("url").first().text()).openStream(),
                    "Kitty_" + System.currentTimeMillis() + ".png", null).queue();
        }
        catch (Exception e) {
            sendEmbed(event, EmbedUtils.embedMessage("ERROR: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    /**
     * The usage instructions of the command
     * @return a String
     */
    @Override
    public String help() {
        return "A alternative cat command with more kitties";
    }

    @Override
    public String getName() {
        return "kitty";
    }
}
