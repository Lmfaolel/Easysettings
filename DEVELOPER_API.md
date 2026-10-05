# 🔌 EasySettings — Developer API Guide

Welcome to the **EasySettings Developer Guide**! This guide covers everything external plugin developers need to know to hook into EasySettings, add custom settings, listen to events, and control player preferences.

---

## 📑 Table of Contents
1. [Adding Dependency (Maven & Gradle)](#1-adding-dependency)
2. [Soft-Hooking in `plugin.yml`](#2-soft-hooking-in-pluginyml)
3. [Method 1: Registering Settings in Code (Fastest & Recommended)](#3-method-1-registering-settings-in-code)
4. [Method 2: Registering Custom Action Handlers (YAML/Action-Based)](#4-method-2-registering-custom-action-handlers)
5. [Method 3: Listening to Bukkit Events](#5-method-3-listening-to-bukkit-events)
6. [Checking & Modifying Player States](#6-checking--modifying-player-states)
7. [Full Working Plugin Example](#7-full-working-plugin-example)

---

## 1. Adding Dependency

### Maven (`pom.xml`)
If EasySettings is in your local repository or a local jar folder (`libs/`):
```xml
<dependency>
    <groupId>com.easysettings</groupId>
    <artifactId>EasySettings</artifactId>
    <version>1.0.0</version>
    <scope>provided</scope>
</dependency>
```

Or reference the JAR directly in `libs/`:
```xml
<dependency>
    <groupId>com.easysettings</groupId>
    <artifactId>EasySettings</artifactId>
    <version>1.0.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/EasySettings-1.0.0.jar</systemPath>
</dependency>
```

### Gradle (`build.gradle`)
```groovy
repositories {
    flatDir {
        dirs 'libs'
    }
}

dependencies {
    compileOnly name: 'EasySettings-1.0.0'
}
```

---

## 2. Soft-Hooking in `plugin.yml`

Add `EasySettings` to your `plugin.yml` under `softdepend`:
```yaml
name: MyAwesomePlugin
version: 1.0.0
main: com.example.myplugin.MyPlugin
api-version: '1.20'
softdepend: [EasySettings]
```

In your plugin's `onEnable()`:
```java
if (Bukkit.getPluginManager().isPluginEnabled("EasySettings")) {
    getLogger().info("EasySettings found! Registering settings...");
    setupSettings();
}
```

---

## 3. Method 1: Registering Settings in Code

You can create and inject settings directly into EasySettings without touching any YAML files.

### A. Creating a `TOGGLE` Setting (On/Off)
```java
import com.easysettings.api.EasySettingsAPI;
import com.easysettings.api.Setting;
import com.easysettings.api.SettingType;
import org.bukkit.Material;

public void setupSettings() {
    Setting autoPickup = Setting.builder("auto_pickup")
        .displayName("&#66FCF1&lᴀᴜᴛᴏ ᴘɪᴄᴋᴜᴘ")
        .slot(12) // Desired slot in /setting GUI
        .type(SettingType.TOGGLE)
        .item(Material.HOPPER)
        .defaultState(false) // Default off
        .permission("myplugin.autopickup") // Optional permission
        .description(List.of(
            "&#868E96Automatically suck mined drops",
            "&#868E96directly into your inventory.",
            "",
            "&#495057│ &#868E96Status: %status%",
            "",
            "&#66FCF1▸ Click to toggle"
        ))
        // Java callback whenever player flips the toggle:
        .onToggle((player, setting, newState) -> {
            player.sendMessage("Auto Pickup is now " + (newState ? "ENABLED!" : "DISABLED!"));
            // Trigger your plugin's internal logic here!
        })
        .build();

    // Register into EasySettings:
    EasySettingsAPI.registerSetting(autoPickup);
}
```

### B. Creating an `ACTION` Setting (Click Button)
```java
Setting trashCan = Setting.builder("trash_can")
    .displayName("&#FF7597&lᴏᴘᴇɴ ᴅɪsᴘᴏsᴀʟ")
    .slot(14)
    .type(SettingType.ACTION)
    .item(Material.CAULDRON)
    .description(List.of(
        "&#868E96Instantly open a disposal bin.",
        "",
        "&#FF7597▸ Click to open"
    ))
    .onClick((player, setting, clickType) -> {
        // Open your plugin's disposal menu
        player.openInventory(Bukkit.createInventory(null, 27, "Disposal"));
    })
    .build();

EasySettingsAPI.registerSetting(trashCan);
```

---

## 4. Method 2: Registering Custom Action Handlers

If server admins want to write actions in `settings.yml` or the `/setting editor` GUI (like `[myplugin] doSomething`), you can register a custom action tag:

```java
// Register custom tag: [myplugin] <argument>
EasySettingsAPI.registerActionHandler("myplugin", (player, argument) -> {
    // Example: argument = "heal" or "give 500"
    if (argument.equalsIgnoreCase("heal")) {
        player.setHealth(20.0);
        player.sendMessage("You were healed by EasySettings!");
    }
});
```

Now in `settings.yml` (or via `/setting editor`), admins can simply add:
```yaml
actions:
  - "[myplugin] heal"
  - "[sound] ENTITY_PLAYER_LEVELUP:1.0:1.0"
```

---

## 5. Method 3: Listening to Bukkit Events

EasySettings fires standard Bukkit events that your plugin can listen to using standard `@EventHandler` methods:

```java
import com.easysettings.api.event.PlayerSettingToggleEvent;
import com.easysettings.api.event.PlayerSettingClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SettingsListener implements Listener {

    @EventHandler
    public void onSettingToggle(PlayerSettingToggleEvent event) {
        if (event.getSetting().getId().equalsIgnoreCase("auto_pickup")) {
            boolean isEnabled = event.getNewState();
            Player player = event.getPlayer();
            
            // You can cancel the toggle if the player doesn't meet requirements:
            if (!player.hasPermission("myplugin.vip") && isEnabled) {
                event.setCancelled(true);
                player.sendMessage("Only VIPs can enable this!");
            }
        }
    }

    @EventHandler
    public void onSettingClick(PlayerSettingClickEvent event) {
        // Called when ANY setting is clicked
    }
}
```

---

## 6. Checking & Modifying Player States

### Check if a Setting is Enabled for a Player
```java
UUID uuid = player.getUniqueId();
boolean isAutoPickupOn = EasySettingsAPI.isSettingEnabled(uuid, "auto_pickup");

if (isAutoPickupOn) {
    // Suck drop into inventory
}
```

### Force Update a Player's Setting State
```java
// Change state directly in persistent database:
EasySettingsAPI.setSettingEnabled(uuid, "auto_pickup", true);

// Or programmatically toggle (which runs actions & fires events):
EasySettingsAPI.toggleSetting(player, "auto_pickup");
```

### Open the GUI for a Player via Code
```java
EasySettingsAPI.openGUI(player);
```

---

## 7. Full Working Plugin Example

Here is a complete, minimal Spigot plugin implementing an `auto_pickup` setting:

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

        // Hook into EasySettings if present
        if (Bukkit.getPluginManager().isPluginEnabled("EasySettings")) {
            registerMySettings();
        }
    }

    private void registerMySettings() {
        Setting setting = Setting.builder("autopickup")
            .displayName("&#66FCF1&lᴀᴜᴛᴏ ᴘɪᴄᴋᴜᴘ")
            .slot(13)
            .type(SettingType.TOGGLE)
            .item(Material.HOPPER)
            .defaultState(false)
            .description(List.of(
                "&#868E96Directly deposit mined blocks into inventory.",
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

        // Check if player enabled the setting in EasySettings:
        if (Bukkit.getPluginManager().isPluginEnabled("EasySettings")) {
            if (EasySettingsAPI.isSettingEnabled(player.getUniqueId(), "autopickup")) {
                for (Item item : event.getItems()) {
                    player.getInventory().addItem(item.getItemStack());
                }
                event.getItems().clear(); // Prevent drops on ground
            }
        }
    }
}
```
