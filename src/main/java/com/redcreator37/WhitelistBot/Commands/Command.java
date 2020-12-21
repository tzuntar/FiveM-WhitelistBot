package com.redcreator37.WhitelistBot.Commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

/**
 * Represents the bot's command
 */
public interface Command {

    /**
     * Executes the command triggered by this {@link MessageCreateEvent}
     *
     * @param event the preceding {@link MessageCreateEvent} which
     *              occurred when the message was sent
     */
    Mono<Void> execute(MessageCreateEvent event);

}
