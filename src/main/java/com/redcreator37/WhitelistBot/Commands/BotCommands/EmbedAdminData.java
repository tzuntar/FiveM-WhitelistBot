package com.redcreator37.WhitelistBot.Commands.BotCommands;

import com.redcreator37.WhitelistBot.Commands.BotCommand;
import com.redcreator37.WhitelistBot.Commands.CommandUtils;
import com.redcreator37.WhitelistBot.DataModels.Guild;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

/**
 * Embeds data about the current admin role into the channel
 */
public class EmbedAdminData extends BotCommand {

    public EmbedAdminData(String requiredRole) {
        super("getadmin", "Displays the current admin role", null, requiredRole);
    }

    /**
     * Runs the action for this command
     *
     * @param args    the command arguments entered, can be <code>null</code>
     *                if none are required
     * @param context the {@link Guild} context in which to run the
     *                command. Can be <code>null</code> if no guild is
     *                tied to the command's working.
     * @param event   the {@link MessageCreateEvent} which occurred
     *                when the message was sent
     * @return an empty {@link Mono} object
     */
    @Override
    public Mono<Void> execute(List<String> args, Guild context, MessageCreateEvent event) {
        // fixme: localizations
        if (!this.checkValidity(args, event).block()) return Mono.empty();
        return CommandUtils.getMessageChannel(event).createEmbed(spec -> {
            if (context.getAdminRole() == null) {
                spec.setTitle("No admin role defined");
                spec.setColor(Color.RED);
                spec.addField("There's currently no admin role defined yet",
                        "Use the command `%setadmin` to set the admin role", false);
            } else {
                spec.setColor(Color.YELLOW);
                spec.addField("The current admin role is " + context.getAdminRole(),
                        "To change it, use the `%setadmin` command", false);
            }
            CommandUtils.setSelfAuthor(event.getGuild(), spec);
            spec.setTimestamp(Instant.now());
        }).then();
    }
}
