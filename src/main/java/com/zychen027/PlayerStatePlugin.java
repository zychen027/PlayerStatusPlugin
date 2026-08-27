package com.zychen027;

import com.zenith.plugin.api.Plugin;
import com.zenith.plugin.api.PluginAPI;
import com.zenith.plugin.api.ZenithProxyPlugin;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import com.zychen027.command.PlayerStatusCommand;
import com.zychen027.command.PosCommand;
import com.zychen027.command.PerspectiveCommand;
import com.zychen027.command.QueueWarn3cCommand;
import com.zychen027.command.TitleCommand;
import com.zychen027.module.QueueWarnModule;
import com.zychen027.module.TitleTrackerModule;

@Plugin(
    id = BuildConstants.PLUGIN_ID,
    version = BuildConstants.VERSION,
    description = "Player state query commands (pos, perspective, playerStatus, title) + console queue warning",
    url = "https://github.com/zychen027/PlayerStatusPlugin",
    authors = {"zt3927"},
    mcVersions = {BuildConstants.MC_VERSION}
)
public class PlayerStatePlugin implements ZenithProxyPlugin {
    public static ComponentLogger LOG;
    public static PluginConfig PLUGIN_CONFIG;

    @Override
    public void onLoad(PluginAPI pluginAPI) {
        LOG = pluginAPI.getLogger();
        LOG.info("PlayerState Plugin loading...");
        // initialize config before modules/commands read it
        PLUGIN_CONFIG = pluginAPI.registerConfig(BuildConstants.PLUGIN_ID, PluginConfig.class);
        // Tracks on-screen title/subtitle/action bar state for the `title` command
        pluginAPI.registerModule(new TitleTrackerModule());
        // Console warning when a number after a keyword in the title drops <= threshold
        pluginAPI.registerModule(new QueueWarnModule());
        pluginAPI.registerCommand(new PosCommand());
        pluginAPI.registerCommand(new PerspectiveCommand());
        pluginAPI.registerCommand(new PlayerStatusCommand());
        pluginAPI.registerCommand(new TitleCommand());
        pluginAPI.registerCommand(new QueueWarn3cCommand());
        LOG.info("PlayerState Plugin loaded! Commands: pos / perspective / playerStatus / title / queuewarn3c");
    }
}
