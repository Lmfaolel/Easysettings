# ⚙️ EasySettings

**EasySettings** is a fully modular, extensible Minecraft GUI settings plugin for Spigot / Paper (1.20+). It provides a sleek in-game settings GUI for players, a full in-game command editor for server administrators, and a comprehensive Developer API that lets other plugins easily add their own settings, actions, and callbacks without hassle.

---

## 🌟 Key Features

- **Intuitive GUI (`/setting`)**: Players can easily toggle preferences or trigger actions with live state updates and glowing effects.
- **Interactive Visual GUI Editor (`/setting editor`)**:
  - Mirror layout of your GUI in real-time.
  - Click any empty slot (`+ Add Setting`) to instantly place a setting.
  - Left-Click settings to open the **Inspector Menu** (change name, icon, type, glint, permission, lore, actions).
  - Shift-Left-Click to pick up and move a setting to any slot.
  - Right-Click to delete.
  - Integrated chat input capture with `cancel` support.
- **In-Game Command Editor**:
  - `/setting add <name>`: Automatically finds the next free slot and adds a new setting with preconfigured templates.
  - `/setting edit <name>`: Directly opens the setting inspector GUI, or accepts command arguments for quick edits.
  - `/setting delete <name>`: Remove any setting instantly from memory and disk.
