package com.javadiscord.javabot;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.javadiscord.javabot.events.*;
import com.javadiscord.javabot.properties.ConfigString;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.reflections.Reflections;

import java.util.Objects;


public class Bot {

    public static JDA jda;
    public static EventWaiter waiter;


    public static void main(String[] args) throws Exception {

            ConfigString token = new ConfigString("token", "null");
            waiter = new EventWaiter();

            CommandClientBuilder client = new CommandClientBuilder()
                    .setOwnerId("374328434677121036")
                    .setCoOwnerIds("299555811804315648", "620615131256061972")
                    .setPrefix("!")
                    .setEmojis("✅", "⚠️", "❌")
                    .useHelpBuilder(false)
                    .addCommands(discoverCommands());


            jda = JDABuilder.createDefault(token.getValue())
                    .setStatus(OnlineStatus.DO_NOT_DISTURB)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableCache(CacheFlag.ACTIVITY)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES)
                    .build();

            jda.addEventListener(waiter, client.build());

            //EVENTS
            jda.addEventListener(new GuildJoin());
            jda.addEventListener(new UserJoin());
            jda.addEventListener(new UserLeave());
            jda.addEventListener(new Startup());
            jda.addEventListener(new StatusUpdate());
            jda.addEventListener(new ReactionListener());
            jda.addEventListener(new SuggestionListener());
            jda.addEventListener(new CstmCmdListener());
            jda.addEventListener(new AutoMod());
            jda.addEventListener(new SubmissionListener());
            jda.addEventListener(new SlashCommands());
            //jda.addEventListener(new StarboardListener());
    }

    /**
     * Discovers and instantiates all commands found in the bot's "commands"
     * package. This uses the reflections API to find all classes in that
     * package which extend from the base {@link Command} class.
     * <p>
     *     <strong>All command classes MUST have a no-args constructor.</strong>
     * </p>
     * @return The array of commands.
     */
    private static Command[] discoverCommands() {
        Reflections reflections = new Reflections("com.javadiscord.javabot.commands");
        return reflections.getSubTypesOf(Command.class).stream()
            .map(type -> {
                try {
                    return (Command) type.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .toArray(Command[]::new);
    }
}

