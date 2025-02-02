package net.javadiscord.javabot.systems.help;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.javadiscord.javabot.data.config.BotConfig;
import net.javadiscord.javabot.data.config.guild.HelpConfig;
import net.javadiscord.javabot.data.h2db.DbActions;
import net.javadiscord.javabot.systems.help.model.ChannelReservation;
import net.javadiscord.javabot.util.ExceptionLogger;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

/**
 * This listener is responsible for handling messages that are sent in one or
 * more designated help channels.
 */
@Slf4j
@RequiredArgsConstructor
public class HelpChannelListener extends ListenerAdapter {

	/**
	 * A static Map that holds all messages that was sent in a specific reserved channel.
	 */
	public static final Map<Long, List<Message>> reservationMessages = new HashMap<>();

	private final BotConfig botConfig;
	private final ScheduledExecutorService asyncPool;
	private final DbActions dbActions;
	private final HelpExperienceService helpExperienceService;

	@Override
	public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
		if (event.getAuthor().isBot() || event.getAuthor().isSystem() || event.getChannelType() != ChannelType.TEXT) {
			return;
		}
		HelpConfig config = botConfig.get(event.getGuild()).getHelpConfig();
		TextChannel channel = event.getChannel().asTextChannel();
		HelpChannelManager manager = new HelpChannelManager(botConfig, event.getGuild(),dbActions, asyncPool, helpExperienceService);

		// If a message was sent in an open text channel, reserve it.
		Category openChannelCategory = config.getOpenChannelCategory();
		if (openChannelCategory == null) {
			log.debug("Could not find Open Help Category for Guild {}", event.getGuild().getName());
			return;
		}
		if (openChannelCategory.equals(channel.getParentCategory())) {
			if (manager.mayUserReserveChannel(event.getAuthor())) {
				try {
					manager.reserve(channel, event.getAuthor(), event.getMessage());
				} catch (SQLException e) {
					ExceptionLogger.capture(e, getClass().getSimpleName());
					channel.sendMessage("An error occurred and this channel could not be reserved.").queue();
				}
			} else {
				event.getMessage().reply(config.getReservationNotAllowedMessage()).queue();
			}
		} else if (config.getReservedChannelCategory().equals(channel.getParentCategory())) {
			Optional<ChannelReservation> reservationOptional = manager.getReservationForChannel(event.getChannel().getIdLong());
			reservationOptional.ifPresent(reservation -> {
				List<Message> messages = new ArrayList<>();
				messages.add(event.getMessage());
				if (reservationMessages.containsKey(reservation.getId())) {
					messages.addAll(reservationMessages.get(reservation.getId()));
				}
				reservationMessages.put(reservation.getId(), messages);
			});
		} else if (config.getDormantChannelCategory().equals(channel.getParentCategory())) {
			// Prevent anyone from sending messages in dormant channels.
			event.getMessage().delete().queue();
		}
	}
}
