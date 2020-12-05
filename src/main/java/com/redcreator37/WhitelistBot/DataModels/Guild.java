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
 * Represents a Discord guild ("server")
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
     * The date when the bot has joined this guild in an ISO-8601
     * compliant format
     */
    private final Instant joined;

    /**
     * The role of the guild members required to alter the data for
     * this guild
     */
    private final String adminRole;

    /**
     * The connection information for the shared database
     */
    private final SharedDbProvider sharedDb;

    /**
     * The shared MySQL database with all game data
     */
    private FiveMDb fiveMDb;

    /**
     * A list of all whitelisted players in this guild
     */
    static List<WhitelistedPlayer> whitelisted;

    /**
     * Constructs a new Guild instance
     *
     * @param id        the guild's database id
     * @param snowflake the guild's snowflake
     * @param joined    the guild's join date in an ISO 8601-compliant
     *                  format
     * @param adminRole the role required to edit the data for this
     *                  guild
     * @param db        the database data provider
     */
    public Guild(int id, Snowflake snowflake, Instant joined, String adminRole, SharedDbProvider db) {
        this.id = id;
        this.snowflake = snowflake;
        this.joined = joined;
        this.adminRole = adminRole;
        this.sharedDb = db;
    }

    /**
     * Connects to the shared game database, registered in this guild
     *
     * @throws SQLException on errors
     */
    public void connectSharedDb() throws SQLException {
        fiveMDb = new FiveMDb(sharedDb.connect());
    }

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

    public void whitelistPlayer(List<String> cmd, MessageCreateEvent event) {
        if (CommandHandlers.checkNotAllowed(adminRole, event)) return;
        MessageChannel channel = event.getMessage().getChannel().block();
        assert channel != null;
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

    public void unlistPlayer(List<String> cmd, MessageCreateEvent event) {
        if (CommandHandlers.checkNotAllowed(adminRole, event)) return;
        MessageChannel channel = event.getMessage().getChannel().block();
        assert channel != null;
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

    private Optional<String> unlistPlayerDb(String playerId) {
        try {
            fiveMDb.removePlayer(new WhitelistedPlayer(playerId));
            whitelisted.remove(new WhitelistedPlayer(playerId));
        } catch (SQLException e) {
            return Optional.of(e.getMessage());
        }
        return Optional.empty();
    }

    private Optional<String> whitelistPlayerDb(String playerId) {
        try {
            fiveMDb.whitelistPlayer(new WhitelistedPlayer(playerId));
            whitelisted.add(new WhitelistedPlayer(playerId));
        } catch (SQLException e) {
            return Optional.of(e.getMessage());
        }
        return Optional.empty();
    }

    public int getId() {
        return id;
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
        return id == guild.id &&
                snowflake.equals(guild.snowflake) &&
                joined.equals(guild.joined) &&
                adminRole.equals(guild.adminRole) &&
                sharedDb.equals(guild.sharedDb);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, snowflake, joined, adminRole, sharedDb);
    }

}
