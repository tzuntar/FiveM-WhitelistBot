create table guilds
(
    snowflake  text not null
        constraint guilds_pk
        primary key,
    joined     date not null,
    admin_role text default 'admins'
);
--
create table caches
(
    guild_id     text not null
        references guilds (snowflake),
    last_refresh text not null
);
--
create unique index caches_guild_id_uindex
    on caches (guild_id);
--
create table db_instances
(
    guild_id text            not null
        references guilds (snowflake),
    server   text            not null,
    username text default '' not null,
    password text default '' not null,
    database text            not null
);
--
create unique index db_instances_guild_id_uindex
    on db_instances (guild_id);
--
create unique index guilds_snowflake_uindex
    on guilds (snowflake);
--
