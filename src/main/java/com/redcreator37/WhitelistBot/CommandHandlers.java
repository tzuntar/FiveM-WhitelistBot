package com.redcreator37.WhitelistBot;

import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;

import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Handler methods for common command-related routines
 */
public class CommandHandlers {

    /**
     * Checks whether the entered command is invalid
     *
     * @param entered the message, separated by spaces
     * @param channel the {@link MessageChannel} in which the message was sent
     * @return <code>true</code> if the command is <strong>invalid</strong>,
     * otherwise <code>false</code>
     */
    public static boolean checkCmdInvalid(List<String> entered, MessageChannel channel) {
        assert channel != null;
        if (entered.size() < 2) {
            channel.createMessage("Which player?").block();
            return true;
        }
        return false;
    }

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
        if (!permission) {
            getMessageChannel(event).createEmbed(spec -> {
                spec.setTitle("Permission denied");
                spec.setColor(Color.RED);
                spec.setAuthor(event.getMember().get().getUsername(), null, null);
                spec.addField("You do not have the permission\nto use this command",
                        "Required role: " + roleName, false);
                spec.setTimestamp(Instant.now());
            }).block();
        }
        return !permission;
    }

    /**
     * Sends the welcome message to the private channel of the owner of
     * the guild where the {@link GuildCreateEvent} has occurred
     *
     * @param event the {@link GuildCreateEvent} which has occurred when
     *              the bot has joined the guild
     */
    static void sendWelcomeMessage(GuildCreateEvent event) {
        event.getGuild().getOwner().flatMap(User::getPrivateChannel)
                .flatMap(channel -> channel.createEmbed(spec -> {
                    spec.setTitle("Hi there!");
                    spec.setColor(Color.LIGHT_SEA_GREEN);
                    spec.setDescription("You've received this message because you're"
                            + " the owner of the server " + event.getGuild().getName());
                    spec.addField("Finish the setup", "To finish the setup of "
                            + "the bot, please run the commands `" + DiscordBot.cmdPrefix + "setadmin` and `"
                            + DiscordBot.cmdPrefix + "connectdb`, respectively.", false);
                    spec.setTimestamp(Instant.now());
                })).block();
    }

}
