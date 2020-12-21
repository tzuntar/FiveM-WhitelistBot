package com.redcreator37.WhitelistBot.Commands.BotCommands;

import com.redcreator37.WhitelistBot.Commands.BotCommand;
import com.redcreator37.WhitelistBot.Commands.CommandUtils;
import com.redcreator37.WhitelistBot.DataModels.Guild;
import com.redcreator37.WhitelistBot.DiscordBot;
import com.redcreator37.WhitelistBot.Localizations;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Removes the current guild from the internal database and kicks the bot
 */
public class LeaveGuild extends BotCommand {

    public LeaveGuild() {
        super("kickbot", Localizations.lc("kicks-the-bot"), null);
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
    @SuppressWarnings("BlockingMethodInNonBlockingContext")
    @Override
    public Mono<Void> execute(List<String> args, Guild context, MessageCreateEvent event) {
        if (!this.checkValidity(args, event, context).block()) return Mono.empty();
        MessageChannel channel = CommandUtils.getMessageChannel(event);
        if (!DiscordBot.removeGuild(context).block())
            return channel.createEmbed(spec -> {
                spec.setTitle(Localizations.lc("error"));
                spec.setColor(Color.RED);
                spec.addField(Localizations.lc("leaving-failed"),
                        Localizations.lc("leaving-failed-try-again-later"), false);
                CommandUtils.setSelfAuthor(event.getGuild(), spec);
                spec.setTimestamp(Instant.now());
            }).then();
        channel.createEmbed(spec -> {
            spec.setTitle(Localizations.lc("bye"));
            spec.addField(Localizations.lc("bye-longer"),
                    Localizations.lc("leaving-the-guild"), false);
            spec.setColor(Color.SUBMARINE);
            CommandUtils.setSelfAuthor(event.getGuild(), spec);
            spec.setTimestamp(Instant.now());
        }).block();
        return Objects.requireNonNull(event.getGuild().block()).leave();
    }

}
