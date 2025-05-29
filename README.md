# ConsentPVP

ConsentPVP is a Minecraft plugin designed to manage PVP consent and combat. It allows players to enable or disable PVP, send and accept PVP requests, and includes features to prevent abuse and ensure fair gameplay.

## Features

- **PVP Consent**: Players can enable or disable PVP using `/pvp enable` and `/pvp disable`.
- **PVP Requests**: Players can send and accept PVP requests using `/pvp request <player>` and `/pvp accept`.
- **Cooldown**: Admins can set a cooldown between PVP toggles to prevent abuse.
- **Combat Restrictions**: Players cannot disable PVP while in combat.
- **Permissions**: Configurable permissions for players and admins.

## Installation

1. Download the ConsentPVP plugin jar file.
2. Place the jar file in the `plugins` directory of your Minecraft server.
3. Restart the server to load the plugin.

## Configuration

The plugin can be configured by editing the `config.yml` file in the `plugins/ConsentPVP` directory. You can set the cooldown duration and other settings as needed.

## Commands

- `/pvp enable`: Enable PVP consent.
- `/pvp disable`: Disable PVP consent.
- `/pvp request <player>`: Send a PVP request to another player.
- `/pvp accept`: Accept a pending PVP request.

## Permissions

- `consentpvp.use`: Allows players to use the `/pvp` command.
- `consentpvp.admin`: Allows admins to bypass cooldowns and combat restrictions.

## Support

If you encounter any issues or have suggestions for improvements, please open an issue on the plugin's GitHub repository.