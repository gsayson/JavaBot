package net.javadiscord.javabot.systems.help.naming_strategies;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.javadiscord.javabot.data.config.guild.HelpConfig;

import java.util.List;

/**
 * A strategy to use to generate names for new help channels as they're needed.
 */
public interface ChannelNamingStrategy {
	String getName(List<TextChannel> channels, HelpConfig config);
}
