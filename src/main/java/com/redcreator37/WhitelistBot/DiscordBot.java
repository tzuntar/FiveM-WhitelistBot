package com.redcreator37.WhitelistBot;

import com.redcreator37.WhitelistBot.DataModels.Guild;
import com.redcreator37.WhitelistBot.Database.BotHandling.GuildsDb;
import com.redcreator37.WhitelistBot.Database.BotHandling.LocalDb;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.GuildDeleteEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A Discord bot that aims to simplify server management for FiveM-based
 * game servers
 *
 * @author RedCreator37
 */
public class DiscordBot {

    /**
     * The prefix to look for when parsing messages into commands
     */
    static final char cmdPrefix = '%';

    /**
     * The currently used {@link GatewayDiscordClient} object when
     * connecting to Discord's servers
     */
    private static GatewayDiscordClient client = null;

    /**
     * The connection to the local SQLite database
     */
    private static Connection localDb = null;

    /**
     * A {@link HashMap} holding all currently implemented commands
     */
    private static final Map<String, Command> commands = new HashMap<>();

    /**
     * A {@link HashMap} of all registered guilds
     */
    static HashMap<Snowflake, Guild> guilds = new HashMap<>();

    /**
     * The currently used local database support object
     */
    private static GuildsDb guildsDb = null;

    /**
     * Initializes the bot commands
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static void setUpCommands() {
        commands.put("list", e -> Mono.just(guilds.get(e.getGuildId().get())
                .listWhitelisted(e)).then());
        commands.put("whitelist", e -> Mono.justOrEmpty(e.getMessage().getContent())
                .map(cnt -> Arrays.asList(cnt.split(" ")))
                .doOnNext(cmd -> guilds.get(e.getGuildId().get())
                        .whitelistPlayer(cmd, e)).then());
        commands.put("unlist", e -> Mono.justOrEmpty(e.getMessage().getContent())
                .map(cnt -> Arrays.asList(cnt.split(" ")))
                .doOnNext(cmd -> guilds.get(e.getGuildId().get())
                        .unlistPlayer(cmd, e)).then());
    }

    /**
     * Adds this {@link Guild} to the local database and sends its
     * owner the welcome message
     *
     * @param guild the {@link Guild} to add
     * @param event the {@link GuildCreateEvent} which occurred when the
     *              guild was registered
     * @return the status message
     */
    private static Mono<String> addGuild(Guild guild, GuildCreateEvent event) {
        try {
            guildsDb.addGuild(guild);
            guilds.put(guild.getSnowflake(), guild);
            CommandHandlers.sendWelcomeMessage(event);
            return Mono.just("Registered guild "
                    + guild.getSnowflake().asString() + " to the database");
        } catch (SQLException ex) {
            return Mono.just("Warning! Adding the guild failed: "
                    + ex.getMessage());
        }
    }

    /**
     * Removes this {@link Guild} from the local database
     *
     * @param guild the {@link Guild} to remove
     * @return the status message
     */
    private static Mono<String> removeGuild(Guild guild) {
        try {
            guildsDb.removeGuild(guild);
            guilds.remove(guild.getSnowflake());
            return Mono.just("Unregistered guild "
                    + guild.getSnowflake().asString() + " from the database");
        } catch (SQLException ex) {
            return Mono.just("Warning! Removing the guild failed: "
                    + ex.getMessage());
        }
    }

    /**
     * Initializes and hooks up the event handlers
     */
    private static void setUpEventDispatcher() {
        client.getEventDispatcher().on(MessageCreateEvent.class)
                .flatMap(e -> Mono.just(e.getMessage().getContent())
                        .flatMap(content -> Flux.fromIterable(commands.entrySet())
                                .filter(entry -> content.startsWith(cmdPrefix + entry.getKey()))
                                .flatMap(entry -> entry.getValue().execute(e)).next()))
                .subscribe();
        client.getEventDispatcher().on(GuildCreateEvent.class)
                .flatMap(e -> Mono.just(e.getGuild())
                        .flatMap(guild -> Mono.just(new Guild(0, guild.getId(), Instant.now())))
                        .flatMap(guild -> Mono.just(addGuild(guild, e))))
                .subscribe(System.out::println);
        client.getEventDispatcher().on(GuildDeleteEvent.class)
                .flatMap(e -> Mono.justOrEmpty(e.getGuild())
                        .flatMap(guild -> Mono.just(guilds.get(e.getGuildId())))
                        .flatMap(DiscordBot::removeGuild)).subscribe(System.out::println);
    }

    /**
     * Sets up the local database connection
     */
    private static void setUpDatabase() {
        boolean success = true, isNew = !new File("data.db").exists();
        try {
            localDb = LocalDb.connect("data.db");
            guildsDb = new GuildsDb(localDb);
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }

        if (isNew) // create a new database
            try {
                new LocalDb().createDatabaseTables(localDb);
                System.out.println("Created an empty database");
            } catch (SQLException | IOException e) {
                System.err.println("Error while creating the database:" + e.getMessage());
                success = false;
            }

        try {
            guilds = guildsDb.getGuilds();
            System.out.println("Database loaded successfully");
        } catch (SQLException e) {
            System.err.println("Error while reading from the database: " + e.getMessage());
            success = false;
        }

        if (!success) {
            System.err.println("FATAL: Unable to establish the database connection");
            System.exit(1);
        }
    }

    /**
     * Starts up the bot, loads the local database and connects to the
     * Discord's API
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Please provide the bot token!");
            System.exit(1);
        }

        setUpCommands();
        setUpDatabase();

        client = DiscordClientBuilder.create(args[0]).build().login().block();
        if (client == null) {
            System.err.println("Error: Login failed");
            System.exit(1);
        }
        setUpEventDispatcher();
        // close the database connection on shutdown
        client.onDisconnect().filter(unused -> {
            try {
                localDb.close();
            } catch (SQLException e) {
                System.err.println("Warning! Closing the database connection"
                        + " failed: " + e.getMessage());
            }
            return true;
        }).block();
    }

}
