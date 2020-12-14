package com.redcreator37.WhitelistBot.Commands.BotCommands;

import com.redcreator37.WhitelistBot.Commands.BotCommand;
import com.redcreator37.WhitelistBot.Commands.CommandUtils;
import com.redcreator37.WhitelistBot.DataModels.Guild;
import com.redcreator37.WhitelistBot.Database.GameHandling.SharedDbProvider;
import com.redcreator37.WhitelistBot.DiscordBot;
import com.redcreator37.WhitelistBot.Localizations;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.List;

/**
 * Embeds the current database data into the chat
 */
public class EmbedDatabaseData extends BotCommand {

    public EmbedDatabaseData() {
        super("getdatabase", Localizations.lc("displays-current-db"), null);
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
        if (CommandUtils.checkDbNotPresent(event, context)) return Mono.empty();
        return CommandUtils.getMessageChannel(event).createEmbed(spec -> {
            SharedDbProvider provider = context.getSharedDbProvider();
            spec.setTitle(Localizations.lc("db-connect-data"));
            spec.setColor(Color.GREEN);
            spec.addField(Localizations.lc("server"), provider.getDbServer(), true);
            spec.addField(Localizations.lc("db-name"), provider.getDbName(), true);
            spec.addField(Localizations.lc("username"), provider.getUsername(), true);
            spec.setDescription(MessageFormat.format(Localizations.lc("to-change-db-run"),
                    DiscordBot.cmdPrefix));
            CommandUtils.setSelfAuthor(event.getGuild(), spec);
            spec.setTimestamp(Instant.now());
        }).then();
    }
}
