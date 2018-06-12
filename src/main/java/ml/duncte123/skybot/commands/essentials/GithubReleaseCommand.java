package ml.duncte123.skybot.commands.essentials;

import com.github.natanbc.reliqua.request.RequestException;
import me.duncte123.botCommons.web.WebUtils;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.MessageUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class GithubReleaseCommand extends Command {

    private static final String GITHUB_API = "https://api.github.com";
    private static final String REPO_PART = "/repos/DuncteBot/SkyBot";

    private static final String CREATE_RELEASE = GITHUB_API + REPO_PART + "/releases?access_token="
            + AirUtils.CONFIG.getString("apis.github");

    /*private static final String UPDATE_RELEASE = GITHUB_API + REPO_PART + "/releases/%s?access_token="
            + AirUtils.CONFIG.getString("apis.github");

    private static final String UPLOAD_ASSET = "https://uploads.github.com" + REPO_PART + "/releases/%s/assets?name=%s&access_token="
            + AirUtils.CONFIG.getString("apis.github");*/

    @Override
    public void executeCommand(@NotNull String invoke, @NotNull String[] args, @NotNull GuildMessageReceivedEvent event) {
        if (!isDev(event.getAuthor())
                && !Settings.OWNER_ID.equals(event.getAuthor().getId())) {
            MessageUtils.sendError(event.getMessage());
            MessageUtils.sendMsg(event, "You must be the bot owner to run this command!");

            return;
        }

        //get the version name
        String name = "v" + Settings.VERSION.split("_")[0];

        // The message from after the {prefix}{invoke} syntax
        String message = event.getMessage().getContentDisplay();
        message = message.substring(message.indexOf(invoke) + invoke.length());

        /*
         * Format is:
         *
         * Skybot <version>, release authored by <author>
         *
         * <given message>
         */
        message = String.format("SkyBot %s, release authored by %s%n%n%s",
                name,
                event.getAuthor().getName() + '#' + event.getAuthor().getDiscriminator(),
                message);

        JSONObject releaseOut = new JSONObject()
                .put("tag_name", name)
                .put("name", name)
                .put("target_commitish", "dev")
                .put("body", message)
                .put("draft", false)
                .put("prerelease", false);

        try {
            //You meant to post the json ramid?
            JSONObject releaseIn = WebUtils.ins.postJSON(CREATE_RELEASE, releaseOut,
                    mapper -> {
                        assert mapper.body() != null;
                        return new JSONObject(mapper.body().string());
                    }).execute();

            if (releaseIn == null)
                return;

            System.out.println(releaseIn);
            MessageUtils.sendMsg(event, "Release uploaded");
        } catch (RequestException e) {
            MessageUtils.sendError(event.getMessage());
            MessageUtils.sendMsg(event, "An error occurred creating a release, it has been logged in the console");
        }
    }

    @Override
    public String help() {
        return "Releases a new version of the bot to GitHub, if configured";
    }

    @Override
    public String getName() {
        return "release";
    }
}
