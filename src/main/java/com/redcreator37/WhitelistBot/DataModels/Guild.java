package com.redcreator37.WhitelistBot.DataModels;

import com.redcreator37.WhitelistBot.CommandHandlers;
import com.redcreator37.WhitelistBot.Database.GameHandling.FiveMDb;
import com.redcreator37.WhitelistBot.Database.GameHandling.SharedDbProvider;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

import static com.redcreator37.WhitelistBot.Localizations.lc;

/**
 * Represents exactly one Discord {@link discord4j.core.object.entity.Guild}
 * (called a "server" in the UI).
 * <p>
 * All data, required to connect to external databases successfully and
 * transiently handle errors is handled by this class.
 */
public class Guild {

    /**
     * This guild's database id
     */
    private final int id;

    /**
     * This guild's unique snowflake
     */
    private final Snowflake snowflake;

    /**
     * The date when the bot has joined this guild
     */
    private final Instant joined;

    /**
     * The role of the guild members required to retrieve / alter the
     * data for this guild
     */
    private String adminRole;

    /**
     * The connection information for the shared database
     */
    private SharedDbProvider sharedDbProvider;

    /**
     * The shared MySQL database with all game data
     */
    private FiveMDb fiveMDb;

    /**
     * A list of all whitelisted players in this guild
     */
    private List<WhitelistedPlayer> whitelisted;

    /**
     * Constructs a new Guild instance
     * <p>
     * This constructor is meant to be used <i>exclusively</i> when
     * registering new guilds, as it doesn't require additional data to
     * be present yet. When possible, use the full constructor
     * {@link Guild#Guild(int, Snowflake, Instant, String, SharedDbProvider)}.
     *
     * @param id        the guild's database id
     * @param snowflake the guild's snowflake
     * @param joined    the guild's join date in an ISO 8601-compliant
     *                  format
     */
    public Guild(int id, Snowflake snowflake, Instant joined) {
        this.id = id;
        this.snowflake = snowflake;
        this.joined = joined;
    }

    /**
     * Constructs a new Guild instance.
     * <p>
     * Shared database connection and related fields (ex. the list of
     * whitelisted players) are manually lazily-initialized using the
     * {@link Guild#connectSharedDb()} method.
     *
     * @param id        the guild's database id
     * @param snowflake the guild's snowflake
     * @param joined    the guild's join date
     * @param adminRole the role required to edit the data for this
     *                  guild
     * @param db        the database data provider
     */
    public Guild(int id, Snowflake snowflake, Instant joined, String adminRole, SharedDbProvider db) {
        this.id = id;
        this.snowflake = snowflake;
        this.joined = joined;
        this.adminRole = adminRole;
        this.sharedDbProvider = db;
    }

    /**
     * Connects to the shared game database, registered in this guild
     *
     * @throws SQLException on errors
     */
    public void connectSharedDb() throws SQLException {
        fiveMDb = new FiveMDb(sharedDbProvider.connect());
        whitelisted = fiveMDb.getWhitelistedPlayers();
    }

    /**
     * Lists all whitelisted players for this guild using multiple
     * embeds
     *
     * @param event the {@link MessageCreateEvent} which occurred when
     *              the calling message was sent
     * @return an empty {@link Mono} call
     */
    @SuppressWarnings("BlockingMethodInNonBlockingContext")
    public Mono<Void> listWhitelisted(MessageCreateEvent event) {
        if (CommandHandlers.checkNotAllowed(adminRole, event)) return Mono.empty();
        MessageChannel channel = CommandHandlers.getMessageChannel(event);
        Stack<WhitelistedPlayer> players = whitelisted.stream()
                .collect(Collectors.toCollection(Stack::new));
        for (int fieldsPerMessage = 0; fieldsPerMessage < 25; fieldsPerMessage++) {
            if (players.isEmpty()) break;
            List<String> fields = new ArrayList<>();
            int namesPerField = 10;
            // filter player IDs into groups of 10
            for (int i = 0; i < players.size(); i++) {
                StringBuilder b = new StringBuilder(1024);
                for (int j = 0; j < namesPerField && i < players.size(); j++) {
                    b.append(players.pop()).append("\n");
                    i++;
                }
                fields.add(b.toString().trim());
            }
            // add the fields with player IDs (10 per each field)
            // and put them into an embed on the guild
            int currentMessage = fieldsPerMessage;
            channel.createEmbed(spec -> {
                spec.setTitle(MessageFormat.format(lc("whitelisted-players-format"),
                        currentMessage + 1, players.size() / 25));
                spec.setColor(Color.YELLOW);
                for (int i = 0; i < fields.size(); i++)
                    for (int j = 0; j < 3 && i < fields.size(); j++) {
                        spec.addField("`[" + j + 1 + "-3]`", fields.get(j), j != 2);
                        i++;
                    }
                CommandHandlers.setSelfAuthor(Objects.requireNonNull(event
                        .getGuild().block()), spec);
                spec.setTimestamp(Instant.now());
            }).block();
        }
        return Mono.empty();
    }

