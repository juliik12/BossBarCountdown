# BossBarCountdown

A customizable BossBar countdown plugin for Spigot/Paper Minecraft servers.

---

## Features

- Create multiple boss bars with configurable titles, colors, styles, and visibility.
- Countdown timers with dynamic `%time%` placeholder.
- Permission-based boss bars to show only to players with specific permissions.
- Custom actions executed when countdown finishes: messages, commands, broadcasts, sounds, titles.
- PlaceholderAPI support for dynamic text.
- Easy commands to start, stop, reload, test, verify config, and create boss bars.
- Multi-language support (English and Spanish included).
- Lightweight and easy to configure.

---

## Installation

1. Download the latest release JAR from [Releases](https://github.com/tuUsuario/BossBarCountdown/releases).
2. Place the JAR file in your server's `plugins` folder.
3. Start or reload your server to generate the default config files.
4. Edit `config.yml` and language files in `lang/` as needed.
5. Use `/bossbar help` for command usage.

---

## Commands

| Command                   | Permission                 | Description                          |
|---------------------------|----------------------------|------------------------------------|
| `/bossbar` or `/bossbar help` | `bossbar.command.help`       | Shows help message                  |
| `/bossbar start <type> <seconds>` | `bossbar.command.start`      | Starts countdown boss bar           |
| `/bossbar stop`           | `bossbar.command.stop`       | Stops current countdown             |
| `/bossbar reload`         | `bossbar.command.reload`     | Reloads config and language files  |
| `/bossbar test <type>`    | `bossbar.command.test`       | Executes finish actions immediately |
| `/bossbar verify`         | `bossbar.command.verify`     | Checks for config errors            |
| `/bossbar create <type> <color> <style>` | `bossbar.command.create`     | Creates new boss bar config         |

---

## Configuration

The plugin configuration is stored in `config.yml`.

Example boss bar config:

```yaml
bossbars:
  example:
    title: "&aCountdown: %time%"
    color: GREEN
    style: SEGMENTED_10
    type: global  # or permission
    on_finish:
      repeat: 1
      delay_between: 0
      actions:
        msg_all:
          type: broadcast
          values:
            - "&eCountdown finished!"
        cmd_run:
          type: command
          values:
            - "say Time's up!"
````

---

## Permissions

* `bossbar.command.help`
* `bossbar.command.start`
* `bossbar.command.stop`
* `bossbar.command.reload`
* `bossbar.command.test`
* `bossbar.command.verify`
* `bossbar.command.create`
* `bossbar.view.<type>` (for boss bars with `type: permission`)

---

## Localization

Supports multiple languages through YAML files in `lang/`.

Change language in `config.yml` with:

```yaml
language: en
```

---

## Dependencies

* [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) (optional, for placeholders support)


## Contact

Developed by Julirexs

---
