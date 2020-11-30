package com.redcreator37.WhitelistBot;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

/**
 * A simple discord bot command
 */
public interface Command {

    /**
     * Executes the command triggered by this event
     *
     * @param event the preceding event
     */
    Mono<Void> execute(MessageCreateEvent event);

}
