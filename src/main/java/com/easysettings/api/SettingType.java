package com.easysettings.api;

/**
 * Types of settings available in EasySettings.
 */
public enum SettingType {
    /**
     * A toggleable setting that persists an ON/OFF state for players.
     * Executes actions-on when toggled ON, and actions-off when toggled OFF.
     */
    TOGGLE,

    /**
     * An action button setting that executes actions when clicked.
     */
    ACTION
}
