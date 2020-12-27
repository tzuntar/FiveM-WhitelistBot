# FiveM Whitelist Bot

This is a Discord bot for easier management of whitelisted users on FiveM-based GTA 5 servers. It's still in early
stages of development so expect things to break on random occasions.

# How does it work?

This bot connects to configured FiveM ESX MySQL database instances and adds/removes steam-id:s of players to either
whitelist or block them. Server instances also need to have configured a script which checks and only allows whitelisted
players to connect.

The bot also caches certain data in its own (offline) SQLite database for easier access and management of data when the
target game servers are offline.

As of now, each guild can have up to one external database.

# What needs to be done?

This bot is still very buggy, and many edge-cases are yet to be implemented. If you encounter any bugs, you're welcome
to submit an issue or open a PR.

# How do I use this bot on my Discord server?

This bot is not hosted anywhere yet and probably won't ever be (unless there's huge interest). For now, you can compile
and run the code or download a `.jar` from the latest release and run it by yourself. For this to work, you'll also need
to get a [Discord bot token](https://discord.com/developers/docs/topics/oauth2#bots) and supply it when running the bot.
