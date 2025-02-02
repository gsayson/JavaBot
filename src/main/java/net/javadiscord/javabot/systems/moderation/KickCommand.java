package net.javadiscord.javabot.systems.moderation;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.javadiscord.javabot.data.config.BotConfig;
import net.javadiscord.javabot.systems.moderation.warn.dao.WarnRepository;
import net.javadiscord.javabot.systems.notification.NotificationService;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.javadiscord.javabot.util.Checks;
import net.javadiscord.javabot.util.Responses;

import java.util.concurrent.ExecutorService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * <h3>This class represents the /kick command.</h3>
 */
public class KickCommand extends ModerateUserCommand {
	private final NotificationService notificationService;
	private final WarnRepository warnRepository;
	private final ExecutorService asyncPool;

	/**
	 * The constructor of this class, which sets the corresponding {@link net.dv8tion.jda.api.interactions.commands.build.SlashCommandData}.
	 * @param notificationService The {@link NotificationService}
	 * @param botConfig The main configuration of the bot
	 * @param asyncPool The main thread pool for asynchronous operations
	 * @param warnRepository DAO for interacting with the set of {@link Warn} objects.
	 */
	public KickCommand(NotificationService notificationService, BotConfig botConfig, ExecutorService asyncPool, WarnRepository warnRepository) {
		super(botConfig);
		this.notificationService = notificationService;
		this.warnRepository = warnRepository;
		this.asyncPool = asyncPool;
		setModerationSlashCommandData(Commands.slash("kick", "Kicks a member")
				.addOption(OptionType.USER, "user", "The user to kick.", true)
				.addOption(OptionType.STRING, "reason", "The reason for kicking this user.", true)
				.addOption(OptionType.BOOLEAN, "quiet", "If true, don't send a message in the server channel where the kick is issued.", false)
		);
	}

	@Override
	protected WebhookMessageCreateAction<Message> handleModerationUserCommand(@Nonnull SlashCommandInteractionEvent event, @Nonnull Member commandUser, @Nonnull User target, @Nullable String reason) {
		if (!Checks.hasPermission(event.getGuild(), Permission.KICK_MEMBERS)) {
			return Responses.replyInsufficientPermissions(event.getHook(), Permission.KICK_MEMBERS);
		}
		boolean quiet = event.getOption("quiet", false, OptionMapping::getAsBoolean);
		ModerationService service = new ModerationService(notificationService, botConfig, event.getInteraction(), warnRepository, asyncPool);
		service.kick(target, reason, event.getMember(), event.getChannel(), quiet);
		return Responses.success(event.getHook(), "User Kicked", "%s has been kicked.", target.getAsMention());
	}
}