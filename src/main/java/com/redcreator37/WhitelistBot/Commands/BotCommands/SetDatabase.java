package com.redcreator37.WhitelistBot.Commands.BotCommands;

import com.redcreator37.WhitelistBot.Commands.BotCommand;
import com.redcreator37.WhitelistBot.Commands.CommandUtils;
import com.redcreator37.WhitelistBot.DataModels.Guild;
import com.redcreator37.WhitelistBot.Database.GameHandling.SharedDbProvider;
import com.redcreator37.WhitelistBot.Localizations;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Adds new / modifies existing external database connection data
 * in the guild's {@link com.redcreator37.WhitelistBot.Database.GameHandling.SharedDbProvider}
 */
public class SetDatabase extends BotCommand {

    public SetDatabase() {
        super("setdatabase", "Modifies the database connection data",
                new HashMap<String, Boolean>() {{
                    put("server", true);
                    put("database", true);
                    put("username", true);
                    put("password", false);
                }});
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
        if (!this.checkValidity(args, event, context).block()) return Mono.empty();
        String password = args.size() < 4 ? "" : args.get(4);   // allow empty passwords
        SharedDbProvider provider = new SharedDbProvider(context.getSnowflake(),
                args.get(1), args.get(3), password, args.get(2));
        context.setSharedDbProvider(provider);
        return CommandUtils.getMessageChannel(event).createEmbed(spec -> {
            spec.setTitle(Localizations.lc("db-data-changed"));
            spec.setColor(Color.CYAN);
            spec.addField(Localizations.lc("server"), provider.getDbServer(), true);
            spec.addField(Localizations.lc("db-name"), provider.getDbName(), true);
            spec.addField(Localizations.lc("username"), provider.getUsername(), true);
            spec.setDescription(Localizations.lc("connecting-to-db-shortly"));
            CommandUtils.setSelfAuthor(event.getGuild(), spec);
            spec.setTimestamp(Instant.now());
            CommandUtils.attemptConnectDb(Objects.requireNonNull(event.getMessage()
                    .getChannel().block()), Objects.requireNonNull(event.getGuild().block()));
        }).then();
    }
}
