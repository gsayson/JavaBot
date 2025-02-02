package net.javadiscord.javabot.systems.staff_commands.tags.commands;

import com.dynxsty.dih4jda.interactions.commands.SlashCommand;

import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.javadiscord.javabot.systems.moderation.CommandModerationPermissions;

/**
 * Represents the `/tag-admin` command. This holds administrative commands for managing "Custom Tags".
 */
public class TagsAdminCommand extends SlashCommand implements CommandModerationPermissions {
	/**
	 * This classes constructor which sets the {@link net.dv8tion.jda.api.interactions.commands.build.SlashCommandData} and
	 * adds the corresponding {@link net.dv8tion.jda.api.interactions.commands.Command.Subcommand}s.
	 * @param createCustomTagSubcommand /tag-admin create
	 * @param deleteCustomTagSubcommand /tag-admin delete
	 * @param editCustomTagSubcommand /tag-admin edit
	 */
	public TagsAdminCommand(CreateCustomTagSubcommand createCustomTagSubcommand, DeleteCustomTagSubcommand deleteCustomTagSubcommand, EditCustomTagSubcommand editCustomTagSubcommand) {
		setModerationSlashCommandData(Commands.slash("tag-admin", "Administrative commands for managing \"Custom Tags\"."));
		addSubcommands(createCustomTagSubcommand, deleteCustomTagSubcommand, editCustomTagSubcommand);
	}
}