# ConsentPVP

## Overview

**ConsentPVP** is a Minecraft plugin that enhances player versus player (PvP) interactions by requiring mutual consent before any combat can occur. The plugin empowers players to control their PvP status, send and accept PvP requests, and ensures fair gameplay by preventing unconsented PvP in all forms.

---

## Features

- **Mutual PvP Consent:** Players can enable or disable PvP for themselves using simple commands. PvP only occurs when both parties have consented.
- **Cooldowns:** Admins can configure cooldown periods between PvP toggles to prevent abuse.
- **Customizable Messages:** Supports MiniMessage formatting for rich, customizable in-game messages.
- **Comprehensive Protection:** Blocks all direct and indirect forms of PvP damage unless both players have consented.

---

## Installation

1. **Download the Plugin:** Obtain the latest ConsentPVP plugin JAR file via our [Releases Page](https://github.com/ModularSoftAU/ConsentPvP/releases).
2. **Install the Plugin:** Place the JAR file in your server's `plugins` directory.
3. **Restart the Server:** Restart your server to load ConsentPVP.

---

## Commands

| Command           | Description                                    |
|-------------------|------------------------------------------------|
| `/pvp`            | Displays your PvP status.                      |
| `/pvp status`     | Displays your PvP status.                      |
| `/pvp enable`     | Enable PvP consent for yourself.               |
| `/pvp disable`    | Disable PvP consent for yourself.              |
| `/pvp death`      | Toggles whether PVP is disabled on death.      |

---

## Permissions

| Permission           | Description                                               |
|----------------------|----------------------------------------------------------|
| `consentpvp.use`     | Allows players to use the `/pvp` command.                |
| `consentpvp.admin`   | Allows admins to bypass cooldowns and combat restrictions.|

---

## Configuration

The `config.yml` file allows you to customize the plugin's behavior.

```yaml
pvp:
  # If true, players will have their PVP consent disabled upon death.
  disable-on-death: false

cooldown:
  # Duration in minutes before a player can toggle PVP consent again
  duration: 5

messages:
  prefix: "<bold><gray>[<red>ConsentPVP<gray>]</bold> <white>"
  # Where PvP denial messages should appear. Options: chat, action_bar
  pvp_attempt_delivery: chat
  # If true, both players are notified when a PvP attempt is denied.
  notify-defender-on-denial: false
  pvp_enabled: "<green>PVP consent enabled."
  pvp_disabled: "<red>PVP consent disabled."
  on_cooldown: "<red>You must wait %time% before toggling PVP again."
  pvp_not_consented_attacker: "<red>You tried to hit %player% but PVP is not consented."
  pvp_not_consented_defender: "<red>%player% tried to hit you but PVP is not consented."
  pvp_not_consented_attacker_multiple: "<red>You tried to hit %players% but PVP is not consented."
  no_permission: "<red>You don't have permission to use this command."
  pvp_death_toggle: "<green>PVP disable on death is now %status%."
  pvp_disabled_on_death: "<red>Your PVP has been disabled due to your death."
  pvp_status: "<white>Your PVP status is currently <green>%status%<white>."
```

- `pvp_attempt_delivery` lets you move PvP denial notifications to the action bar instead of chat.
- `notify-defender-on-denial` controls whether the defender sees denial notifications.

## Usage

- **Enable PvP:** Use `/pvp enable` to allow others to engage in PvP with you (if they have also enabled consent).
- **Disable PvP:** Use `/pvp disable` to prevent all PvP interactions with you.
- **Check Status:** Use `/pvp` to check your current PvP status and see if you can engage in combat with others.

---

## Support

If you encounter issues or have suggestions, please open an issue on our GitHub repository or join our Discord community for support.