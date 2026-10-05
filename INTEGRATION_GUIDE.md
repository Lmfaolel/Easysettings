# 📖 EasySettings — Complete Integration Guide
### For Custom Plugins & Ready-Made Third-Party Plugins

Whether you are writing your own custom Minecraft plugin from scratch or integrating popular existing plugins (*EssentialsX*, *Vault*, *LuckPerms*, *WorldGuard*, *CombatLogX*, etc.), this guide covers everything you need.

---

## 📑 Table of Contents
1. [Overview](#1-overview)
2. [Part A: Integrating with Custom Plugins (Java Developers)](#part-a-custom-plugin-integration-java-api)
   - [Dependency Setup (Maven & Gradle)](#1-dependency-setup)
   - [Soft-Hooking in plugin.yml](#2-soft-hooking-in-pluginyml)
   - [Creating a Toggle Setting](#3-creating-a-toggle-setting)
   - [Creating an Action Button](#4-creating-an-action-button)
   - [Checking & Modifying Player States in Code](#5-checking--modifying-player-states-in-code)
   - [Registering Custom Action Handlers](#6-registering-custom-action-handlers)
   - [Listening to Bukkit Events](#7-listening-to-bukkit-events)
   - [Full Example Custom Plugin](#8-full-example-custom-plugin)
3. [Part B: Integrating with Ready-Made Plugins (Zero-Code / Server Admins)](#part-b-ready-made-plugins-integration-zero-code)
   - [How It Works](#1-how-it-works)
   - [EssentialsX Integration (Fly, God, Speed, Night Vision)](#2-essentialsx-integration)
   - [PvP & Combat Plugins (PvP Toggle)](#3-pvp--combat-plugins)
   - [Vault & Economy Plugins (Paid Perks)](#4-vault--economy-plugins)
   - [Scoreboard & Chat Plugins](#5-scoreboard--chat-plugins)
   - [PlaceholderAPI Integration](#6-placeholderapi-integration)
4. [Action Prefix Reference](#4-action-prefix-reference)
5. [Typography & Color Formatting (<sc> & Hex)](#5-typography--color-formatting)

---

# Part A: Custom Plugin Integration (Java API)

Use this method if you are developing your own plugin and want your settings to appear in the `/setting` menu dynamically.

### 1. Dependency Setup

#### Maven (`pom.xml`)
```xml
<dependencies>
    <!-- EasySettings API -->
    <dependency>
        <groupId>com.easysettings</groupId>
        <artifactId>EasySettings</artifactId>
        <version>1.0.0</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```
*If using a local file:*
```xml
<dependency>
    <groupId>com.easysettings</groupId>
    <artifactId>EasySettings</artifactId>
    <version>1.0.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/EasySettings-1.0.0.jar</systemPath>
</dependency>
```

#### Gradle (`build.gradle`)
```groovy
repositories {
    flatDir { dirs 'libs' }
}

dependencies {
    compileOnly name: 'EasySettings-1.0.0'
}
```

---

### 2. Soft-Hooking in `plugin.yml`

In your plugin's `plugin.yml`, add `EasySettings` to `softdepend`:
```yaml
name: MyCustomPlugin
version: 1.0.0
main: com.example.myplugin.MyCustomPlugin
api-version: '1.20'
softdepend: [EasySettings]
```

In your main class's `onEnable()`:
```java
if (Bukkit.getPluginManager().isPluginEnabled("EasySettings")) {
    getLogger().info("EasySettings found! Registering custom settings...");
    registerSettings();
}
```

---

### 3. Creating a Toggle Setting

A `TOGGLE` setting remembers per-player ON/OFF preferences and fires callbacks whenever flipped:

```java
import com.easysettings.api.EasySettingsAPI;
import com.easysettings.api.Setting;
import com.easysettings.api.SettingType;
import org.bukkit.Material;
import java.util.List;

public void registerSettings() {
    Setting autoPickup = Setting.builder("auto_pickup")
        .displayName("&#66FCF1&l<sc>Auto Pickup</sc>")
        .slot(11) // Inventory slot in /setting GUI
        .type(SettingType.TOGGLE)
        .item(Material.HOPPER)
        .defaultState(false) // Default: disabled
        .permission("myplugin.autopickup") // Optional permission
        .description(List.of(
            "&#868E96Automatically collect mined items directly",
            "&#868E96into your inventory.",
            "",
            "&#495057│ &#868E96Status: %status%",
            "",
            "&#66FCF1▸ Click to toggle"
        ))
        // Code executed when player toggles this:
        .onToggle((player, setting, newState) -> {
            player.sendMessage("Auto-pickup is now " + (newState ? "ON" : "OFF"));
        })
        .build();

    EasySettingsAPI.registerSetting(autoPickup);
}
```

---

### 4. Creating an Action Button

An `ACTION` setting executes immediate callbacks or commands when clicked:

```java
Setting trashCan = Setting.builder("trash_can")
    .displayName("&#FF7597&l<sc>Disposal Bin</sc>")
    .slot(15)
    .type(SettingType.ACTION)
    .item(Material.CAULDRON)
    .description(List.of(
        "&#868E96Open a quick item disposal menu.",
        "",
        "&#FF7597▸ Click to open"
    ))
    .onClick((player, setting, clickType) -> {
        player.openInventory(Bukkit.createInventory(null, 27, "Disposal"));
    })
    .build();

EasySettingsAPI.registerSetting(trashCan);
```

---

### 5. Checking & Modifying Player States in Code

You can read or update setting states anywhere across your plugin:

```java
UUID uuid = player.getUniqueId();

// 1. Check if setting is enabled for a player:
boolean isEnabled = EasySettingsAPI.isSettingEnabled(uuid, "auto_pickup");

// 2. Change a player's setting directly:
EasySettingsAPI.setSettingEnabled(uuid, "auto_pickup", true);

// 3. Programmatically toggle (triggers actions & event listeners):
EasySettingsAPI.toggleSetting(player, "auto_pickup");

// 4. Open the EasySettings GUI for a player:
EasySettingsAPI.openGUI(player);
```

---

### 6. Registering Custom Action Handlers

If you want server owners to write your plugin's actions in `settings.yml` or `/setting editor`:

```java
// Register custom tag: [myplugin] <argument>
EasySettingsAPI.registerActionHandler("myplugin", (player, argument) -> {
    if (argument.equalsIgnoreCase("reset")) {
        // Reset player stats...
        player.sendMessage("Stats reset!");
    }
});
```

Now in `settings.yml`:
```yaml
actions:
  - "[myplugin] reset"
  - "[sound] ENTITY_PLAYER_LEVELUP:1.0:1.0"
```

---

### 7. Listening to Bukkit Events

```java
import com.easysettings.api.event.PlayerSettingToggleEvent;
import com.easysettings.api.event.PlayerSettingClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SettingListener implements Listener {

    @EventHandler
    public void onSettingToggle(PlayerSettingToggleEvent event) {
        if (event.getSetting().getId().equalsIgnoreCase("auto_pickup")) {
            boolean newState = event.getNewState();
            Player player = event.getPlayer();

            // Cancel toggle if player doesn't meet requirements:
            if (newState && player.getLevel() < 5) {
                event.setCancelled(true);
                player.sendMessage("You need level 5 to enable Auto-Pickup!");
            }
        }
    }
}
```

---

### 8. Full Example Custom Plugin

```java
package com.example.autopickup;

import com.easysettings.api.EasySettingsAPI;
import com.easysettings.api.Setting;
import com.easysettings.api.SettingType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class AutoPickupPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        if (Bukkit.getPluginManager().isPluginEnabled("EasySettings")) {
            setupSettings();
        }
    }

    private void setupSettings() {
        Setting setting = Setting.builder("autopickup")
            .displayName("&#66FCF1&l<sc>Auto Pickup</sc>")
            .slot(13)
            .type(SettingType.TOGGLE)
            .item(Material.HOPPER)
            .defaultState(false)
            .description(List.of(
                "&#868E96Directly deposit drops into inventory.",
                "",
                "&#495057│ &#868E96Status: %status%",
                "",
                "&#66FCF1▸ Click to toggle"
            ))
            .build();

        EasySettingsAPI.registerSetting(setting);
    }

    @EventHandler
    public void onBlockDrop(BlockDropItemEvent event) {
        Player player = event.getPlayer();

        if (Bukkit.getPluginManager().isPluginEnabled("EasySettings")) {
            if (EasySettingsAPI.isSettingEnabled(player.getUniqueId(), "autopickup")) {
                for (Item item : event.getItems()) {
                    player.getInventory().addItem(item.getItemStack());
                }
                event.getItems().clear(); // Placed directly into inventory!
            }
        }
    }
}
```

---

# Part B: Ready-Made Plugins Integration (Zero-Code)

You do **not** need Java programming to integrate third-party plugins. You can configure any existing plugin into EasySettings via `/setting editor` in-game or by adding it to `settings.yml`.

### 1. How It Works
EasySettings executes commands on behalf of the player (`[player]`) or from console (`[console]`), meaning any plugin that has commands can be hooked instantly!

---

### 2. EssentialsX Integration

#### A. Flight Mode Toggle (`/fly`)
```yaml
settings:
  flight:
    name: "&#66FCF1&l<sc>Flight Mode</sc>"
    slot: 11
    type: "TOGGLE"
    default-state: false
    permission: "essentials.fly"
    item: "FEATHER"
    description:
      - "&#868E96Toggle your personal flight ability."
      - ""
      - "&#495057│ &#868E96Status: %status%"
      - ""
      - "&#66FCF1▸ Click to toggle"
    actions-on:
      - "[player] fly on"
      - "[sound] ENTITY_EXPERIENCE_ORB_PICKUP:1.0:1.0"
      - "[message] &#66FCF1&lғʟɪɢʜᴛ &#495057» &#51CF66ғʟɪɢʜᴛ ᴍᴏᴅᴇ ᴇɴᴀʙʟᴇᴅ!"
    actions-off:
      - "[player] fly off"
      - "[sound] BLOCK_NOTE_BLOCK_BASS:1.0:0.5"
      - "[message] &#66FCF1&lғʟɪɢʜᴛ &#495057» &#FF6B6Bғʟɪɢʜᴛ ᴍᴏᴅᴇ ᴅɪsᴀʙʟᴇᴅ!"
```

#### B. Teleportation Toggle (`/tptoggle`)
```yaml
  tptoggle:
    name: "&#FFA94D&l<sc>Teleport Requests</sc>"
    slot: 12
    type: "TOGGLE"
    permission: "essentials.tptoggle"
    item: "ENDER_EYE"
    description:
      - "&#868E96Accept or deny incoming teleport requests."
      - ""
      - "&#495057│ &#868E96Status: %status%"
      - ""
      - "&#FFA94D▸ Click to toggle"
    actions-on:
      - "[player] tptoggle"
      - "[message] &#FFA94D&lᴛᴘ &#495057» &#51CF66ᴛᴇʟᴇᴘᴏʀᴛ ʀᴇǫᴜᴇsᴛs ᴇɴᴀʙʟᴇᴅ!"
    actions-off:
      - "[player] tptoggle"
      - "[message] &#FFA94D&lᴛᴘ &#495057» &#FF6B6Bᴛᴇʟᴇᴘᴏʀᴛ ʀᴇǫᴜᴇsᴛs ᴅɪsᴀʙʟᴇᴅ!"
```

---

### 3. PvP & Combat Plugins

#### Global / Player PvP Toggle
```yaml
  pvp_mode:
    name: "&#FF6B6B&l<sc>PvP Mode</sc>"
    slot: 13
    type: "TOGGLE"
    item: "DIAMOND_SWORD"
    description:
      - "&#868E96Toggle PvP vulnerability on or off."
      - ""
      - "&#495057│ &#868E96Status: %status%"
      - ""
      - "&#FF6B6B▸ Click to toggle"
    actions-on:
      - "[console] pvp %player% on"
      - "[sound] ENTITY_WITHER_SPAWN:0.5:1.5"
      - "[message] &#FF6B6B&lᴘᴠᴘ &#495057» &#FF6B6Bʏᴏᴜ ᴀʀᴇ ɴᴏᴡ ᴠᴜʟɴᴇʀᴀʙʟᴇ ᴛᴏ ᴘᴠᴘ!"
    actions-off:
      - "[console] pvp %player% off"
      - "[sound] BLOCK_ANVIL_USE:0.8:1.0"
      - "[message] &#FF6B6B&lᴘᴠᴘ &#495057» &#51CF66ᴘᴠᴘ ᴘʀᴏᴛᴇᴄᴛɪᴏɴ ᴇɴᴀʙʟᴇᴅ!"
```

---

### 4. Vault & Economy Plugins

Execute console commands to deduct balance and give perks:
```yaml
  buy_fly_speed:
    name: "&#FFD43B&l<sc>Speed Boost</sc> &#868E96($500)"
    slot: 14
    type: "ACTION"
    item: "SUGAR"
    description:
      - "&#868E96Purchase a 10-minute speed boost for $500."
      - ""
      - "&#FFD43B▸ Click to purchase"
    actions:
      - "[console] eco take %player% 500"
      - "[console] effect give %player% speed 600 1"
      - "[sound] ENTITY_PLAYER_LEVELUP:1.0:1.2"
      - "[message] &#51CF66✔ Purchased Speed Boost for $500!"
```

---

### 5. Scoreboard & Chat Plugins

```yaml
  scoreboard_toggle:
    name: "&#70E1F5&l<sc>Scoreboard</sc>"
    slot: 15
    type: "TOGGLE"
    item: "MAP"
    description:
      - "&#868E96Show or hide your sidebar scoreboard."
      - ""
      - "&#495057│ &#868E96Status: %status%"
      - ""
      - "&#70E1F5▸ Click to toggle"
    actions-on:
      - "[player] sb on"
    actions-off:
      - "[player] sb off"
```

---

### 6. PlaceholderAPI Integration

EasySettings natively supports **PlaceholderAPI** in:
- GUI Title
- Display Names
- Lore / Description lines
- Console commands & messages

#### Example Lore using PAPI placeholders:
```yaml
description:
  - "&#868E96Player: &#CED4DA%player_name%"
  - "&#868E96Balance: &#FFD43B$%vault_eco_balance_formatted%"
  - "&#868E96Rank: &#66FCF1%luckperms_primary_group_name%"
  - ""
  - "&#495057│ &#868E96Status: %status%"
```

---

## ⚡ Action Prefix Reference

When writing actions in `settings.yml` or `/setting editor`:

| Prefix | Usage | Description | Example |
| :--- | :--- | :--- | :--- |
| `[player]` | `[player] <cmd>` | Runs command as the player | `[player] fly` |
| `[console]` | `[console] <cmd>` | Runs command from server console | `[console] give %player% diamond 1` |
| `[message]` | `[message] <text>` | Sends formatted chat message | `[message] &#51CF66Enabled!` |
| `[broadcast]`| `[broadcast] <text>`| Broadcasts message to all players | `[broadcast] &#FF6B6B%player% entered PvP!` |
| `[sound]` | `[sound] <SOUND:VOL:PITCH>` | Plays a sound at player's location | `[sound] ENTITY_PLAYER_LEVELUP:1.0:1.5` |
| `[close]` | `[close]` | Closes the open GUI | `[close]` |
| `[refresh]` | `[refresh]` | Re-renders the GUI for player | `[refresh]` |
| `[api]` | `[api] <tag:args>` | Triggers custom Java handler | `[api] economy:take 100` |

---

## 🎨 Typography & Color Formatting

EasySettings supports modern Unicode small caps and RGB hex codes anywhere in text:

1. **Small Caps Tag**: `<sc>text</sc>` or `<smallcaps>text</smallcaps>`
   - `<sc>Player Settings</sc>` &rarr; `ᴘʟᴀʏᴇʀ sᴇᴛᴛɪɴɢs`
2. **Hex RGB Colors**:
   - `&#66FCF1`
   - `<#66FCF1>`
   - `{#66FCF1}`
3. **Legacy Codes**:
   - `&a`, `&b`, `&c`, `&l`, `&o`, `&r`, etc.
