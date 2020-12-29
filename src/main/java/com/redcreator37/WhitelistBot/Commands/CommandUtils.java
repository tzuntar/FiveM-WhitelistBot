package com.redcreator37.WhitelistBot.Commands;

import com.redcreator37.WhitelistBot.DiscordBot;
import com.redcreator37.WhitelistBot.Localizations;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.regex.Pattern;

import static com.redcreator37.WhitelistBot.Localizations.lc;

/**
 * Handler methods for common command-related routines
 */
public class CommandUtils {

    /**
     * Checks whether this SteamID is not in the correct format
     *
     * @param id the SteamID to check
     * @return {@code true} if the ID is invalid, {@code false}
     * otherwise
     */
    public static boolean checkIdInvalid(String id) {
        return !Pattern.matches("^steam:[a-zA-Z0-9]+$", id);
    }

    /**
     * Returns the {@link Role} with the matching name for this {@link Member}
     *
     * @param member the {@link Member} which should have the role
     * @param name   the name of the role to look for
     * @return the matching {@link Role} or {@code null} if the
     * member lacks it
     */
    static Role findRole(Member member, String name) {
        return member.getRoles().filter(role -> role.getName().equals(name)).blockFirst();
    }

    /**
     * Returns the event channel in which the {@link MessageCreateEvent}
     * has occurred
     *
     * @param event the {@link MessageCreateEvent} which occurred when
     *              the message was sent
     * @return the matching {@link MessageChannel}
     */
    public static MessageChannel getMessageChannel(MessageCreateEvent event) {
        MessageChannel channel = event.getMessage().getChannel().block();
        assert channel != null;
        return channel;
    }

    /**
     * Sets the author data for this {@link EmbedCreateSpec} to the currently
     * running bot instance
     *
     * @param guild the {@link Guild} context to operate on
     * @param spec  the {@link EmbedCreateSpec} to set the data into
     */
    public static void setSelfAuthor(Mono<Guild> guild, EmbedCreateSpec spec) {
        guild.flatMap(g -> g.getClient().getSelf().flatMap(bot -> {
            spec.setAuthor(bot.getUsername(), null, bot.getAvatarUrl());
            return Mono.empty();
        })).block();
    }

    /**
     * Sends the welcome message to the private channel of the owner
     * of the guild
     *
     * @param guild the {@link Guild} context to get the data from
     */
    public static void sendWelcome(Guild guild) {
        guild.getOwner().flatMap(User::getPrivateChannel)
                .flatMap(channel -> channel.createEmbed(spec -> {
                    spec.setTitle(lc("hi-there"));
                    spec.setColor(Color.LIGHT_SEA_GREEN);
                    spec.addField(lc("finish-setup"), MessageFormat
                            .format(lc("to-finish-setup-do"), DiscordBot.cmdPrefix), false);
                    spec.setFooter(MessageFormat.format(lc("received-message-owner"),
                            guild.getName()), null);
                    spec.setTimestamp(Instant.now());
                    CommandUtils.setSelfAuthor(Mono.just(guild), spec);
                })).block();
    }

    /**
     * Checks if this player ID is invalid and embeds an error message
     * into the passed {@link MessageChannel} if it is
     *
     * @param id      the ID to check
     * @param channel the {@link MessageChannel} where the message about
     *                an invalid ID will be embedded
     * @return {@code true} if the ID is invalid, {@code false}
     * otherwise
     */
    public static boolean invalidPlayerIdEmbed(String id, MessageChannel channel) {
        if (!checkIdInvalid(id)) return false;
        channel.createEmbed(spec -> {
            spec.setTitle(lc("invalid-id"));
            spec.setColor(Color.ORANGE);
            spec.addField(lc("entered-id"), id, true);
            spec.setTimestamp(Instant.now());
        }).block();
        return true;
    }

    /**
     * Attempts to connect to the database, specified in this
     * {@link com.redcreator37.WhitelistBot.DataModels.Guild} and embed
     * the status into the {@link MessageChannel}
     *
     * @param channel the {@link MessageChannel} to embed the data into
     * @param guild   the {@link Guild} to embed the status into
     */
    public static void attemptConnectDb(MessageChannel channel, Guild guild) {
        try {
            DiscordBot.guilds.get(guild.getId()).connectSharedDb();
            channel.createEmbed(spec -> {
                spec.setTitle(Localizations.lc("connected-to-db"));
                spec.setColor(Color.GREEN);
                spec.addField(Localizations.lc("db-connect-established"),
                        Localizations.lc("you-can-now-perform-db-actions"), false);
                setSelfAuthor(Mono.just(guild), spec);
                spec.setTimestamp(Instant.now());
            }).block();
        } catch (SQLException e) {
            channel.createEmbed(spec -> {
                spec.setTitle(Localizations.lc("db-connect-failed"));
                spec.setColor(Color.RED);
                spec.addField(Localizations.lc("db-connect-could-not-be-established"),
                        e.getMessage(), false);
                spec.setDescription(Localizations.lc("check-connect-data"));
                setSelfAuthor(Mono.just(guild), spec);
                spec.setTimestamp(Instant.now());
            }).block();
        }
    }

    /**
     * Checks whether there's no database associated with this
     * {@link com.redcreator37.WhitelistBot.DataModels.Guild} and embeds
     * an error message in this case
     *
     * @param event the {@link MessageCreateEvent} which occurred when
     *              the message was sent
     * @param guild the {@link Guild} context to check
     * @return {@code true} if there's <strong>no</strong> database
     * data associated, {@code false} otherwise
     */
    public static boolean checkDbNotPresent(MessageCreateEvent event, com.redcreator37.WhitelistBot.DataModels.Guild guild) {
        if (guild.getSharedDbProvider() != null) return false;
        getMessageChannel(event).createEmbed(spec -> {
            spec.setTitle(Localizations.lc("no-db-yet"));
            spec.setColor(Color.RED);
            spec.addField(Localizations.lc("no-db-connected"), MessageFormat
                    .format(Localizations.lc("use-to-connect-db"), DiscordBot.cmdPrefix), false);
            setSelfAuthor(event.getGuild(), spec);
            spec.setTimestamp(Instant.now());
        }).block();
        return true;
    }

}