- **Color & Hex Support**: Supports standard formatting (`&a`, `&l`) and RGB Hex colors in multiple formats: `&#RRGGBB`, `<#RRGGBB>`, and `{#RRGGBB}`.
- **Configurable GUI**: Full control over GUI size (9 to 54), title, background filler glass pane, and close button in [`config.yml`](file:///e:/Easysettings/src/main/resources/config.yml).
- **Dual Setting Types**:
  - `TOGGLE`: Persistent per-player ON/OFF toggle with `%status%` placeholder, optional glowing state, and distinct ON/OFF actions.
  - `ACTION`: One-click button to trigger commands, messages, or custom plugin hooks.
- **Rich Action Engine**:
  - `[player] <command>`: Execute as player
  - `[console] <command>`: Execute from console (`%player%` supported)
  - `[message] <text>`: Send colorized message to player
  - `[broadcast] <text>`: Broadcast to server
  - `[sound] <SOUND:VOL:PITCH>`: Play sound effect
  - `[close]`: Closes the GUI
  - `[refresh]`: Refreshes GUI items
  - `[api] <tag>`: Triggers custom external plugin actions
- **Developer Java API (`EasySettingsAPI`)**: Register settings programmatically with a fluent builder, handle clicks/toggles in Java code, or register custom action tags.
- **PlaceholderAPI Integration**: Soft-dependency support for custom placeholders in titles, display names, and lore.

---

## 💻 Commands & Permissions

### Permissions
- `easysettings.use` *(Default: true)*: Allows opening the GUI with `/setting`.
- `easysettings.admin` *(Default: op)*: Grants access to `/setting editor`, `/setting add`, `edit`, `delete`, `list`, and `reload`.

### Commands
| Command | Description |
| :--- | :--- |
| `/setting` | Opens the settings GUI for players. |
| `/setting editor` | **Opens the interactive visual GUI settings editor.** |
| `/setting help` | Displays the help menu. |
| `/setting edit <id>` | Opens the Inspector GUI for a specific setting. |
| `/setting add <id>` | Adds a new setting with default template at the next free slot. |
| `/setting delete <id>` | Permanently deletes a setting. |
| `/setting edit <id> displayname <text>` | Updates display name (supports color codes & Hex). |
| `/setting edit <id> item <material \| hand>` | Sets the item material or uses the item held in your main hand. |
| `/setting edit <id> slot <0-53>` | Moves the setting to a different slot. |
| `/setting edit <id> description <lore...>` | Sets lore. Use `\|` to separate lines (e.g. `Line 1 \| Line 2`). |
| `/setting edit <id> action <action...>` | Sets action string or command. |
| `/setting edit <id> action add <action>` | Adds an action to the setting (supports `[on]` and `[off]` prefixes). |
| `/setting edit <id> action clear` | Clears all actions from the setting. |
| `/setting edit <id> type <TOGGLE \| ACTION>` | Changes setting mode. |
| `/setting edit <id> permission <perm \| none>` | Sets required permission to view/use. |
| `/setting list` | Lists all registered settings, slots, types, and sources. |
| `/setting reload` | Reloads `config.yml` and `settings.yml`. |

---

## 🛠 Developer API (`EasySettingsAPI`)

Other plugins can interact with EasySettings through [`com.easysettings.api.EasySettingsAPI`](file:///e:/Easysettings/src/main/java/com/easysettings/api/EasySettingsAPI.java).

### 1. Registering a Custom Setting
```java
import com.easysettings.api.EasySettingsAPI;
import com.easysettings.api.Setting;
import com.easysettings.api.SettingType;
import org.bukkit.Material;

Setting mySetting = Setting.builder("auto_pickup")
    .displayName("&#00FFCC&lAuto Pickup")
    .slot(14)
    .type(SettingType.TOGGLE)
    .item(Material.HOPPER)
    .defaultState(false)
    .permission("myplugin.autopickup")
    .description(List.of(
        "&7Automatically pick up mined items.",
        "",
        "&7Status: %status%",
        "&eClick to toggle!"
    ))
    .onToggle((player, setting, newState) -> {
        player.sendMessage("Auto Pickup is now " + (newState ? "ENABLED" : "DISABLED"));
    })
    .build();

// Register the setting directly into the GUI:
EasySettingsAPI.registerSetting(mySetting);
```

### 2. Registering Custom Action Handlers
External plugins can register their own action prefixes for YAML configurations (e.g., `[myeco] take 100`):

```java
EasySettingsAPI.registerActionHandler("myeco", (player, argument) -> {
    // argument = "take 100"
    String[] parts = argument.split(" ");
    if (parts[0].equalsIgnoreCase("take")) {
        double amount = Double.parseDouble(parts[1]);
        // Deduct from player balance...
    }
});
```

### 3. Querying / Modifying Player Setting States
```java
// Check if a setting is enabled for a player:
boolean isEnabled = EasySettingsAPI.isSettingEnabled(player.getUniqueId(), "auto_pickup");

// Update player setting state:
EasySettingsAPI.setSettingEnabled(player.getUniqueId(), "auto_pickup", true);

// Programmatically trigger toggle (fires events, actions & handlers):
EasySettingsAPI.toggleSetting(player, "auto_pickup");

// Open the GUI for a player:
EasySettingsAPI.openGUI(player);
```

### 4. Listening to Bukkit Events
EasySettings fires custom Bukkit events:
- [`PlayerSettingClickEvent`](file:///e:/Easysettings/src/main/java/com/easysettings/api/event/PlayerSettingClickEvent.java) (Cancellable)
- [`PlayerSettingToggleEvent`](file:///e:/Easysettings/src/main/java/com/easysettings/api/event/PlayerSettingToggleEvent.java) (Cancellable)
- [`EasySettingsReloadEvent`](file:///e:/Easysettings/src/main/java/com/easysettings/api/event/EasySettingsReloadEvent.java)

---

## 📂 Configuration Files

- [`config.yml`](file:///e:/Easysettings/src/main/resources/config.yml): GUI title, size, status placeholders, filler glass panes, close button, and messages.
- [`settings.yml`](file:///e:/Easysettings/src/main/resources/settings.yml): YAML definitions for all settings added either manually or via in-game `/setting add` / `/setting edit` commands.

---

## 📦 Building the Plugin

Built with Maven and Java 17:
```bash
mvn clean package
```
Compiled output JAR is generated at:
`target/EasySettings-1.0.0.jar`
