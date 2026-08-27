package com.zychen027.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.zenith.command.api.Command;
import com.zenith.command.api.CommandCategory;
import com.zenith.command.api.CommandContext;
import com.zenith.command.api.CommandUsage;

import static com.zychen027.PlayerState.fmtBlockPos;
import static com.zychen027.PlayerState.fmtPos;
import static com.zychen027.PlayerState.getBot;

public class PosCommand extends Command {
    @Override
    public CommandUsage commandUsage() {
        return CommandUsage.simple(
            "pos",
            CommandCategory.INFO,
            "Outputs the bot's current coordinates"
        );
    }

    @Override
    public LiteralArgumentBuilder<CommandContext> register() {
        return command("pos").executes(c -> {
            final var bot = getBot();
            if (bot == null) {
                c.getSource().getEmbed()
                    .title("Not Connected")
                    .errorColor()
                    .addField("Position", "Bot is not connected to the server.");
                return;
            }
            c.getSource().getEmbed()
                .title("Position")
                .primaryColor()
                .addField("Coordinates", fmtPos(bot))
                .addField("Block", fmtBlockPos(bot));
        });
    }
}
