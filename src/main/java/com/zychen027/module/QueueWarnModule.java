package com.zychen027.module;

import com.zenith.module.api.Module;
import com.zenith.network.codec.PacketHandlerCodec;
import com.zenith.network.codec.PacketHandlerStateCodec;
import org.geysermc.mcprotocollib.protocol.data.ProtocolState;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.title.ClientboundSetActionBarTextPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.title.ClientboundSetSubtitleTextPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.title.ClientboundSetTitleTextPacket;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.zychen027.PlayerState.plain;
import static com.zychen027.PlayerStatePlugin.PLUGIN_CONFIG;

/**
 * QueueWarn3c - watches the on-screen title / subtitle / action bar.
 *
 * Extracts the number after the configured keyword (default "Position in queue: ").
 * When the number CHANGES, the new position is logged at info level.
 * When the position is <= threshold, it is logged at warn level instead.
 */
public class QueueWarnModule extends Module {
    private volatile Integer lastPosition = null;

    @Override
    public boolean enabledSetting() {
        return PLUGIN_CONFIG.queueWarn.enabled;
    }

    @Override
    public PacketHandlerCodec registerClientPacketHandlerCodec() {
        // client codec: sees every title/subtitle/actionbar packet the BOT receives
        // from the destination server, whether or not a player is connected.
        return PacketHandlerCodec.clientBuilder()
            .setId("queuewarn3c")
            .setPriority(-5) // after standard client packet handlers
            .state(ProtocolState.GAME, PacketHandlerStateCodec.clientBuilder()
                .inbound(ClientboundSetTitleTextPacket.class, (packet, session) -> {
                    check(plain(packet.getText()));
                    return packet;
                })
                .inbound(ClientboundSetSubtitleTextPacket.class, (packet, session) -> {
                    check(plain(packet.getText()));
                    return packet;
                })
                .inbound(ClientboundSetActionBarTextPacket.class, (packet, session) -> {
                    check(plain(packet.getText()));
                    return packet;
                })
                .build())
            .build();
    }

    private void check(final String text) {
        if (text == null || text.isBlank()) return;
        final var cfg = PLUGIN_CONFIG.queueWarn;
        if (!cfg.enabled) return;
        final String keyword = cfg.keyword;
        if (keyword == null || keyword.isBlank()) return;

        final Integer position = extractNumberAfterKeyword(text, keyword);
        if (position == null) return;

        // only log when the position actually changed
        if (position.equals(lastPosition)) return;
        final boolean first = lastPosition == null;
        lastPosition = position;

        if (position <= cfg.threshold) {
            warn(GREEN + "[QueueWarn3c] position: {} ({} threshold {})" + RESET,
                position, first ? "initial," : "changed, now <=", cfg.threshold);
        } else {
            info(YELLOW + "[QueueWarn3c] position: {}" + RESET, position);
        }
    }

    // ANSI colors for console output
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN  = "\u001B[32m";
    private static final String RESET  = "\u001B[0m";

    /**
     * Extracts the first integer appearing after the keyword in the text.
     * e.g. text="555 Playing | Position in queue: 121", keyword="Position in queue: " -> 121
     *
     * @return null if keyword not present or no number follows it
     */
    public static Integer extractNumberAfterKeyword(final String text, final String keyword) {
        final Pattern p = Pattern.compile(
            Pattern.quote(keyword) + "[^0-9\\-]*(-?\\d+)",
            Pattern.CASE_INSENSITIVE);
        final Matcher m = p.matcher(text);
        if (!m.find()) return null;
        try {
            return Integer.parseInt(m.group(1));
        } catch (final NumberFormatException e) {
            return null;
        }
    }
}
