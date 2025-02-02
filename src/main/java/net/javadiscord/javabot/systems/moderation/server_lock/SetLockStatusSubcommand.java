package net.javadiscord.javabot.systems.moderation.server_lock;

import com.dynxsty.dih4jda.interactions.commands.SlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.javadiscord.javabot.data.config.BotConfig;
import net.javadiscord.javabot.data.config.GuildConfig;
import net.javadiscord.javabot.util.Checks;
import net.javadiscord.javabot.util.Responses;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

/**
 * <h3>This class represents the /serverlock-admin set-status command.</h3>
 */
public class SetLockStatusSubcommand extends SlashCommand.Subcommand {
	private final ServerLockManager serverLockManager;
	private final BotConfig botConfig;

	/**
	 * The constructor of this class, which sets the corresponding {@link SubcommandData}.
	 * @param serverLockManager the service containing functionality regarding the server lock
	 * @param botConfig The main configuration of the bot
	 */
	public SetLockStatusSubcommand(ServerLockManager serverLockManager, BotConfig botConfig) {
		this.serverLockManager = serverLockManager;
		this.botConfig = botConfig;
		setSubcommandData(new SubcommandData("set-status", "Command for changing the current server lock status.")
				.addOption(OptionType.BOOLEAN, "locked", "Whether the server should be locked or not.", true));
	}

	@Override
	public void execute(@NotNull SlashCommandInteractionEvent event) {
		OptionMapping lockedMapping = event.getOption("locked");
		if (lockedMapping == null) {
			Responses.replyMissingArguments(event).queue();
			return;
		}
		if (event.getGuild() == null || event.getMember() == null) {
			Responses.replyGuildOnly(event).queue();
			return;
		}
		if (!Checks.hasStaffRole(botConfig, event.getMember())) {
			Responses.replyStaffOnly(event, botConfig.get(event.getGuild())).queue();
			return;
		}
		GuildConfig config = botConfig.get(event.getGuild());
		boolean locked = lockedMapping.getAsBoolean();
		if (locked == config.getServerLockConfig().isLocked()) {
			Responses.info(event, String.format("Server already %slocked", locked ? "" : "un"),
					"The server is already %slocked!", locked ? "" : "un").queue();
			return;
		}
		config.getServerLockConfig().setLocked(String.valueOf(locked));
		botConfig.flush();
		if (locked) {
			serverLockManager.lockServer(event.getGuild(), Collections.emptyList(), event.getUser());
		} else {
			serverLockManager.unlockServer(event.getGuild(), event.getUser());
		}
		Responses.info(event, "Server Lock Status", "Successfully %slocked the current server!", locked ? "" : "un").queue();
	}
}
