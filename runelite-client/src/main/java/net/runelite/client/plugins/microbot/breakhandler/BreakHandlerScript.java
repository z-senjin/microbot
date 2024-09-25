package net.runelite.client.plugins.microbot.breakhandler;

import lombok.Getter;
import lombok.Setter;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.math.Random;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.security.Login;
import net.runelite.client.ui.ClientUI;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class BreakHandlerScript extends Script {
    public static String version = "1.0.0";

    public static int breakIn = -1;
    public static int breakDuration = -1;

    public static int totalBreaks = 0;

    public static Duration duration;
    public static Duration breakInDuration;
    @Setter
    @Getter
    public static boolean lockState = false;
    private String title = "";

    public static boolean isBreakActive() {
        return breakDuration > 0;
    }

    public static String formatDuration(Duration duration, String header) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;
        return String.format(header + " %02d:%02d:%02d", hours, minutes, seconds);
    }

    public boolean run(BreakHandlerConfig config) {

        Microbot.enableAutoRunOn = false;
        title = ClientUI.getFrame().getTitle();
        breakIn = Random.random(config.timeUntilBreakStart() * 60, config.timeUntilBreakEnd() * 60);
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {

                if (config.playSchedule().isOutsideSchedule() && config.usePlaySchedule() && !isLockState()) {
                    Duration untilNextSchedule = config.playSchedule().timeUntilNextSchedule();
                    breakIn = -1;
                    breakDuration = (int) untilNextSchedule.toSeconds();
                }

                if (breakIn > 0 && breakDuration <= 0) {
                    breakIn--;
                    duration = Duration.between(LocalDateTime.now(), LocalDateTime.now().plusSeconds(breakIn));
                    breakInDuration = duration;
                }

                if (breakDuration > 0) {
                    breakDuration--;
                    duration = Duration.between(LocalDateTime.now(), LocalDateTime.now().plusSeconds(breakDuration));
                    long hours = BreakHandlerScript.duration.toHours();
                    long minutes = BreakHandlerScript.duration.toMinutes() % 60;
                    long seconds = BreakHandlerScript.duration.getSeconds() % 60;
                    if (Rs2AntibanSettings.takeMicroBreaks && Rs2AntibanSettings.microBreakActive) {
                        ClientUI.getFrame().setTitle(String.format("Micro break duration: %02d:%02d:%02d", hours, minutes, seconds));
                    } else if (config.playSchedule().isOutsideSchedule() && config.usePlaySchedule()) {
                        ClientUI.getFrame().setTitle(String.format("Next schedule in: %02d:%02d:%02d", hours, minutes, seconds));
                    } else {
                        ClientUI.getFrame().setTitle(String.format("Break duration: %02d:%02d:%02d", hours, minutes, seconds));
                    }
                }

                if (breakDuration <= 0 && Microbot.pauseAllScripts) {
                    if (Rs2AntibanSettings.universalAntiban && Rs2AntibanSettings.actionCooldownActive)
                        return;
                    Microbot.pauseAllScripts = false;
                    if (breakIn <= 0)
                        breakIn = Random.random(config.timeUntilBreakStart() * 60, config.timeUntilBreakEnd() * 60);

                    new Login();
                    totalBreaks++;
                    ClientUI.getFrame().setTitle(title);
                    if (Rs2AntibanSettings.takeMicroBreaks) {
                        Rs2AntibanSettings.microBreakActive = false;
                    }
                    return;
                }

                if ((breakIn <= 0 && !Microbot.pauseAllScripts && !isLockState()) || (Rs2AntibanSettings.microBreakActive && !Microbot.pauseAllScripts && !isLockState())) {
                    Microbot.pauseAllScripts = true;

                    if (Rs2AntibanSettings.microBreakActive)
                        return;
                    if (config.playSchedule().isOutsideSchedule() && config.usePlaySchedule()) {
                        Rs2Player.logout();
                        return;
                    }


                    breakDuration = Random.random(config.breakDurationStart() * 60, config.breakDurationEnd() * 60);

                    if (config.logoutAfterBreak()) {
                        Rs2Player.logout();
                    }
                }

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        breakIn = 0;
        breakDuration = 0;
        ClientUI.getFrame().setTitle(title);
        super.shutdown();
    }

    public void reset() {
        breakIn = 0;
        breakDuration = 0;
        ClientUI.getFrame().setTitle(title);
    }
}
