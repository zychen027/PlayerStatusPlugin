package com.zychen027.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.zenith.cache.data.entity.EntityLiving;
import com.zenith.cache.data.entity.PotionEffect;
import com.zenith.command.api.Command;
import com.zenith.command.api.CommandCategory;
import com.zenith.command.api.CommandContext;
import com.zenith.command.api.CommandUsage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.lang.String.format;
import static com.zychen027.PlayerState.compassDirection;
import static com.zychen027.PlayerState.fmtPos;
import static com.zychen027.PlayerState.fmtTicks;
import static com.zychen027.PlayerState.getBot;
import static com.zychen027.PlayerState.roman;
import static com.zychen027.PlayerState.safeFloat;

public class PlayerStatusCommand extends Command {
    @Override
    public CommandUsage commandUsage() {
        return CommandUsage.simple(
            "playerStatus",
            CommandCategory.INFO,
            "Outputs the bot's health, active status effects, coordinates and view direction"
        );
    }

    @Override
    public LiteralArgumentBuilder<CommandContext> register() {
        return command("playerStatus").executes(c -> {
            final var bot = getBot();
            if (bot == null) {
                c.getSource().getEmbed()
                    .title("Not Connected")
                    .errorColor()
                    .addField("Player Status", "Bot is not connected to the server.");
                return;
            }
            final Float health = bot.getHealth();

            final List<String> effectLines = new ArrayList<>();
            if (bot instanceof EntityLiving living
                && living.getPotionEffectMap() != null
                && !living.getPotionEffectMap().isEmpty()) {
                living.getPotionEffectMap().values().stream()
                    .sorted(Comparator.comparing(e -> e.getEffect().name()))
                    .forEach(e -> effectLines.add(formatEffect(e)));
            }

            final float yaw = safeFloat(bot.getYaw());
            final float pitch = safeFloat(bot.getPitch());

            c.getSource().getEmbed()
                .title("Player Status")
                .primaryColor()
                .addField("Health", health == null ? "Unknown" : format("%.1f \u2764", health))
                .addField("Effects", effectLines.isEmpty()
                    ? "None"
                    : String.join("\n", effectLines))
                .addField("Coordinates", fmtPos(bot))
                .addField("View", format("%.1f\u00B0 / %.1f\u00B0 (%s)",
                    yaw, pitch, compassDirection(yaw, pitch)));
        });
    }

    private static String formatEffect(final PotionEffect e) {
        return format("**%s %s** \u2014 %s",
            prettify(e.getEffect().name()),
            roman(e.getAmplifier()),
            fmtTicks(e.getDuration()));
    }

    private static String prettify(final String enumName) {
        final String[] parts = enumName.toLowerCase().split("_");
        final StringBuilder sb = new StringBuilder();
        for (final String p : parts) {
            if (p.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
        }
        return sb.toString();
    }
}
