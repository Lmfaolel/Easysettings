package com.easysettings.util;

import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * High-performance, zero-leak utility for parsing Minecraft color codes,
 * RGB hex codes, and small caps typography with LRU caching.
 */
public final class ColorUtil {

    private static final Pattern HEX_PATTERN_1 = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern HEX_PATTERN_2 = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    private static final Pattern HEX_PATTERN_3 = Pattern.compile("\\{#([A-Fa-f0-9]{6})\\}");
    private static final Pattern SMALL_CAPS_PATTERN = Pattern.compile("(?i)<(?:sc|smallcaps)>(.*?)</(?:sc|smallcaps)>");

    // High-speed, bounded LRU cache for colorized strings (max 1024 entries)
    private static final int MAX_CACHE_SIZE = 1024;
    private static final Map<String, String> COLOR_CACHE = Collections.synchronizedMap(
            new LinkedHashMap<>(256, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                    return size() > MAX_CACHE_SIZE;
                }
            }
    );

    private ColorUtil() {
    }

    /**
     * Colorize a single string with legacy and RGB hex color codes and parses <sc> tags.
     * Includes fast-path zero-allocation check and LRU caching.
     *
     * @param message Text to colorize
     * @return Colorized text
     */
    public static String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        // Fast-path: If string contains no color or tag symbols, return directly with 0 allocations
        if (message.indexOf('&') == -1 && message.indexOf('#') == -1 && message.indexOf('<') == -1 && message.indexOf('{') == -1) {
            return message;
        }

        // Check LRU cache to eliminate regex re-evaluation on repeated calls
        String cached = COLOR_CACHE.get(message);
        if (cached != null) {
            return cached;
        }

        String result = message;

        // Process <sc>...</sc> small caps tags only if '<' is present
        if (result.indexOf('<') != -1) {
            Matcher scMatcher = SMALL_CAPS_PATTERN.matcher(result);
            if (scMatcher.find()) {
                StringBuilder scBuilder = new StringBuilder(result.length());
                do {
                    String inner = scMatcher.group(1);
                    scMatcher.appendReplacement(scBuilder, Matcher.quoteReplacement(toSmallCaps(inner)));
                } while (scMatcher.find());
                scMatcher.appendTail(scBuilder);
                result = scBuilder.toString();
            }
        }

        // Process &#RRGGBB
        if (result.contains("&#")) {
            Matcher matcher1 = HEX_PATTERN_1.matcher(result);
            if (matcher1.find()) {
                StringBuilder sb1 = new StringBuilder(result.length() + 32);
                do {
                    String hex = matcher1.group(1);
                    matcher1.appendReplacement(sb1, ChatColor.of("#" + hex).toString());
                } while (matcher1.find());
                matcher1.appendTail(sb1);
                result = sb1.toString();
            }
        }

        // Process <#RRGGBB>
        if (result.contains("<#")) {
            Matcher matcher2 = HEX_PATTERN_2.matcher(result);
            if (matcher2.find()) {
                StringBuilder sb2 = new StringBuilder(result.length() + 32);
                do {
                    String hex = matcher2.group(1);
                    matcher2.appendReplacement(sb2, ChatColor.of("#" + hex).toString());
                } while (matcher2.find());
                matcher2.appendTail(sb2);
                result = sb2.toString();
            }
        }

        // Process {#RRGGBB}
        if (result.contains("{#")) {
            Matcher matcher3 = HEX_PATTERN_3.matcher(result);
            if (matcher3.find()) {
                StringBuilder sb3 = new StringBuilder(result.length() + 32);
                do {
                    String hex = matcher3.group(1);
                    matcher3.appendReplacement(sb3, ChatColor.of("#" + hex).toString());
                } while (matcher3.find());
                matcher3.appendTail(sb3);
                result = sb3.toString();
            }
        }

        // Translate standard '&' codes only if '&' is present
        if (result.indexOf('&') != -1) {
            result = ChatColor.translateAlternateColorCodes('&', result);
        }

        // Store in cache
        COLOR_CACHE.put(message, result);
        return result;
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
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case 'a': case 'A': sb.append('ᴀ'); break;
                case 'b': case 'B': sb.append('ʙ'); break;
                case 'c': case 'C': sb.append('ᴄ'); break;
                case 'd': case 'D': sb.append('ᴅ'); break;
                case 'e': case 'E': sb.append('ᴇ'); break;
                case 'f': case 'F': sb.append('ғ'); break;
                case 'g': case 'G': sb.append('ɢ'); break;
                case 'h': case 'H': sb.append('ʜ'); break;
                case 'i': case 'I': sb.append('ɪ'); break;
                case 'j': case 'J': sb.append('ᴊ'); break;
                case 'k': case 'K': sb.append('ᴋ'); break;
                case 'l': case 'L': sb.append('ʟ'); break;
                case 'm': case 'M': sb.append('ᴍ'); break;
                case 'n': case 'N': sb.append('ɴ'); break;
                case 'o': case 'O': sb.append('ᴏ'); break;
                case 'p': case 'P': sb.append('ᴘ'); break;
                case 'q': case 'Q': sb.append('ǫ'); break;
                case 'r': case 'R': sb.append('ʀ'); break;
                case 's': case 'S': sb.append('s'); break;
                case 't': case 'T': sb.append('ᴛ'); break;
                case 'u': case 'U': sb.append('ᴜ'); break;
                case 'v': case 'V': sb.append('ᴠ'); break;
                case 'w': case 'W': sb.append('ᴡ'); break;
                case 'x': case 'X': sb.append('x'); break;
                case 'y': case 'Y': sb.append('ʏ'); break;
                case 'z': case 'Z': sb.append('ᴢ'); break;
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
        if (lines == null || lines.isEmpty()) {
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

    /**
     * Clears internal color cache to release memory when reloading.
     */
    public static void clearCache() {
        COLOR_CACHE.clear();
    }
}
