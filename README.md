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
| `/pvp`            | Displays your PvP status and available actions.|
| `/pvp enable`     | Enable PvP consent for yourself.               |
| `/pvp disable`    | Disable PvP consent for yourself.              |

---

## Permissions

| Permission           | Description                                               |
|----------------------|----------------------------------------------------------|
| `consentpvp.use`     | Allows players to use the `/pvp` command.                |
| `consentpvp.admin`   | Allows admins to bypass cooldowns and combat restrictions.|

---

## Usage

- **Enable PvP:** Use `/pvp enable` to allow others to engage in PvP with you (if they have also enabled consent).
- **Disable PvP:** Use `/pvp disable` to prevent all PvP interactions with you.
- **Check Status:** Use `/pvp` to check your current PvP status and see if you can engage in combat with others.

---

## Support

If you encounter issues or have suggestions, please open an issue on our GitHub repository or join our Discord community for support.