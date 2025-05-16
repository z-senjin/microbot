package net.runelite.client.plugins.microbot.mntn.AccountBuilder;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
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


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(accountBuilderOverlay);
        }
        accountBuilderScript.run(config);
    }

    protected void shutDown() {
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
