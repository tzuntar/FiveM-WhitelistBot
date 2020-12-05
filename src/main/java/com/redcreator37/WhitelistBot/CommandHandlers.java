package com.redcreator37.WhitelistBot;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;

import java.time.Instant;
import java.util.List;
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

    private static Role findRole(Member member, String name) {
        return member.getRoles().filter(role -> role.getName().equals(name)).blockFirst();
    }

    public static MessageChannel getMessageChannel(MessageCreateEvent event) {
        MessageChannel channel = event.getMessage().getChannel().block();
        assert channel != null;
        return channel;
    }

    public static boolean checkNotAllowed(String roleName, MessageCreateEvent event) {
        if (!event.getMember().isPresent()) return true;
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

}
