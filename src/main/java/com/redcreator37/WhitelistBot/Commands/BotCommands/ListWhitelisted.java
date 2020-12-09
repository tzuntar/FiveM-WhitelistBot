package com.redcreator37.WhitelistBot.Commands.BotCommands;

import com.redcreator37.WhitelistBot.Commands.BotCommand;
import com.redcreator37.WhitelistBot.Commands.CommandUtils;
import com.redcreator37.WhitelistBot.DataModels.Guild;
import com.redcreator37.WhitelistBot.DataModels.WhitelistedPlayer;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import static com.redcreator37.WhitelistBot.Localizations.lc;

/**
 * Lists all whitelisted players for this guild using multiple embeds
 */
public class ListWhitelisted extends BotCommand {

    public ListWhitelisted(String requiredRole) {
        super("list", "Lists whitelisted players on this server", null, requiredRole);
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
        if (!this.checkValidity(args, event).block()) return Mono.empty();
        MessageChannel channel = CommandUtils.getMessageChannel(event);
        Stack<WhitelistedPlayer> players = context.getWhitelisted().stream()
                .collect(Collectors.toCollection(Stack::new));
        for (int fieldsPerMessage = 0; fieldsPerMessage < 25; fieldsPerMessage++) {
            if (players.isEmpty()) break;
            List<String> fields = new ArrayList<>();
            int namesPerField = 10;
            // filter player IDs into groups of 10
            for (int i = 0; i < players.size(); i++) {
                StringBuilder b = new StringBuilder(1024);
                for (int j = 0; j < namesPerField && i < players.size(); j++) {
                    b.append(players.pop()).append("\n");
                    i++;
                }
                fields.add(b.toString().trim());
            }
            // add the fields with player IDs (10 per each field)
            // and put them into an embed on the guild
            int currentMessage = fieldsPerMessage;
            channel.createEmbed(spec -> {
                spec.setTitle(MessageFormat.format(lc("whitelisted-players-format"),
                        currentMessage + 1, players.size() / 25));
                spec.setColor(Color.YELLOW);
                for (int i = 0; i < fields.size(); i++)
                    for (int j = 0; j < 3 && i < fields.size(); j++) {
                        spec.addField("`[" + j + 1 + "-3]`", fields.get(j), j != 2);
                        i++;
                    }
                CommandUtils.setSelfAuthor(event.getGuild(), spec);
                spec.setTimestamp(Instant.now());
            }).block();
        }
        return Mono.empty();
    }
}
