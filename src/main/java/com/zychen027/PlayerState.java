package com.zychen027;

import com.zenith.Proxy;
import com.zenith.cache.data.entity.Entity;
import com.zenith.cache.data.entity.EntityPlayer;
import com.zenith.cache.data.PlayerCache;
import com.zenith.util.ComponentSerializer;
import net.kyori.adventure.text.Component;

import java.util.Locale;

import static com.zenith.Globals.CACHE;

/**
 * Shared data access & formatting helpers for the player-state commands.
 */
public final class PlayerState {

    private PlayerState() {}

    /**
     * @return the bot's own cached player entity, or null if not connected.
     */
    public static EntityPlayer getBot() {
        if (!Proxy.getInstance().isConnected()) return null;
        try {
            final PlayerCache pc = CACHE.getPlayerCache();
            if (pc == null) return null;
            return pc.getThePlayer();
        } catch (final Exception e) {
            return null;
        }
    }

    /** Plain text of a component, null-safe; empty/blank collapses to null. */
    public static String plain(final Component component) {
        if (component == null) return null;
        final String s = ComponentSerializer.serializePlain(component);
        return (s == null || s.isBlank()) ? null : s;
    }

    /** Short positional string: "x y z". */
    public static String fmtPos(final Entity e) {
        return String.format(Locale.ROOT, "%.2f %.2f %.2f", e.getX(), e.getY(), e.getZ());
    }

    /** Block coordinates. */
    public static String fmtBlockPos(final Entity e) {
        return (int) Math.floor(e.getX()) + " " + (int) Math.floor(e.getY()) + " " + (int) Math.floor(e.getZ());
    }

    /** Normalize yaw to [-180, 180). */
    public static float normYaw(final float yaw) {
        float y = yaw % 360f;
        if (y >= 180f) y -= 360f;
        if (y < -180f) y += 360f;
        return y;
    }

    /** Compass direction name for a Minecraft yaw (0 = south / +Z, 90 = west / -X). */
    public static String facing(final float yawIn) {
        final float yaw = normYaw(yawIn);
        // bucket centers: 0=S, +/-45=SW/SE, +/-90=W/E, +/-135=NW/NE, 180=N
        final String[] dirs = {"south", "south-west", "west", "north-west",
                               "north", "north-east", "east", "south-east"};
        int idx = Math.round(yaw / 45f); // -4..4
        idx &= 7;                        // map -4..-1 -> 4..7
        return dirs[idx];
    }

    public static String axisHint(final float yaw) {
        final String f = facing(yaw);
        return switch (f) {
            case "south" -> "+Z";
            case "north" -> "-Z";
            case "east" -> "+X";
            case "west" -> "-X";
            default -> "";
        };
    }

    /** Direction name incl. pitch descriptor. */
    public static String compassDirection(final float yaw, final float pitch) {
        String d = facing(yaw);
        if (pitch >= 60f) d += " (down)";
        else if (pitch <= -60f) d += " (up)";
        return d;
    }

    public static float safeFloat(final Float f) {
        return f == null ? 0f : f;
    }

    /** Roman numeral for effect amplifier display (amplifier 0 -> I). */
    public static String roman(final int n) {
        return switch (n) {
            case 1 -> "II"; case 2 -> "III"; case 3 -> "IV"; case 4 -> "V";
            case 5 -> "VI"; case 6 -> "VII"; case 7 -> "VIII"; case 8 -> "IX";
            case 9 -> "X";
            default -> n <= 0 ? "I" : "x" + (n + 1);
        };
    }

    /** Format a duration in ticks as h/m/s; negative means infinite. */
    public static String fmtTicks(long ticks) {
        if (ticks < 0) return "\u221E"; // infinity
        long seconds = ticks / 20L;
        long h = seconds / 3600L;
        long m = (seconds % 3600L) / 60L;
        long s = seconds % 60L;
        if (h > 0) return h + "h" + m + "m" + s + "s";
        if (m > 0) return m + "m" + s + "s";
        return s + "s";
    }
}
