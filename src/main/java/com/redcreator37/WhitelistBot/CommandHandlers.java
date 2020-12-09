package com.redcreator37.WhitelistBot;

import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

import static com.redcreator37.WhitelistBot.Localizations.lc;

/**
 * Handler methods for common command-related routines
 */
public class CommandHandlers {

    /**
     * Checks whether the entered command is missing the player
     * specified
     *
     * @param entered the message, separated by spaces
     * @param channel the {@link MessageChannel} in which the message was sent
     * @return <code>true</code> if the command is <strong>invalid</strong>,
     * otherwise <code>false</code>
     */
    public static boolean checkPlayerParamMissing(List<String> entered, MessageChannel channel) {
        assert channel != null;
        if (entered.size() < 2) {
            channel.createMessage(lc("which-player")).block();
            return true;
        }
        return false;
    }

    /**
     * Checks whether this SteamID is not in the correct format
     *
     * @param id the SteamID to check
     * @return <code>true</code> if the ID is invalid, <code>false</code>
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
     * @return the matching {@link Role} or <code>null</code> if the
     * member lacks it
     */
    private static Role findRole(Member member, String name) {
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
    public static void setSelfAuthor(Guild guild, EmbedCreateSpec spec) {
        guild.getClient().getSelf().flatMap(bot -> {
            spec.setAuthor(bot.getUsername(), null, bot.getAvatarUrl());
            return Mono.empty();
        }).block();
    }

    /**
     * Checks whether the member causing the {@link MessageCreateEvent}
     * doesn't have the permission to invoke the command.
     *
     * @param roleName the {@link Role} the member is required to have to
     *                 be allowed to invoke the command
     * @param event    the {@link MessageCreateEvent} which occurred when
     *                 the message was sent
     * @return <code>true</code> if the user <strong>doesn't have</strong>
     * the permission, <code>false</code> otherwise
     */
    public static boolean checkNotAllowed(String roleName, MessageCreateEvent event) {
        if (!event.getMember().isPresent()) return true;
        else if (roleName == null) return false;
        boolean permission = findRole(event.getMember().get(), roleName) != null;
        if (!permission) getMessageChannel(event).createEmbed(spec -> {
            spec.setTitle(lc("permission-denied"));
            spec.setColor(Color.RED);
            spec.setAuthor(event.getMember().get().getUsername(), null, null);
            spec.addField(lc("no-permission-to-use-command"),
                    MessageFormat.format(lc("required-role"), roleName), false);
            spec.setTimestamp(Instant.now());
        }).block();
        return !permission;
    }

    /**
     * Sends the welcome message to the private channel of the owner of
     * the guild where the {@link GuildCreateEvent} has occurred
     *
     * @param guild the {@link Guild} where the bot has joined
     */
    static void sendWelcomeMessage(Guild guild) {
        guild.getOwner().flatMap(User::getPrivateChannel)
                .flatMap(channel -> channel.createEmbed(spec -> {
                    spec.setTitle(lc("hi-there"));
                    spec.setColor(Color.LIGHT_SEA_GREEN);
                    spec.addField(lc("finish-setup"), MessageFormat
                            .format(lc("to-finish-setup-do"), DiscordBot.cmdPrefix), false);
                    spec.setFooter(MessageFormat.format(lc("received-message-owner"),
                            guild.getName()), null);
                    spec.setTimestamp(Instant.now());
                    setSelfAuthor(guild, spec);
                })).block();
    }

    /**
     * Checks if this player ID is invalid and embeds an error message
     * into the passed {@link MessageChannel} if it is
     *
     * @param id      the ID to check
     * @param channel the {@link MessageChannel} where the message about
     *                an invalid ID will be embedded
     * @return <code>true</code> if the ID is invalid, <code>false</code>
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

}
