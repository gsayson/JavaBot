package net.javadiscord.javabot.systems.moderation;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.javadiscord.javabot.command.ResponseException;
import net.javadiscord.javabot.command.Responses;
import net.javadiscord.javabot.command.moderation.UserModerationAction;

/**
 * Command that allows staff-members to ban guild members.
 */
public class BanCommand extends UserModerationAction {

	@Override
	protected ReplyCallbackAction handleModerationActionCommand(SlashCommandInteractionEvent event, Member commandUser, Member target) throws ResponseException {
		var reasonOption = event.getOption("reason");

		if (reasonOption == null) {
			return Responses.error(event, "Missing required arguments.");
		}

		var reason = reasonOption.getAsString();
		var quietOption = event.getOption("quiet");
		boolean quiet = quietOption != null && quietOption.getAsBoolean();

		var moderationService = new ModerationService(event.getInteraction());
		if (moderationService.ban(target, reason, commandUser, event.getTextChannel(), quiet)) {
			return Responses.success(event, "User Banned", String.format("%s has been banned.", target.getAsMention()));
		} else {
			return Responses.warning(event, "You're not permitted to ban this user.");
		}
	}
}