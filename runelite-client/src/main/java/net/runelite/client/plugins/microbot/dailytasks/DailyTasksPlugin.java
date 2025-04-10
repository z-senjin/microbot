package net.runelite.client.plugins.microbot.dailytasks;

import com.google.inject.Inject;
import com.google.inject.Provides;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.pluginscheduler.event.ScheduledStopEvent;
import net.runelite.client.ui.overlay.OverlayManager;

import static net.runelite.client.plugins.PluginDescriptor.Mocrosoft;

@PluginDescriptor(
        name = Mocrosoft + "Daily Tasks",
        description = "Microbot daily tasks plugin",
        tags = {"misc"},
        enabledByDefault = false,
        canBeScheduled = true
)
public class DailyTasksPlugin extends Plugin {
    static final String CONFIG_GROUP = "dailytasks";
    static String currentState = "";

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private DailyTasksOverlay dailyTasksOverlay;

    @Inject
    private DailyTasksScript dailyTasksScript;

    @Provides
    DailyTasksConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(DailyTasksConfig.class);
    }


    @Override
    protected void startUp() throws Exception {
        overlayManager.add(dailyTasksOverlay);
        dailyTasksScript.run();
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(dailyTasksOverlay);
        dailyTasksScript.shutdown();
    }

    @Subscribe
    public void onScheduledStopEvent(ScheduledStopEvent event) {
        if (event.getPlugin() == this) {
            Microbot.stopPlugin(this);
        }
    }
}
