package net.runelite.client.plugins.microbot.frosty.bloods;

import com.google.inject.Provides;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.breakhandler.BreakHandlerPlugin;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;
import java.time.Instant;

@PluginDescriptor(
        name = PluginDescriptor.Frosty + "Bloods",
        description = "A plugin to automate Blood Rune crafting",
        tags = {"blood", "ruc", "rune", "Frosty"},
        enabledByDefault = false
)

public class BloodsPlugin extends Plugin {
    @Inject
    private BloodsConfig config;
    @Inject
    private Client client;
    @Provides
    BloodsConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(BloodsConfig.class);
    }
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private BloodsOverlay bloodsOverlay;
    @Inject
    private BloodsScript bloodsScript;
    public static String version = "1.0.0";
    @Getter
    public Instant startTime;
    @Getter
    private int lastBloodRuneCount = 0;
    @Getter
    private int totalXpGained = 0;
    @Getter
    private int startXp = 0;

    @Override
    protected void startUp() throws AWTException {
        startTime = Instant.now();
        if (overlayManager != null) {
            overlayManager.add(bloodsOverlay);
        }
        startXp = client.getSkillExperience(Skill.RUNECRAFT);
        bloodsScript.run();
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(bloodsOverlay);
        bloodsScript.shutdown();
    }

    public void updateXpGained() {
        int currentXp = client.getSkillExperience(Skill.RUNECRAFT);
        totalXpGained = currentXp - startXp;
    }

    public boolean isBreakHandlerEnabled() {
        return Microbot.isPluginEnabled(BreakHandlerPlugin.class);
    }
}
