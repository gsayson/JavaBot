package net.javadiscord.javabot.systems.help.commands;

import net.javadiscord.javabot.systems.help.commands.subcommands.HelpAccountSubcommand;

/**
 * Handler class for all Help Commands.
 */
public class HelpCommandHandler extends DelegatingCommandHandler {
	/**
	 * Adds all subcommands.
	 * {@link DelegatingCommandHandler#addSubcommand}
	 */
	public HelpCommandHandler() {
		this.addSubcommand("account", new HelpAccountSubcommand());
	}
}
