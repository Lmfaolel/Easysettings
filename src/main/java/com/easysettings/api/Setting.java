package com.easysettings.api;

import com.easysettings.api.action.SettingClickHandler;
import com.easysettings.api.action.SettingToggleHandler;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a setting displayed within the EasySettings GUI.
 */
public class Setting {

    private final String id;
    private String displayName;
    private int slot;
    private SettingType type;
    private Material item;
    private Material itemOn;
    private Material itemOff;
    private int customModelData;
    private boolean glowing;
    private boolean defaultState;
    private String permission;
    private List<String> description;
    private List<String> actions;
    private List<String> actionsOn;
    private List<String> actionsOff;
    private SettingClickHandler clickHandler;
    private SettingToggleHandler toggleHandler;
    private boolean fromConfig;

    public Setting(String id) {
        this.id = id;
        this.displayName = "&e" + id;
        this.slot = 0;
        this.type = SettingType.TOGGLE;
        this.item = Material.STONE;
        this.itemOn = null;
        this.itemOff = null;
        this.customModelData = 0;
        this.glowing = false;
        this.defaultState = false;
        this.permission = "";
        this.description = new ArrayList<>();
        this.actions = new ArrayList<>();
        this.actionsOn = new ArrayList<>();
        this.actionsOff = new ArrayList<>();
        this.clickHandler = null;
        this.toggleHandler = null;
        this.fromConfig = true;
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public SettingType getType() {
        return type;
    }

    public void setType(SettingType type) {
        this.type = type;
    }

    public Material getItem() {
        return item;
    }

    public void setItem(Material item) {
        this.item = item != null ? item : Material.STONE;
    }

    public Material getItemOn() {
        return itemOn;
    }

    public void setItemOn(Material itemOn) {
        this.itemOn = itemOn;
    }

    public Material getItemOff() {
        return itemOff;
    }

    public void setItemOff(Material itemOff) {
        this.itemOff = itemOff;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public void setCustomModelData(int customModelData) {
        this.customModelData = customModelData;
    }

    public boolean isGlowing() {
        return glowing;
    }

    public void setGlowing(boolean glowing) {
        this.glowing = glowing;
    }

    public boolean isDefaultState() {
        return defaultState;
    }

    public void setDefaultState(boolean defaultState) {
        this.defaultState = defaultState;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission != null ? permission : "";
    }

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description != null ? new ArrayList<>(description) : new ArrayList<>();
    }

    public List<String> getActions() {
        return actions;
    }

    public void setActions(List<String> actions) {
        this.actions = actions != null ? new ArrayList<>(actions) : new ArrayList<>();
    }

    public List<String> getActionsOn() {
        return actionsOn;
    }

    public void setActionsOn(List<String> actionsOn) {
        this.actionsOn = actionsOn != null ? new ArrayList<>(actionsOn) : new ArrayList<>();
    }

    public List<String> getActionsOff() {
        return actionsOff;
    }

    public void setActionsOff(List<String> actionsOff) {
        this.actionsOff = actionsOff != null ? new ArrayList<>(actionsOff) : new ArrayList<>();
    }

    public SettingClickHandler getClickHandler() {
        return clickHandler;
    }

    public void setClickHandler(SettingClickHandler clickHandler) {
        this.clickHandler = clickHandler;
    }

    public SettingToggleHandler getToggleHandler() {
        return toggleHandler;
    }

    public void setToggleHandler(SettingToggleHandler toggleHandler) {
        this.toggleHandler = toggleHandler;
    }

    public boolean isFromConfig() {
        return fromConfig;
    }

    public void setFromConfig(boolean fromConfig) {
        this.fromConfig = fromConfig;
    }

    /**
     * Builder for creating Setting instances programmatically.
     */
    public static class Builder {
        private final Setting setting;

        public Builder(String id) {
            this.setting = new Setting(id);
            this.setting.setFromConfig(false);
        }

        public Builder displayName(String displayName) {
            setting.setDisplayName(displayName);
            return this;
        }

        public Builder slot(int slot) {
            setting.setSlot(slot);
            return this;
        }

        public Builder type(SettingType type) {
            setting.setType(type);
            return this;
        }

        public Builder item(Material item) {
            setting.setItem(item);
            return this;
        }

        public Builder itemOn(Material itemOn) {
            setting.setItemOn(itemOn);
            return this;
        }

        public Builder itemOff(Material itemOff) {
            setting.setItemOff(itemOff);
            return this;
        }

        public Builder customModelData(int customModelData) {
            setting.setCustomModelData(customModelData);
            return this;
        }

        public Builder glowing(boolean glowing) {
            setting.setGlowing(glowing);
            return this;
        }

        public Builder defaultState(boolean defaultState) {
            setting.setDefaultState(defaultState);
            return this;
        }

        public Builder permission(String permission) {
            setting.setPermission(permission);
            return this;
        }

        public Builder description(List<String> description) {
            setting.setDescription(description);
            return this;
        }

        public Builder actions(List<String> actions) {
            setting.setActions(actions);
            return this;
        }

        public Builder actionsOn(List<String> actionsOn) {
            setting.setActionsOn(actionsOn);
            return this;
        }

        public Builder actionsOff(List<String> actionsOff) {
            setting.setActionsOff(actionsOff);
            return this;
        }

        public Builder onClick(SettingClickHandler clickHandler) {
            setting.setClickHandler(clickHandler);
            return this;
        }

        public Builder onToggle(SettingToggleHandler toggleHandler) {
            setting.setToggleHandler(toggleHandler);
            return this;
        }

        public Setting build() {
            return setting;
        }
    }
}
