package com.redcreator37.WhitelistBot;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.sql.SQLException;
import java.text.MessageFormat;
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

    private static boolean checkIdInvalid(String id) {
        return !Pattern.matches("^steam:[a-zA-Z0-9]+$", id);
    }

    private static Role findRole(Member member, String name) {
        return member.getRoles().filter(role -> role.getName().equals(name)).blockFirst();
    }

    @SuppressWarnings("BlockingMethodInNonBlockingContext")
    static Mono<Void> listWhitelisted(MessageCreateEvent event) {
        MessageChannel channel = event.getMessage().getChannel().block();
        assert channel != null;
        for (int i = 0; i < DiscordBot.whitelisted.size(); i++) {
            int perMessage = 20;
            String header = MessageFormat.format("**Registered players** `[{0}-{1}]`",
                    (i + 2) / perMessage, DiscordBot.whitelisted.size());
            StringBuilder msg = new StringBuilder(perMessage * 35).append(header);
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
        channel.createEmbed(spec -> event.getGuild().subscribe(guild -> {
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
        channel.createEmbed(spec -> event.getGuild().subscribe(guild -> {
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
            DiscordBot.fiveMDb.removePlayer(new WhitelistedPlayer(playerId));
            DiscordBot.whitelisted.remove(new WhitelistedPlayer(playerId));
        } catch (SQLException e) {
            return Optional.of(e.getMessage());
        }
        return Optional.empty();
    }

    private static Optional<String> whitelistPlayerDb(String playerId) {
        try {
            DiscordBot.fiveMDb.whitelistPlayer(new WhitelistedPlayer(playerId));
            DiscordBot.whitelisted.add(new WhitelistedPlayer(playerId));
        } catch (SQLException e) {
            return Optional.of(e.getMessage());
        }
        return Optional.empty();
    }

}
