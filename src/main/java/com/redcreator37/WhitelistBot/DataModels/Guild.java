package com.redcreator37.WhitelistBot.DataModels;

import com.redcreator37.WhitelistBot.CommandHandlers;
import com.redcreator37.WhitelistBot.Database.GameHandling.FiveMDb;
import com.redcreator37.WhitelistBot.Database.GameHandling.SharedDbProvider;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
     * messages
     *
     * @param event the {@link MessageCreateEvent} which occurred when
     *              the calling message was sent
     * @return An empty {@link Mono} call
     */
    @SuppressWarnings("BlockingMethodInNonBlockingContext")
    public Mono<Void> listWhitelisted(MessageCreateEvent event) {
        if (CommandHandlers.checkNotAllowed(adminRole, event)) return Mono.empty();
        MessageChannel channel = event.getMessage().getChannel().block();
        assert channel != null;
        for (int i = 0; i < whitelisted.size(); i++) {
            int perMessage = 20;
            int current = (i / perMessage) + 1;
            String header = MessageFormat.format("**Registered players** `[{0}-{1}]`",
                    current, current * perMessage);
            StringBuilder msg = new StringBuilder(perMessage * 35).append(header);
            for (int j = 0; j < perMessage && i < whitelisted.size(); j++) {
                msg.append(whitelisted.get(i).getIdentifier()).append("\n");
                i++;
            }
            channel.createMessage(msg.toString()).block();
        }
        return Mono.empty();
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
            if (CommandHandlers.checkIdInvalid(cmd.get(1))) {
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
            if (CommandHandlers.checkIdInvalid(cmd.get(1))) {
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
                && Objects.equals(sharedDbProvider, guild.sharedDbProvider) && Objects.equals(fiveMDb, guild.fiveMDb)
                && Objects.equals(whitelisted, guild.whitelisted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, snowflake, joined, adminRole, sharedDbProvider, fiveMDb, whitelisted);
    }

}
