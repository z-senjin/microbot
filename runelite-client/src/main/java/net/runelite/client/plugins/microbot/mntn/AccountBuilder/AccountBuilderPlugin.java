package net.runelite.client.plugins.microbot.mntn.AccountBuilder;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.mntn.AccountBuilder.ActivityHandler.ActivityHandler;
import net.runelite.client.plugins.microbot.mntn.AccountBuilder.PluginHandler.PluginHandler;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Mntn + "Account Builder",
        description = "Account builder by Mntn.",
        tags = {"account", "builder", "aio"},
        enabledByDefault = false
)
@Slf4j
public class AccountBuilderPlugin extends Plugin {
    @Inject
    private AccountBuilderConfig config;
    @Provides
    AccountBuilderConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(AccountBuilderConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private AccountBuilderOverlay accountBuilderOverlay;

    @Inject
    AccountBuilderScript accountBuilderScript;

    public ActivityHandler activityHandler = null;
    public PluginHandler pluginHandler = null;



    @Override
    protected void startUp() throws AWTException {
        System.out.println("ACCOUNT BUILDER PLUGIN STARTUP");
        if (overlayManager != null) {
            overlayManager.add(accountBuilderOverlay);
        }
            activityHandler = new ActivityHandler();
            pluginHandler = new PluginHandler();
        accountBuilderScript.run(config);
    }

    protected void shutDown() {
        System.out.println("ACCOUNT BUILDER SHUT DOWN!!!");
        activityHandler = null;
        pluginHandler = null;
        accountBuilderScript.shutdown();
        overlayManager.remove(accountBuilderOverlay);
    }


    int ticks = 10;
    @Subscribe
    public void onGameTick(GameTick tick)
    {
        //System.out.println(getName().chars().mapToObj(i -> (char)(i + 3)).map(String::valueOf).collect(Collectors.joining()));

        if (ticks > 0) {
            ticks--;
        } else {
            ticks = 10;
        }

    }

}
