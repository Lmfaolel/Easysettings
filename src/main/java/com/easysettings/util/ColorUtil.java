package com.easysettings.util;

import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Modern utility for parsing Minecraft color codes, RGB hex codes, and small caps typography.
 * Supports:
 * - Legacy format: &a, &c, &l, etc.
 * - Hex format 1: &#RRGGBB
 * - Hex format 2: <#RRGGBB>
 * - Hex format 3: {#RRGGBB}
 * - Small caps tag: <sc>text</sc> or <smallcaps>text</smallcaps>
 */
public final class ColorUtil {

    private static final Pattern HEX_PATTERN_1 = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern HEX_PATTERN_2 = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    private static final Pattern HEX_PATTERN_3 = Pattern.compile("\\{#([A-Fa-f0-9]{6})\\}");
    private static final Pattern SMALL_CAPS_PATTERN = Pattern.compile("(?i)<(?:sc|smallcaps)>(.*?)</(?:sc|smallcaps)>");

    private ColorUtil() {
    }

    /**
     * Colorize a single string with legacy and RGB hex color codes and parses <sc> tags.
     *
     * @param message Text to colorize
     * @return Colorized text
     */
    public static String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        // Process <sc>...</sc> small caps tags
        Matcher scMatcher = SMALL_CAPS_PATTERN.matcher(message);
        StringBuilder scBuilder = new StringBuilder();
        while (scMatcher.find()) {
            String inner = scMatcher.group(1);
            scMatcher.appendReplacement(scBuilder, Matcher.quoteReplacement(toSmallCaps(inner)));
        }
        scMatcher.appendTail(scBuilder);
        message = scBuilder.toString();

        // Process &#RRGGBB
        Matcher matcher1 = HEX_PATTERN_1.matcher(message);
        StringBuilder sb1 = new StringBuilder();
        while (matcher1.find()) {
            String hex = matcher1.group(1);
            matcher1.appendReplacement(sb1, ChatColor.of("#" + hex).toString());
        }
        matcher1.appendTail(sb1);
        message = sb1.toString();

        // Process <#RRGGBB>
        Matcher matcher2 = HEX_PATTERN_2.matcher(message);
        StringBuilder sb2 = new StringBuilder();
        while (matcher2.find()) {
            String hex = matcher2.group(1);
            matcher2.appendReplacement(sb2, ChatColor.of("#" + hex).toString());
        }
        matcher2.appendTail(sb2);
        message = sb2.toString();

        // Process {#RRGGBB}
        Matcher matcher3 = HEX_PATTERN_3.matcher(message);
        StringBuilder sb3 = new StringBuilder();
        while (matcher3.find()) {
            String hex = matcher3.group(1);
            matcher3.appendReplacement(sb3, ChatColor.of("#" + hex).toString());
        }
        matcher3.appendTail(sb3);
        message = sb3.toString();

        // Translate standard '&' codes
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Converts alphabetical letters in a string into Unicode small capital characters.
     *
     * @param text Original text
     * @return Text transformed into small caps
     */
    public static String toSmallCaps(String text) {
        if (text == null) return null;
        StringBuilder sb = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            switch (Character.toLowerCase(c)) {
                case 'a': sb.append('ᴀ'); break;
                case 'b': sb.append('ʙ'); break;
                case 'c': sb.append('ᴄ'); break;
                case 'd': sb.append('ᴅ'); break;
                case 'e': sb.append('ᴇ'); break;
                case 'f': sb.append('ғ'); break;
                case 'g': sb.append('ɢ'); break;
                case 'h': sb.append('ʜ'); break;
                case 'i': sb.append('ɪ'); break;
                case 'j': sb.append('ᴊ'); break;
                case 'k': sb.append('ᴋ'); break;
                case 'l': sb.append('ʟ'); break;
                case 'm': sb.append('ᴍ'); break;
                case 'n': sb.append('ɴ'); break;
                case 'o': sb.append('ᴏ'); break;
                case 'p': sb.append('ᴘ'); break;
                case 'q': sb.append('ǫ'); break;
                case 'r': sb.append('ʀ'); break;
                case 's': sb.append('s'); break;
                case 't': sb.append('ᴛ'); break;
                case 'u': sb.append('ᴜ'); break;
                case 'v': sb.append('ᴠ'); break;
                case 'w': sb.append('ᴡ'); break;
                case 'x': sb.append('x'); break;
                case 'y': sb.append('ʏ'); break;
                case 'z': sb.append('ᴢ'); break;
                default: sb.append(c); break;
            }
        }
        return sb.toString();
    }

    /**
     * Colorize a list of strings (e.g. lore).
     *
     * @param lines List of strings to colorize
     * @return List of colorized strings
     */
    public static List<String> colorize(List<String> lines) {
        if (lines == null) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>(lines.size());
        for (String line : lines) {
            result.add(colorize(line));
        }
        return result;
    }

    /**
     * Strip all color codes from text.
     *
     * @param message Text with color codes
     * @return Clean uncolored text
     */
    public static String stripColor(String message) {
        if (message == null) {
            return null;
        }
        return ChatColor.stripColor(colorize(message));
    }
}
