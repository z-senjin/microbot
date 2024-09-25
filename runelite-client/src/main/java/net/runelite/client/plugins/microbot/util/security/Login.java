package net.runelite.client.plugins.microbot.util.security;

import net.runelite.client.config.ConfigProfile;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.util.WorldUtil;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldRegion;
import net.runelite.http.api.worlds.WorldResult;
import net.runelite.http.api.worlds.WorldType;

import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.util.Global.sleep;

public class Login {

    public static ConfigProfile activeProfile = null;
    private static final int MAX_PLAYER_COUNT = 1950;

    public Login() {
        this(getRandomWorld(activeProfile.isMember()));
    }

    public Login(int world) {
        this(activeProfile.getName(), activeProfile.getPassword(), world);
    }

    public Login(String username, String password) {
        this(username, password, 360);
    }

    public Login(String username, String password, int world) {
        if(Microbot.isLoggedIn())
            return;
        if (Microbot.getClient().getLoginIndex() == 3 || Microbot.getClient().getLoginIndex() == 24) { // you were disconnected from the server.
            int loginScreenWidth = 804;
            int startingWidth = (Microbot.getClient().getCanvasWidth() / 2) - (loginScreenWidth / 2);
            Microbot.getMouse().click(365 + startingWidth, 308); //clicks a button "OK" when you've been disconnected
            sleep(600);
        }
        Rs2Keyboard.keyPress(KeyEvent.VK_ENTER);
        sleep(600);
        try {
            setWorld(world);
        } catch (Exception e) {
            System.out.println("Changing world failed");
        }
        Microbot.getClient().setUsername(username);
        try {
            Microbot.getClient().setPassword(Encryption.decrypt(password));
        } catch (Exception e) {
            System.out.println("no password has been set in the profile");
        }
        sleep(300);
        Rs2Keyboard.keyPress(KeyEvent.VK_ENTER);
        sleep(300);
        Rs2Keyboard.keyPress(KeyEvent.VK_ENTER);
        if (Microbot.getClient().getLoginIndex() == 10) {
            int loginScreenWidth = 804;
            int startingWidth = (Microbot.getClient().getCanvasWidth() / 2) - (loginScreenWidth / 2);
            Microbot.getMouse().click(365 + startingWidth, 250); //clicks a button "OK" when you've been disconnected
        } else if (Microbot.getClient().getLoginIndex() == 9) {
            int loginScreenWidth = 804;
            int startingWidth = (Microbot.getClient().getCanvasWidth() / 2) - (loginScreenWidth / 2);
            Microbot.getMouse().click(365 + startingWidth, 300); //clicks a button "OK" when you've been disconnected
        }
    }

    public static int getRandomWorld(boolean isMembers, WorldRegion region) {
        WorldResult worldResult = Microbot.getWorldService().getWorlds();

        List<World> worlds;
        if (worldResult != null) {
            worlds = worldResult.getWorlds();
            Random r = new Random();
            List<World> filteredWorlds = worlds
                    .stream()
                    .filter(x ->
                            (!x.getTypes().contains(WorldType.PVP) &&
                                    !x.getTypes().contains(WorldType.HIGH_RISK) &&
                                    !x.getTypes().contains(WorldType.BOUNTY) &&
                                    !x.getTypes().contains(WorldType.SKILL_TOTAL) &&
                                    !x.getTypes().contains(WorldType.LAST_MAN_STANDING) &&
                                    !x.getTypes().contains(WorldType.QUEST_SPEEDRUNNING) &&
                                    !x.getTypes().contains(WorldType.BETA_WORLD) &&
                                    !x.getTypes().contains(WorldType.DEADMAN) &&
                                    !x.getTypes().contains(WorldType.PVP_ARENA) &&
                                    !x.getTypes().contains(WorldType.TOURNAMENT) &&
                                    !x.getTypes().contains(WorldType.FRESH_START_WORLD)) &&
                                    x.getPlayers() < MAX_PLAYER_COUNT &&
                                    x.getPlayers() >= 0)
                    .collect(Collectors.toList());

            if (!isMembers) {
                filteredWorlds = filteredWorlds
                        .stream()
                        .filter(x -> !x.getTypes().contains(WorldType.MEMBERS)).collect(Collectors.toList());
            } else {
                filteredWorlds = filteredWorlds
                        .stream()
                        .filter(x -> x.getTypes().contains(WorldType.MEMBERS)).collect(Collectors.toList());
            }

            if (region != null)
                filteredWorlds = filteredWorlds
                        .stream()
                        .filter(x -> x.getRegion() == region).collect(Collectors.toList());

            World world =
                    filteredWorlds.stream()
                            .skip(r.nextInt(filteredWorlds.size()))
                            .findFirst()
                            .orElse(null);

            if (world != null) {
                return world.getId();
            }
        }

        return isMembers ? 360 : 383;
    }

    public static int getRandomWorld(boolean isMembers) {
        return getRandomWorld(isMembers, null);
    }

    public void setWorld(int worldNumber) {
        try {
            net.runelite.http.api.worlds.World world = Microbot.getWorldService().getWorlds().findWorld(worldNumber);
            final net.runelite.api.World rsWorld = Microbot.getClient().createWorld();
            rsWorld.setActivity(world.getActivity());
            rsWorld.setAddress(world.getAddress());
            rsWorld.setId(world.getId());
            rsWorld.setPlayerCount(world.getPlayers());
            rsWorld.setLocation(world.getLocation());
            rsWorld.setTypes(WorldUtil.toWorldTypes(world.getTypes()));
            Microbot.getClient().changeWorld(rsWorld);
        } catch (Exception ex) {
            System.out.println("Failed to find world");
        }
    }
}
