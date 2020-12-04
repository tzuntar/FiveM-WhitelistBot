create table guilds
(
    id         integer not null
        primary key autoincrement,
    snowflake  text    not null,
    joined     date    not null,
    admin_role text default 'admins' not null
);

create table caches
(
    guild_id     text not null
        references guilds (snowflake),
    last_refresh text not null
);

create unique index caches_guild_id_uindex
    on caches (guild_id);

create unique index guilds_snowflake_uindex
    on guilds (snowflake);
