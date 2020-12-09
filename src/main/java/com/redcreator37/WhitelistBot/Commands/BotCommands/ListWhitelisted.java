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
            // add the fields into embeds (10 per each) and submit them
            List<String> fields = splitInSize(players, 10);
            submitFields(fields, channel, fieldsPerMessage, players.size(), event).block();
        }
        return Mono.empty();
    }

    /**
     * Splits this {@link Stack} of {@link WhitelistedPlayer} objects
     * into smaller units for easier distribution
     *
     * @param players       the stack of players to split
     * @param namesPerField the number of names to add to each field
     * @return the {@link List} of fields
     */
    private List<String> splitInSize(Stack<WhitelistedPlayer> players, int namesPerField) {
        List<String> fields = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            StringBuilder b = new StringBuilder(1024);
            for (int j = 0; j < namesPerField && i < players.size(); j++) {
                b.append(players.pop()).append("\n");
                i++;
            }
            fields.add(b.toString().trim());
        }
        return fields;
    }

    /**
     * Submits these fields into an embed on the server
     *
     * @param fields     the list of fields to submit
     * @param channel    the {@link MessageChannel} onto which to submit them
     * @param currentMsg the number of the current message (used when
     *                   splitting into multiple embeds)
     * @param size       the number of all messages (used when
     *                   splitting into multiple embeds)
     * @param event      the {@link MessageCreateEvent} which occurred
     *                   when the original message was sent
     * @return an empty {@link Mono} object
     */
    private Mono<Void> submitFields(List<String> fields, MessageChannel channel, int currentMsg,
                                    int size, MessageCreateEvent event) {
        return channel.createEmbed(spec -> {
            spec.setTitle(MessageFormat.format(lc("whitelisted-players-format"),
                    currentMsg + 1, size / 25));
            spec.setColor(Color.YELLOW);
            for (int i = 0; i < fields.size(); i++)
                for (int j = 0; j < 3 && i < fields.size(); j++) {
                    spec.addField("`[" + j + 1 + "-3]`", fields.get(j), j != 2);
                    i++;
                }
            CommandUtils.setSelfAuthor(event.getGuild(), spec);
            spec.setTimestamp(Instant.now());
        }).then();
    }
}
