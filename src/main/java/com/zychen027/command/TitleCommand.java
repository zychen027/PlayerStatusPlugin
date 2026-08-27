package com.zychen027.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.zenith.command.api.Command;
import com.zenith.command.api.CommandCategory;
import com.zenith.command.api.CommandContext;
import com.zenith.command.api.CommandUsage;
import com.zychen027.BuildConstants;

import java.time.Instant;

import static com.zychen027.PlayerState.fmtTicks;
import static com.zychen027.module.TitleTrackerModule.getActionBarCount;
import static com.zychen027.module.TitleTrackerModule.getAnimationCount;
import static com.zychen027.module.TitleTrackerModule.getClearCount;
import static com.zychen027.module.TitleTrackerModule.getLastActionBar;
import static com.zychen027.module.TitleTrackerModule.getLastPacketTimeMs;
import static com.zychen027.module.TitleTrackerModule.getLastSubtitle;
import static com.zychen027.module.TitleTrackerModule.getLastTitle;
import static com.zychen027.module.TitleTrackerModule.getSubtitleCount;
import static com.zychen027.module.TitleTrackerModule.getTitleCount;
import static com.zychen027.module.TitleTrackerModule.hasAnyData;
import static com.zychen027.module.TitleTrackerModule.isTitleOnScreen;
import static com.zychen027.module.TitleTrackerModule.remainingTicks;

public class TitleCommand extends Command {
    @Override
    public CommandUsage commandUsage() {
        return CommandUsage.builder()
            .name("title")
            .category(CommandCategory.INFO)
            .description("Outputs the title/subtitle/action bar currently displayed on the bot's screen")
            .usageLines("title", "title status")
            .build();
    }

    @Override
    public LiteralArgumentBuilder<CommandContext> register() {
        return command("title")
            .executes(c -> { return showCurrent(c); })
            .then(literal("status").executes(c -> { return showStatus(c); }));
    }

    private int showCurrent(final com.mojang.brigadier.context.CommandContext<CommandContext> c) {
        final String title = getLastTitle();
        final String subtitle = getLastSubtitle();
        final String actionBar = getLastActionBar();

        if (title == null && subtitle == null && actionBar == null) {
            c.getSource().getEmbed()
                .title("Title")
                .primaryColor()
                .addField("Screen", hasAnyData()
                    ? "Empty (last screen content was cleared / expired)"
                    : "Empty (no title-family packet received yet)")
                .addField("Hint", "Run `title status` to check the tracker diagnostics.");
            return OK;
        }

        final var embed = c.getSource().getEmbed().title("Title").primaryColor();
        if (title != null) {
            embed.addField(isTitleOnScreen() ? "Title" : "Title (expired)", title);
            embed.addField("Time Left", isTitleOnScreen() ? fmtTicks(remainingTicks()) : "Expired", true);
        }
        if (subtitle != null) {
            embed.addField("Subtitle", subtitle);
        }
        if (actionBar != null) {
            embed.addField("Action Bar", actionBar);
        }
        return OK;
    }

    private int showStatus(final com.mojang.brigadier.context.CommandContext<CommandContext> c) {
        c.getSource().getEmbed()
            .title("Title Tracker Status")
            .primaryColor()
            .addField("Plugin Version", BuildConstants.VERSION, true)
            .addField("Packets Received",
                "title: " + getTitleCount() + "\n"
                + "subtitle: " + getSubtitleCount() + "\n"
                + "actionbar: " + getActionBarCount() + "\n"
                + "animation: " + getAnimationCount() + "\n"
                + "clear: " + getClearCount())
            .addField("Last Packet", getLastPacketTimeMs() > 0
                ? Instant.ofEpochMilli(getLastPacketTimeMs()).toString()
                : "none")
            .addField("Diagnosis", hasAnyData()
                ? "Tracker is receiving title packets - working normally."
                : "Tracker has NOT received any title-family packet. Either the server sent none, or the module is not active - check the proxy log for '[TitleTracker]' lines.");
        return OK;
    }
}
