package com.easysettings.api;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class SettingTest {

    @Test
    public void testSettingBuilder() {
        Setting setting = Setting.builder("auto_pickup")
                .displayName("&#00FFCC&lAuto Pickup")
                .slot(14)
                .type(SettingType.TOGGLE)
                .item(Material.HOPPER)
                .defaultState(true)
                .permission("easysettings.autopickup")
                .description(Arrays.asList("&7Automatically pick up drops", "&7Status: %status%"))
                .actionsOn(Arrays.asList("[message] &aEnabled!"))
                .actionsOff(Arrays.asList("[message] &cDisabled!"))
                .build();

        assertEquals("auto_pickup", setting.getId());
        assertEquals("&#00FFCC&lAuto Pickup", setting.getDisplayName());
        assertEquals(14, setting.getSlot());
        assertEquals(SettingType.TOGGLE, setting.getType());
        assertEquals(Material.HOPPER, setting.getItem());
        assertTrue(setting.isDefaultState());
        assertEquals("easysettings.autopickup", setting.getPermission());
        assertEquals(2, setting.getDescription().size());
        assertEquals(1, setting.getActionsOn().size());
        assertEquals(1, setting.getActionsOff().size());
        assertFalse(setting.isFromConfig());
    }

    @Test
    public void testSettingActionBuilder() {
        Setting setting = Setting.builder("spawn_warp")
                .displayName("&bWarp to Spawn")
                .slot(4)
                .type(SettingType.ACTION)
                .item(Material.COMPASS)
                .actions(Arrays.asList("[player] spawn", "[close]"))
                .build();

        assertEquals("spawn_warp", setting.getId());
        assertEquals(SettingType.ACTION, setting.getType());
        assertEquals(2, setting.getActions().size());
    }
}
