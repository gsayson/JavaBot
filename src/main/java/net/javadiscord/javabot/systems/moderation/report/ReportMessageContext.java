package net.javadiscord.javabot.systems.moderation.report;

import com.dynxsty.dih4jda.interactions.commands.ContextCommand;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.javadiscord.javabot.data.config.BotConfig;
import net.javadiscord.javabot.util.Responses;

/**
 * <h3>This class represents the "Report Message" Message Context Menu command.</h3>
 */
public class ReportMessageContext extends ContextCommand.Message {
	private final BotConfig botConfig;

	/**
	 * The constructor of this class, which sets the corresponding {@link net.dv8tion.jda.api.interactions.commands.build.CommandData}.
	 * @param botConfig The main configuration of the bot
	 */
	public ReportMessageContext(BotConfig botConfig) {
		this.botConfig = botConfig;
		setCommandData(Commands.message("Report Message")
				.setGuildOnly(true)
		);
	}

	@Override
	public void execute(MessageContextInteractionEvent event) {
		if (event.getTarget().getAuthor().equals(event.getUser())) {
			Responses.error(event, "You cannot perform this action on yourself.").queue();
			return;
		}
		event.replyModal(new ReportManager(botConfig).buildMessageReportModal(event)).queue();
	}
}
