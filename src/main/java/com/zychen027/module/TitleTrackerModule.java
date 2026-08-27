package com.zychen027.module;

import com.zenith.module.api.Module;
import com.zenith.network.codec.PacketHandlerCodec;
import com.zenith.network.codec.PacketHandlerStateCodec;
import org.geysermc.mcprotocollib.protocol.data.ProtocolState;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.title.ClientboundClearTitlesPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.title.ClientboundSetActionBarTextPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.title.ClientboundSetSubtitleTextPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.title.ClientboundSetTitleTextPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.title.ClientboundSetTitlesAnimationPacket;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

import static com.zychen027.PlayerState.plain;

/**
 * Tracks the currently displayed title / subtitle / action bar by listening to
 * the title packets the BOT receives from the destination server (client codec).
 *
 * State is static so commands can read it directly.
 * Every received title-family packet is logged to the console for observability.
 */
public class TitleTrackerModule extends Module {

    private static final long MS_PER_TICK = 50L;
    private static final long DEFAULT_TOTAL_TICKS = 70L; // 20 in + 30 stay + 20 out

    // ---- tracked on-screen state ----
    private static volatile String lastTitle = null;
    private static volatile String lastSubtitle = null;
    private static volatile String lastActionBar = null;
    private static volatile long titleShowStartMs = 0L;
    private static volatile long totalDurationTicks = DEFAULT_TOTAL_TICKS;

    // ---- observability counters ----
    private static volatile long titleCount = 0L;
    private static volatile long subtitleCount = 0L;
    private static volatile long actionBarCount = 0L;
    private static volatile long animationCount = 0L;
    private static volatile long clearCount = 0L;
    private static volatile long lastPacketTimeMs = 0L;

    @Override
    public boolean enabledSetting() {
        return true; // always on; purely passive listener
    }

    @Override
    public PacketHandlerCodec registerClientPacketHandlerCodec() {
        // client codec: fires on every packet the BOT receives from the destination
        // server - independent of whether any player is connected to the proxy.
        return PacketHandlerCodec.clientBuilder()
            .setId("playerstate_title_tracker")
            .setPriority(-5) // after standard client packet handlers
            .state(ProtocolState.GAME, PacketHandlerStateCodec.clientBuilder()
                .inbound(ClientboundSetTitleTextPacket.class, (packet, session) -> {
                    final String text = plain(packet.getText());
                    if (!java.util.Objects.equals(text, lastTitle)) {
                        lastSubtitle = null;
                        titleShowStartMs = System.currentTimeMillis();
                    }
                    lastTitle = text;
                    titleCount++;
                    lastPacketTimeMs = System.currentTimeMillis();
                    return packet;
                })
                .inbound(ClientboundSetSubtitleTextPacket.class, (packet, session) -> {
                    lastSubtitle = plain(packet.getText());
                    subtitleCount++;
                    lastPacketTimeMs = System.currentTimeMillis();
                    return packet;
                })
                .inbound(ClientboundSetTitlesAnimationPacket.class, (packet, session) -> {
                    totalDurationTicks = Math.max(1, packet.getFadeIn())
                        + Math.max(0, packet.getStay())
                        + Math.max(0, packet.getFadeOut());
                    if (lastTitle != null) titleShowStartMs = System.currentTimeMillis();
                    animationCount++;
                    lastPacketTimeMs = System.currentTimeMillis();
                    return packet;
                })
                .inbound(ClientboundSetActionBarTextPacket.class, (packet, session) -> {
                    lastActionBar = plain(packet.getText());
                    actionBarCount++;
                    lastPacketTimeMs = System.currentTimeMillis();
                    return packet;
                })
                .inbound(ClientboundClearTitlesPacket.class, (packet, session) -> {
                    lastTitle = null;
                    lastSubtitle = null;
                    titleShowStartMs = 0L;
                    clearCount++;
                    lastPacketTimeMs = System.currentTimeMillis();
                    return packet;
                })
                .build())
            .build();
    }

    // ---------------------------------------------------------------- accessors

    public static @Nullable String getLastTitle() { return lastTitle; }
    public static @Nullable String getLastSubtitle() { return lastSubtitle; }
    public static @Nullable String getLastActionBar() { return lastActionBar; }

    /** true while the (fadeIn+stay+fadeOut) window since show-start hasn't elapsed */
    public static boolean isTitleOnScreen() {
        return remainingTicks() > 0 && lastTitle != null;
    }

    public static long remainingTicks() {
        if (titleShowStartMs <= 0L || lastTitle == null) return 0L;
        final long elapsedTicks = (System.currentTimeMillis() - titleShowStartMs) / MS_PER_TICK;
        return totalDurationTicks - elapsedTicks;
    }

    public static long getTitleCount() { return titleCount; }
    public static long getSubtitleCount() { return subtitleCount; }
    public static long getActionBarCount() { return actionBarCount; }
    public static long getAnimationCount() { return animationCount; }
    public static long getClearCount() { return clearCount; }
    public static long getLastPacketTimeMs() { return lastPacketTimeMs; }

    /** true if any title-family packet has ever been received */
    public static boolean hasAnyData() {
        return titleCount + subtitleCount + actionBarCount + animationCount + clearCount > 0;
    }

    public static void reset() {
        lastTitle = null;
        lastSubtitle = null;
        lastActionBar = null;
        titleShowStartMs = 0L;
        totalDurationTicks = DEFAULT_TOTAL_TICKS;
    }

    public static String describe() {
        if (!hasAnyData()) return "No title-family packets received yet since proxy start";
        return "title=" + titleCount + " subtitle=" + subtitleCount + " actionbar=" + actionBarCount
            + " animation=" + animationCount + " clear=" + clearCount
            + " last=" + (lastPacketTimeMs > 0 ? Instant.ofEpochMilli(lastPacketTimeMs).toString() : "-");
    }
}
