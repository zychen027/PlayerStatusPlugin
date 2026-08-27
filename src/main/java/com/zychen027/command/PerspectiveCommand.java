package com.zychen027.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.zenith.command.api.Command;
import com.zenith.command.api.CommandCategory;
import com.zenith.command.api.CommandContext;
import com.zenith.command.api.CommandUsage;

import static java.lang.String.format;
import static com.zychen027.PlayerState.compassDirection;
import static com.zychen027.PlayerState.getBot;
import static com.zychen027.PlayerState.safeFloat;

public class PerspectiveCommand extends Command {
    @Override
    public CommandUsage commandUsage() {
        return CommandUsage.simple(
            "perspective",
            CommandCategory.INFO,
            "Outputs the bot's current view direction (yaw / pitch / head yaw)"
        );
    }

    @Override
    public LiteralArgumentBuilder<CommandContext> register() {
        return command("perspective").executes(c -> {
            final var bot = getBot();
            if (bot == null) {
                c.getSource().getEmbed()
                    .title("Not Connected")
                    .errorColor()
                    .addField("Perspective", "Bot is not connected to the server.");
                return;
            }
            final float yaw = safeFloat(bot.getYaw());
            final float pitch = safeFloat(bot.getPitch());
            c.getSource().getEmbed()
                .title("Perspective")
                .primaryColor()
                .addField("Yaw", format("%.1f\u00B0", yaw))
                .addField("Pitch", format("%.1f\u00B0", pitch))
                .addField("Facing", compassDirection(yaw, pitch), true)
                .addField("Head Yaw", format("%.1f\u00B0", safeFloat(bot.getHeadYaw())), true);
        });
    }

}