    /**
     * Embeds data about the current admin role into the channel
     *
     * @param event the {@link MessageCreateEvent} which occurred when
     *              the calling message was sent
     * @return a {@link Mono} with the sent {@link Message}
     */
    public Mono<Message> embedAdminRoleData(MessageCreateEvent event) {
        if (CommandHandlers.checkNotAllowed(adminRole, event)) return Mono.empty();
        return CommandHandlers.getMessageChannel(event).createEmbed(spec -> {
            if (adminRole == null) {
                spec.setTitle("No admin role defined");
                spec.setColor(Color.RED);
                spec.addField("There's currently no admin role defined yet",
                        "Use the command `%setadmin` to set the admin role", false);
            } else {
                spec.setColor(Color.YELLOW);
                spec.addField("The current admin role is " + adminRole,
                        "To change it, use the `%setadmin` command", false);
            }
            CommandHandlers.setSelfAuthor(Objects.requireNonNull(event
                    .getGuild().block()), spec);
            spec.setTimestamp(Instant.now());
        });
    }

    /**
     * Whitelists the player in the game database whose name was
     * specified in the message
     *
     * @param cmd   the list of parameters of the sent message
     * @param event the {@link MessageCreateEvent} which occurred when
     *              the message was sent
     */
    public void whitelistPlayer(List<String> cmd, MessageCreateEvent event) {
        if (CommandHandlers.checkNotAllowed(adminRole, event)) return;
        MessageChannel channel = CommandHandlers.getMessageChannel(event);
        if (CommandHandlers.checkCmdInvalid(cmd, channel)) return;
        channel.createEmbed(spec -> event.getGuild().subscribe(guild -> {
            if (CommandHandlers.invalidPlayerIdEmbed(cmd.get(1), channel)) return;
            Optional<String> fail = whitelistPlayerDb(cmd.get(1));
            if (!fail.isPresent()) {
                spec.setColor(Color.GREEN);
                spec.setTitle(lc("player-whitelisted"));
                spec.addField(lc("player-id"), cmd.get(1), true);
            } else {
                spec.setColor(Color.RED);
                spec.setTitle(lc("whitelist-failed"));
                spec.addField(lc("error"), fail.get(), true);
            }
            spec.setTimestamp(Instant.now());
        })).block();
    }

    /**
     * Removes the player from the in-game whitelist.
     * The player's data is retrieved by parsing the message
     *
     * @param cmd   the list of parameters of the sent message
     * @param event the {@link MessageCreateEvent} which occurred when
     *              the message was sent
     */
    public void unlistPlayer(List<String> cmd, MessageCreateEvent event) {
        if (CommandHandlers.checkNotAllowed(adminRole, event)) return;
        MessageChannel channel = CommandHandlers.getMessageChannel(event);
        if (CommandHandlers.checkCmdInvalid(cmd, channel)) return;
        channel.createEmbed(spec -> event.getGuild().subscribe(guild -> {
            if (CommandHandlers.invalidPlayerIdEmbed(cmd.get(1), channel)) return;
            Optional<String> fail = unlistPlayerDb(cmd.get(1));
            if (!fail.isPresent()) {
                spec.setColor(Color.YELLOW);
                spec.setTitle(lc("player-unlisted"));
                spec.addField(lc("player-id"), cmd.get(1), true);
            } else {
                spec.setColor(Color.RED);
                spec.setTitle(lc("unlist-failed"));
                spec.addField(lc("error"), fail.get(), true);
            }
            spec.setTimestamp(Instant.now());
        })).block();
    }

    /**
     * Uses the connection to the in-game database to whitelist the
     * player with this SteamID
     *
     * @param playerId the SteamID of the player to whitelist
     * @return an empty {@link Optional} on success or the error
     * message
     */
    private Optional<String> whitelistPlayerDb(String playerId) {
        try {
            fiveMDb.whitelistPlayer(new WhitelistedPlayer(playerId));
            whitelisted.add(new WhitelistedPlayer(playerId));
        } catch (SQLException e) {
            return Optional.of(e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Uses the connection to the in-game database to remove the player
     * from its whitelist
     *
     * @param playerId the SteamID of the player to un-whitelist
     * @return an empty {@link Optional} on success or the error
     * message
     */
    private Optional<String> unlistPlayerDb(String playerId) {
        try {
            fiveMDb.removePlayer(new WhitelistedPlayer(playerId));
            whitelisted.remove(new WhitelistedPlayer(playerId));
        } catch (SQLException e) {
            return Optional.of(e.getMessage());
        }
        return Optional.empty();
    }

    public Snowflake getSnowflake() {
        return snowflake;
    }

    public Instant getJoined() {
        return joined;
    }

    public String getAdminRole() {
        return adminRole;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Guild)) return false;
        Guild guild = (Guild) o;
        return id == guild.id && snowflake.equals(guild.snowflake)
                && joined.equals(guild.joined) && Objects.equals(adminRole, guild.adminRole)
                && Objects.equals(sharedDbProvider, guild.sharedDbProvider)
                && Objects.equals(fiveMDb, guild.fiveMDb)
                && Objects.equals(whitelisted, guild.whitelisted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, snowflake, joined, adminRole, sharedDbProvider, fiveMDb, whitelisted);
    }

}
