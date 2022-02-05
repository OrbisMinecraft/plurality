# Plurality

**Plurality is currently alpha software. Use with caution.**

Plurality is a [Velocity](https://velocitypowered.com/) plugin for placing players back into the same server 
they previously left. It works by preventing Velocity from trying to put players into one of the servers defined
in the `try` list of `velocity.toml` if they have joined before. If a player has joined before, Plurality will
select the server the player was in before as the initial join server, if it can. Otherwise it will fall back to
the `try` list defined in `velocity.toml`.

## Setup
You can run Plurality in `file` mode which will cause it to create a simple YAML file containing all user's 
logoff-servers. In that case, no further setup is required. You can, however, run Plurality in `mysql` mode 
which stores logoff-servers in a [MySQL](https://www.mysql.com/) compatible database 
(like [MariaDB](https://mariadb.org/)). In this case, you will have to create a user and a database for Plurality.

Plurality only needs one single table (which is called `plurality` by default) so you can easily bundle it alongside
other data in the same database (ie. with [Tenacity](https://github.com/OrbisMinecraft/tenacity)). You can create a
new user and database like this:

```mysql
CREATE USER 'plurality'@'localhost' IDENTIFIED BY 'ASecurePassword';
CREATE DATABASE plurality;
GRANT ALL PRIVILEGES ON plurality.* TO 'plurality'@'localhost';
```

The name of the user and database are at your discretion.

After starting your server once with Plurality installed, a configuration file will be created in the
`plugins/plurality/` folder. You will have to modify it to match the database you just set up. For the
example above, a configuration like this would be valid:

```yaml
storage:
    method: mysql
    url: 'jdbc:mariadb://localhost/plurality?user=plurality&password=ASecurePassword'
    table: 'plurality'
```

## Building
Plurality is a Gradle project. To build it, you will need an up-to-date build of JDK 17 installed
on your machine. To get started, download the source code (either by downloading the ZIP file or
`git clone`-ing it). Then open the folder with the source code in a terminal or command prompt
and run `./gradlew shadowJar`. You will find the plugin's JAR file in `./build/libs`.

## Other Projects
Also check out our other projects:
- [Ferocity](https://github.com/OrbisMinecraft/ferocity), a Velocity plugin for sharing the tab list across multiple servers
- [Tenacity](https://github.com/OrbisMinecraft/tenacity), a Paper plugin which saves player's inventories in a database so they can be shared across servers
- [Ionicity](https://github.com/OrbisMinecraft/ionicity), a Velocity plugin for sharing chat across multiple servers
