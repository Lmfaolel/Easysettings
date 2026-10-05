package com.easysettings.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ColorUtilTest {

    @Test
    public void testLegacyColorize() {
        String input = "&aHello &bWorld";
        String output = ColorUtil.colorize(input);
        assertNotNull(output);
        assertTrue(output.contains("Hello"));
        assertTrue(output.contains("World"));
    }

    @Test
    public void testHexColorize() {
        String input = "&#FF00AAHex Color";
        String output = ColorUtil.colorize(input);
        assertNotNull(output);
        assertTrue(output.contains("Hex Color"));

        String input2 = "<#00FF88>Tag Hex";
        String output2 = ColorUtil.colorize(input2);
        assertNotNull(output2);
        assertTrue(output2.contains("Tag Hex"));

        String input3 = "{#112233}Brace Hex";
        String output3 = ColorUtil.colorize(input3);
        assertNotNull(output3);
        assertTrue(output3.contains("Brace Hex"));
    }

    @Test
    public void testColorizeList() {
        List<String> input = Arrays.asList("&aLine 1", "&#123456Line 2");
        List<String> output = ColorUtil.colorize(input);
        assertEquals(2, output.size());
        assertTrue(output.get(0).contains("Line 1"));
        assertTrue(output.get(1).contains("Line 2"));
    }

    @Test
    public void testStripColor() {
        String input = "&cRed &lBold text";
        String stripped = ColorUtil.stripColor(input);
        assertEquals("Red Bold text", stripped);
    }

    @Test
    public void testSmallCapsConversion() {
        String input = "Settings";
        String output = ColorUtil.toSmallCaps(input);
        assertEquals("sᴇᴛᴛɪɴɢs", output);
    }

    @Test
    public void testSmallCapsTagParsing() {
        String input = "&#66FCF1<sc>Player Settings</sc>";
        String output = ColorUtil.colorize(input);
        assertTrue(output.contains("ᴘʟᴀʏᴇʀ sᴇᴛᴛɪɴɢs"));
    }
}
