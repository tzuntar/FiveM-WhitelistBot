package com.redcreator37.WhitelistBot.Commands.BotCommands;

import com.redcreator37.WhitelistBot.Commands.BotCommand;
import com.redcreator37.WhitelistBot.Commands.CommandUtils;
import com.redcreator37.WhitelistBot.DataModels.Guild;
import com.redcreator37.WhitelistBot.DiscordBot;
import com.redcreator37.WhitelistBot.Localizations;
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

    public SetAdmin() {
        super("setadmin", Localizations.lc("sets-admin-role"),
                new HashMap<String, Boolean>() {{
                    put("playerName", false);
                }});
    }

    /**
     * Runs the action for this command
     *
     * @param args    the command arguments entered, can be {@code null}
     *                if none are required
     * @param context the {@link Guild} context in which to run the
     *                command. Can be {@code null} if no guild is
     *                tied to the command's working.
     * @param event   the {@link MessageCreateEvent} which occurred
     *                when the message was sent
     * @return an empty {@link Mono} object
     */
    @SuppressWarnings({"BlockingMethodInNonBlockingContext", "OptionalGetWithoutIsPresent"})
    @Override
    public Mono<Void> execute(List<String> args, Guild context, MessageCreateEvent event) {
        if (!this.checkValidity(args, event, context).block()) return Mono.empty();
        // get the entered role or the highest role of the invoking member
        String adminRole = args.size() > 1 ? args.get(1) : Objects.requireNonNull(event
                .getMember().get().getHighestRole().block()).getName();
        context.setAdminRole(adminRole);
        return CommandUtils.getMessageChannel(event).createEmbed(spec -> {
            spec.setTitle(Localizations.lc("admin-role-changed"));
            spec.setColor(Color.CYAN);
            spec.addField(MessageFormat.format(Localizations.lc("admin-role-now"), adminRole),
                    MessageFormat.format(Localizations.lc("to-change-admin-run"),
                            DiscordBot.cmdPrefix, this.getName()), false);
            CommandUtils.setSelfAuthor(event.getGuild(), spec);
            spec.setTimestamp(Instant.now());
        }).then();
    }
}
