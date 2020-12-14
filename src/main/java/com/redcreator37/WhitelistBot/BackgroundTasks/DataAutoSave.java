package com.redcreator37.WhitelistBot.BackgroundTasks;

import com.redcreator37.WhitelistBot.DataModels.Guild;
import com.redcreator37.WhitelistBot.Database.BotHandling.DbInstances;
import com.redcreator37.WhitelistBot.Database.BotHandling.GuildsDb;
import com.redcreator37.WhitelistBot.Database.GameHandling.SharedDbProvider;
import com.redcreator37.WhitelistBot.DiscordBot;

import java.sql.SQLException;
import java.text.MessageFormat;

import static com.redcreator37.WhitelistBot.Localizations.lc;

/**
 * Saves the data to the internal database in the background
 */
public class DataAutoSave implements Runnable {

    private final DbInstances dbInstances;

    private final GuildsDb guildsDb;

    /**
     * Constructs a new DataAutoSave instance
     *
     * @param dbInstances the connection provider to the external
     *                    database instances database
     * @param guildsDb    the connection provider to the guilds
     *                    database
     */
    public DataAutoSave(DbInstances dbInstances, GuildsDb guildsDb) {
        this.dbInstances = dbInstances;
        this.guildsDb = guildsDb;
    }

    /**
     * Runs the auto-save process
     */
    @Override
    public void run() {
        System.out.println(lc("saving-data-do-not-stop"));
        DiscordBot.guilds.values().forEach(guild -> {
            try {
                updateProviderNullSafe(guild);
                if (guild.getAdminRole() != null)
                    guildsDb.updateAdminRole(guild);
            } catch (SQLException ex) {
                System.err.println(MessageFormat.format(lc("writing-guild-data-failed-reason"),
                        guild.getSnowflake().toString(), ex.getMessage()));
            }
        });
    }

    /**
     * Performs a null-safe update of the data about this guild's
     * {@link SharedDbProvider}. The data is either updated or
     * inserted, based on the current internal database state
     *
     * @param guild the {@link Guild} with the relevant provider object
     * @throws SQLException on errors
     */
    private void updateProviderNullSafe(Guild guild) throws SQLException {
        SharedDbProvider instance = guild.getSharedDbProvider();
        if (instance != null) { // use a different operation when registering a new instance
            SharedDbProvider current = dbInstances.getByGuild(guild.getSnowflake());
            if (current == null) dbInstances.registerInstance(guild.getSharedDbProvider());
            else dbInstances.updateInstance(guild.getSharedDbProvider());
        }
    }

}
