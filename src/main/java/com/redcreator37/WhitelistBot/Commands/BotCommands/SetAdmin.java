package com.redcreator37.WhitelistBot.Commands.BotCommands;

import com.redcreator37.WhitelistBot.Commands.BotCommand;
import com.redcreator37.WhitelistBot.Commands.CommandUtils;
import com.redcreator37.WhitelistBot.DataModels.Guild;
import com.redcreator37.WhitelistBot.DiscordBot;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Sets the administrator role for this {@link Guild}
 */
public class SetAdmin extends BotCommand {

    public SetAdmin(String requiredRole) {
        super("setadmin", "Sets the administrator role",
                new HashMap<String, Boolean>() {{
                    put("playerName", false);
                }}, requiredRole);
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
    @SuppressWarnings({"BlockingMethodInNonBlockingContext", "OptionalGetWithoutIsPresent"})
    @Override
    public Mono<Void> execute(List<String> args, Guild context, MessageCreateEvent event) {
        // fixme: localizations
        if (!this.checkValidity(args, event).block()) return Mono.empty();
        // get the entered role or the highest role of the invoking member
        String adminRole = args.size() > 1 ? args.get(1) : Objects.requireNonNull(event
                .getMember().get().getHighestRole().block()).getName();
        context.setAdminRole(adminRole);
        return CommandUtils.getMessageChannel(event).createEmbed(spec -> {
            spec.setTitle("Admin role changed");
            spec.setColor(Color.CYAN);
            spec.addField(MessageFormat.format("The admin role is now `{0}`", adminRole),
                    MessageFormat.format("To change it again, use the `{0}{1}` command",
                            DiscordBot.cmdPrefix, this.getName()), false);
            CommandUtils.setSelfAuthor(event.getGuild(), spec);
            spec.setTimestamp(Instant.now());
        }).then();
    }
}
