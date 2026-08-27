package com.zychen027.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.zenith.command.api.Command;
import com.zenith.command.api.CommandCategory;
import com.zenith.command.api.CommandContext;
import com.zenith.command.api.CommandUsage;
import com.zenith.discord.Embed;
import com.zychen027.module.QueueWarnModule;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.zenith.command.brigadier.ToggleArgumentType.getToggle;
import static com.zenith.command.brigadier.ToggleArgumentType.toggle;
import static com.zenith.Globals.MODULE;
import static com.zychen027.PlayerStatePlugin.PLUGIN_CONFIG;

public class QueueWarn3cCommand extends Command {
    @Override
    public CommandUsage commandUsage() {
        return CommandUsage.builder()
            .name("queuewarn3c")
            .category(CommandCategory.MODULE)
            .description("Logs queue position to console: info on every position change, warn when <= threshold")
            .usageLines(
                "queuewarn3c keyword <text...>",
                "queuewarn3c threshold <int>",
                "queuewarn3c toggle <on|off>",
                "queuewarn3c test <text...>"
            )
            .build();
    }

    @Override
    public LiteralArgumentBuilder<CommandContext> register() {
        return command("queuewarn3c").executes(c -> {
                c.getSource().getEmbed()
                    .title("QueueWarn3c")
                    .primaryColor();
                defaultEmbed(c.getSource().getEmbed());
            })
            .then(literal("keyword").then(argument("keyword", greedyString()).executes(c -> {
                PLUGIN_CONFIG.queueWarn.keyword = getString(c, "keyword");
                c.getSource().getEmbed().title("Keyword Set");
                MODULE.get(QueueWarnModule.class).syncEnabledFromConfig();
            })))
            .then(literal("threshold").then(argument("threshold", integer(0)).executes(c -> {
                PLUGIN_CONFIG.queueWarn.threshold = getInteger(c, "threshold");
                c.getSource().getEmbed().title("Threshold Set");
            })))
            .then(literal("toggle").then(argument("toggle", toggle()).executes(c -> {
                PLUGIN_CONFIG.queueWarn.enabled = getToggle(c, "toggle");
                MODULE.get(QueueWarnModule.class).syncEnabledFromConfig();
                c.getSource().getEmbed()
                    .title("QueueWarn3c " + toggleStrCaps(PLUGIN_CONFIG.queueWarn.enabled));
            })))
            .then(literal("test").then(argument("text", greedyString()).executes(c -> {
                final String text = getString(c, "text");
                final Integer n = QueueWarnModule.extractNumberAfterKeyword(text, PLUGIN_CONFIG.queueWarn.keyword);
                c.getSource().getEmbed()
                    .title("QueueWarn3c Test")
                    .primaryColor()
                    .addField("Keyword", PLUGIN_CONFIG.queueWarn.keyword, true)
                    .addField("Extracted Position", n == null ? "none" : String.valueOf(n), true)
                    .addField("Level", n == null ? "-" : (n <= PLUGIN_CONFIG.queueWarn.threshold ? "warn" : "info"), true)
                    .addField("Input", text);
            })));
    }

    @Override
    public void defaultEmbed(final Embed embed) {
        embed
            .primaryColor()
            .addField("Enabled", toggleStr(PLUGIN_CONFIG.queueWarn.enabled), true)
            .addField("Keyword", PLUGIN_CONFIG.queueWarn.keyword, true)
            .addField("Threshold", String.valueOf(PLUGIN_CONFIG.queueWarn.threshold), true)
            .addField("Output", "Console: info on change, warn when <= threshold", true);
    }
}
