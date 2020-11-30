package com.redcreator37.WhitelistBot;

import com.redcreator37.WhitelistBot.Database.FiveMDb;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Handler methods for bot commands
 */
public class CommandHandlers {

    /**
     * Checks if the entered command is invalid
     *
     * @param entered the message, separated by spaces
     * @param channel the channel in which the message was sent
     * @return <code>true</code> if the command is <strong>invalid</strong>,
     * otherwise <code>false</code>
     */
    private static boolean checkCmdInvalid(List<String> entered, MessageChannel channel) {
        assert channel != null;
        if (entered.size() < 2) {
            channel.createMessage("Which player?").block();
            return true;
        }
        return false;
    }

    /**
     * Returns the guild in which the message was created
     *
     * @param event the message create event that was caused
     * @return the guild or an empty Optional if the guild was not
     * found in the database
     */
    private static Optional<Guild> getGuildFromEvent(MessageCreateEvent event) {
        Snowflake snowflake = event.getGuildId().orElse(null);
        if (snowflake == null) return Optional.empty();
        Guild g = Guild.guildBySnowflake(snowflake, DiscordBot.guilds);
        return Optional.ofNullable(g);
    }

    private static boolean checkIdInvalid(String id) {
        return !Pattern.matches("^steam:[a-zA-Z0-9]+$", id);
    }

    @SuppressWarnings("BlockingMethodInNonBlockingContext")
    static Mono<Void> listWhitelisted(MessageCreateEvent event) {
        MessageChannel channel = event.getMessage().getChannel().block();
        assert channel != null;
        for (int i = 0; i < DiscordBot.whitelisted.size(); i++) {
            int perMessage = 20;
            StringBuilder msg = new StringBuilder(perMessage * 35);
            msg.append("**Registered players** `[").append((i + 2) / perMessage).append("/")
                    .append(DiscordBot.whitelisted.size()).append("]`:\n");
            for (int j = 0; j < perMessage && i < DiscordBot.whitelisted.size(); j++) {
                msg.append(DiscordBot.whitelisted.get(i).getIdentifier()).append("\n");
                i++;
            }
            channel.createMessage(msg.toString()).block();
        }
        return Mono.empty();
    }

    static void whitelistPlayer(List<String> cmd, MessageCreateEvent event) {
        MessageChannel channel = event.getMessage().getChannel().block();
        assert channel != null;
        if (checkCmdInvalid(cmd, channel)) return;
        channel.createEmbed(spec -> event.getGuildId()
                .flatMap(snowflake -> getGuildFromEvent(event))
                .ifPresent(guild -> {
                    if (checkIdInvalid(cmd.get(1))) {
                        spec.setTitle("Invalid ID");
                        spec.setColor(Color.ORANGE);
                        spec.addField("Entered ID", cmd.get(1), true);
                        spec.setTimestamp(Instant.now());
                        return;
                    }
                    Optional<String> fail = whitelistPlayerDb(cmd.get(1));
                    if (!fail.isPresent()) {
                        spec.setColor(Color.GREEN);
                        spec.setTitle("Player whitelisted");
                        spec.addField("Player ID", cmd.get(1), true);
                    } else {
                        spec.setColor(Color.RED);
                        spec.setTitle("Whitelisting failed");
                        spec.addField("Error", fail.get(), true);
                    }
                    spec.setTimestamp(Instant.now());
                })).block();
    }

    static void unlistPlayer(List<String> cmd, MessageCreateEvent event) {
        MessageChannel channel = event.getMessage().getChannel().block();
        assert channel != null;
        if (checkCmdInvalid(cmd, channel)) return;
        channel.createEmbed(spec -> event.getGuildId()
                .flatMap(snowflake -> getGuildFromEvent(event))
                .ifPresent(guild -> {
                    if (checkIdInvalid(cmd.get(1))) {
                        spec.setTitle("Invalid ID");
                        spec.setColor(Color.ORANGE);
                        spec.addField("Entered ID", cmd.get(1), true);
                        spec.setTimestamp(Instant.now());
                        return;
                    }
                    Optional<String> fail = unlistPlayerDb(cmd.get(1));
                    if (!fail.isPresent()) {
                        spec.setColor(Color.YELLOW);
                        spec.setTitle("Player unlisted");
                        spec.addField("Player ID", cmd.get(1), true);
                    } else {
                        spec.setColor(Color.RED);
                        spec.setTitle("Unlisting failed");
                        spec.addField("Error", fail.get(), true);
                    }
                    spec.setTimestamp(Instant.now());
                })).block();
    }

    private static Optional<String> unlistPlayerDb(String playerId) {
        try {
            FiveMDb.removePlayer(DiscordBot.fiveMDb, new FiveMDb.Player(playerId));
        } catch (SQLException e) {
            return Optional.of(e.getMessage());
        }
        return Optional.empty();
    }

    private static Optional<String> whitelistPlayerDb(String playerId) {
        try {
            FiveMDb.whitelistPlayer(DiscordBot.fiveMDb, new FiveMDb.Player(playerId));
        } catch (SQLException e) {
            return Optional.of(e.getMessage());
        }
        return Optional.empty();
    }

}
