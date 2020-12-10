package com.redcreator37.WhitelistBot;

import com.redcreator37.WhitelistBot.Commands.BotCommands.EmbedAdminData;
import com.redcreator37.WhitelistBot.Commands.BotCommands.ListWhitelisted;
import com.redcreator37.WhitelistBot.Commands.BotCommands.SetAdmin;
import com.redcreator37.WhitelistBot.Commands.BotCommands.UnlistPlayer;
import com.redcreator37.WhitelistBot.Commands.BotCommands.WhitelistPlayer;
import com.redcreator37.WhitelistBot.Commands.CommandUtils;
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
import java.text.MessageFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.redcreator37.WhitelistBot.Localizations.lc;

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
    public static final char cmdPrefix = '%';

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
        commands.put("list", e -> Mono.just(guilds.get(e.getGuildId().get()))
                .flatMap(guild -> new ListWhitelisted(guild.getAdminRole())
                        .execute(null, guild, e).then()));
        commands.put("whitelist", e -> Mono.justOrEmpty(e.getMessage().getContent())
                .map(cnt -> Arrays.asList(cnt.split(" ")))
                .doOnNext(cmd -> Mono.justOrEmpty(guilds.get(e.getGuildId().get()))
                        .flatMap(guild -> new WhitelistPlayer(guild.getAdminRole())
                                .execute(cmd, guild, e)).block()).then());
        commands.put("unlist", e -> Mono.justOrEmpty(e.getMessage().getContent())
                .map(cnt -> Arrays.asList(cnt.split(" ")))
                .doOnNext(cmd -> Mono.justOrEmpty(guilds.get(e.getGuildId().get()))
                        .flatMap(guild -> new UnlistPlayer(guild.getAdminRole())
                                .execute(cmd, guild, e)).block()).then());
        commands.put("setadmin", e -> Mono.justOrEmpty(e.getMessage().getContent())
                .map(cnt -> Arrays.asList(cnt.split(" ")))
                .doOnNext(cmd -> Mono.justOrEmpty(guilds.get(e.getGuildId().get()))
                        .flatMap(guild -> new SetAdmin(guild.getAdminRole())
                                .execute(cmd, guild, e)).block()).then());
        commands.put("getadmin", e -> Mono.just(guilds.get(e.getGuildId().get()))
                .flatMap(guild -> new EmbedAdminData(guild.getAdminRole())
                        .execute(null, guild, e).then()));
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
            CommandUtils.sendWelcome(event.getGuild());
            return Mono.just(MessageFormat.format(lc("registered-guild"),
                    guild.getSnowflake().asString()));
        } catch (SQLException ex) {
            return Mono.just(MessageFormat.format(lc("warn-guild-add-failed"),
                    ex.getMessage()));
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
            return Mono.just(MessageFormat.format(lc("unregistered-guild"),
                    guild.getSnowflake().asString()));
        } catch (SQLException ex) {
            return Mono.just(MessageFormat.format(lc("warn-guild-remove-failed"),
                    ex.getMessage()));
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
            System.err.println(MessageFormat.format(lc("error-format"), e.getMessage()));
        }

        if (isNew) // create a new database
            try {
                new LocalDb().createDatabaseTables(localDb, DiscordBot.class
                        .getClassLoader().getResourceAsStream("GenerateDb.sql"));
                System.out.println(lc("created-empty-db"));
            } catch (SQLException | IOException e) {
                System.err.println(MessageFormat.format(lc("error-creating-db"),
                        e.getMessage()));
                success = false;
            }

        try {
            guilds = guildsDb.getGuilds();
            System.out.println(lc("db-loaded-success"));
        } catch (SQLException e) {
            System.err.println(MessageFormat.format(lc("error-reading-db"),
                    e.getMessage()));
            success = false;
        }

        if (!success) {
            System.err.println(lc("fatal-db-connect-failed"));
            System.exit(1);
        }
    }

    /**
     * Starts up the bot, loads the local database and connects to the
     * Discord's API
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println(lc("provide-token"));
            System.exit(1);
        }

        setUpCommands();
        setUpDatabase();

        client = DiscordClientBuilder.create(args[0]).build().login().block();
        if (client == null) {
            System.err.println(lc("login-failed"));
            System.exit(1);
        }
        setUpEventDispatcher();
        // close the database connection on shutdown
        client.onDisconnect().filter(unused -> {
            try {
                localDb.close();
            } catch (SQLException e) {
                System.err.println(MessageFormat.format(lc("warn-db-close-failed"),
                        e.getMessage()));
            }
            return true;
        }).block();
    }

}
