package com.redcreator37.WhitelistBot.Commands.BotCommands;

import com.redcreator37.WhitelistBot.Commands.BotCommand;
import com.redcreator37.WhitelistBot.Commands.CommandUtils;
import com.redcreator37.WhitelistBot.DataModels.Guild;
import com.redcreator37.WhitelistBot.Localizations;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.redcreator37.WhitelistBot.Localizations.lc;

/**
 * Removes the player from the in-game whitelist.
 * The player's data is retrieved by parsing the message
 */
public class UnlistPlayer extends BotCommand {

    public UnlistPlayer() {
        super("unlist", Localizations.lc("unlists-player"),
                new HashMap<String, Boolean>() {{
                    put("playerName", true);
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
    @Override
    public Mono<Void> execute(List<String> args, Guild context, MessageCreateEvent event) {
        if (!this.checkValidity(args, event, context).block()) return Mono.empty();
        MessageChannel channel = CommandUtils.getMessageChannel(event);
        return channel.createEmbed(spec -> event.getGuild().subscribe(guild -> {
            if (CommandUtils.invalidPlayerIdEmbed(args.get(1), channel)) return;
            Optional<String> fail = context.unlistPlayer(args.get(1));
            if (!fail.isPresent()) {
                spec.setColor(Color.YELLOW);
                spec.setTitle(lc("player-unlisted"));
                spec.addField(lc("player-id"), args.get(1), true);
            } else {
                spec.setColor(Color.RED);
                spec.setTitle(lc("unlist-failed"));
                spec.addField(lc("error"), fail.get(), true);
            }
            spec.setTimestamp(Instant.now());
        })).then();
    }
}
