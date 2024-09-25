package net.runelite.client.plugins.microbot.util.camera;

import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.camera.CameraPlugin;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.Global;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;

import java.awt.*;
import java.awt.event.KeyEvent;

public class Rs2Camera {
    private static final NpcTracker NPC_TRACKER = new NpcTracker();

    public static int angleToTile(Actor t) {
        int angle = (int) Math.toDegrees(Math.atan2(t.getWorldLocation().getY() - Microbot.getClient().getLocalPlayer().getWorldLocation().getY(),
                t.getWorldLocation().getX() - Microbot.getClient().getLocalPlayer().getWorldLocation().getX()));
        return angle >= 0 ? angle : 360 + angle;
    }

    public static int angleToTile(TileObject t) {
        int angle = (int) Math.toDegrees(Math.atan2(t.getWorldLocation().getY() - Microbot.getClient().getLocalPlayer().getWorldLocation().getY(),
                t.getWorldLocation().getX() - Microbot.getClient().getLocalPlayer().getWorldLocation().getX()));
        return angle >= 0 ? angle : 360 + angle;
    }

    public static int angleToTile(LocalPoint localPoint) {
        int angle = (int) Math.toDegrees(Math.atan2(localPoint.getY() - Microbot.getClient().getLocalPlayer().getWorldLocation().getY(),
                localPoint.getX() - Microbot.getClient().getLocalPlayer().getWorldLocation().getX()));
        return angle >= 0 ? angle : 360 + angle;
    }

    public static void turnTo(final Actor actor) {
        int angle = getCharacterAngle(actor);
        setAngle(angle, 40);
    }

    public static void turnTo(final Actor actor, int maxAngle) {
        int angle = getCharacterAngle(actor);
        setAngle(angle, maxAngle);
    }

    public static void turnTo(final TileObject tileObject) {
        int angle = getObjectAngle(tileObject);
        setAngle(angle, 40);
    }

    public static void turnTo(final TileObject tileObject, int maxAngle) {
        int angle = getObjectAngle(tileObject);
        setAngle(angle, maxAngle);
    }

    public static void turnTo(final LocalPoint localPoint) {
        int angle = angleToTile(localPoint);
        setAngle(angle, 40);
    }

    public static void turnTo(final LocalPoint localPoint, int maxAngle) {
        int angle = angleToTile(localPoint);
        setAngle(angle, maxAngle);
    }

    public static int getCharacterAngle(Actor actor) {
        return getTileAngle(actor);
    }

    public static int getObjectAngle(TileObject tileObject) {
        return getTileAngle(tileObject);
    }

    public static int getTileAngle(Actor actor) {
        int a = (angleToTile(actor) - 90) % 360;
        return a < 0 ? a + 360 : a;
    }

    public static int getTileAngle(TileObject tileObject) {
        int a = (angleToTile(tileObject) - 90) % 360;
        return a < 0 ? a + 360 : a;
    }

    /**
     * <h1> Checks if the angle to the target is within the desired max angle </h1>
     * <p>
     * The desired max angle should not go over 80-90 degrees as the target will be out of view
     *
     * @param targetAngle     the angle to the target
     * @param desiredMaxAngle the maximum angle to the target (Should be a positive number)
     *
     * @return true if the angle to the target is within the desired max angle
     */
    public static boolean isAngleGood(int targetAngle, int desiredMaxAngle) {
        return Math.abs(getAngleTo(targetAngle)) <= desiredMaxAngle;
    }

    public static void setAngle(int targetDegrees, int maxAngle) {
        // Default camera speed is 1
        double defaultCameraSpeed = 1f;

        // If the camera plugin is enabled, get the camera speed from the config in case it has been changed
        if (Microbot.isPluginEnabled(CameraPlugin.class)) {
            String configGroup = "zoom";
            String configKey = "cameraSpeed";
            defaultCameraSpeed = RuneLite.getInjector().getInstance(ConfigManager.class).getConfiguration(configGroup, configKey, double.class);
        }
        // Set the camera speed to 3 to make the camera move faster
        Microbot.getClient().setCameraSpeed(3f);

        if (getAngleTo(targetDegrees) > maxAngle) {
            Rs2Keyboard.keyHold(KeyEvent.VK_LEFT);
            Global.sleepUntilTrue(() -> Math.abs(getAngleTo(targetDegrees)) <= maxAngle, 50, 5000);
            Rs2Keyboard.keyRelease(KeyEvent.VK_LEFT);
        } else if (getAngleTo(targetDegrees) < -maxAngle) {
            Rs2Keyboard.keyHold(KeyEvent.VK_RIGHT);
            Global.sleepUntilTrue(() -> Math.abs(getAngleTo(targetDegrees)) <= maxAngle, 50, 5000);
            Rs2Keyboard.keyRelease(KeyEvent.VK_RIGHT);
        }
        Microbot.getClient().setCameraSpeed((float) defaultCameraSpeed);
    }

//    todo: These methods are not working as intended, do more testing with the method above and see if its enough
//    public static void setAngle(int degrees, Actor actor) {
//        if (getAngleTo(degrees) > 5) {
//            Rs2Keyboard.keyHold(KeyEvent.VK_LEFT);
//            Global.awaitExecutionUntil(() -> Rs2Keyboard.keyRelease((char) KeyEvent.VK_LEFT),
//                    () -> Perspective.localToCanvas(Microbot.getClient(), actor.getLocalLocation(), Microbot.getClient().getPlane()) != null, 10);
//        } else if (getAngleTo(degrees) < -5) {
//            Rs2Keyboard.keyHold(KeyEvent.VK_RIGHT);
//            Global.awaitExecutionUntil(() -> Rs2Keyboard.keyRelease((char) KeyEvent.VK_RIGHT),
//                    () -> Perspective.localToCanvas(Microbot.getClient(), actor.getLocalLocation(), Microbot.getClient().getPlane()) != null, 10);
//        }
//    }
//
//    public static void setAngle(int degrees, TileObject tileObject) {
//        if (getAngleTo(degrees) > 5) {
//            Rs2Keyboard.keyHold(KeyEvent.VK_LEFT);
//            Global.awaitExecutionUntil(() -> Rs2Keyboard.keyRelease((char) KeyEvent.VK_LEFT),
//                    () -> Perspective.localToCanvas(Microbot.getClient(), tileObject.getLocalLocation(), Microbot.getClient().getPlane()) != null, 600);
//        } else if (getAngleTo(degrees) < -5) {
//            Rs2Keyboard.keyHold(KeyEvent.VK_RIGHT);
//            Global.awaitExecutionUntil(() -> Rs2Keyboard.keyRelease((char) KeyEvent.VK_RIGHT),
//                    () -> Perspective.localToCanvas(Microbot.getClient(), tileObject.getLocalLocation(), Microbot.getClient().getPlane()) != null, 600);
//        }
//    }
//
//    public static void setAngle(int degrees, LocalPoint localPoint) {
//        if (getAngleTo(degrees) > 5) {
//            Rs2Keyboard.keyHold(KeyEvent.VK_LEFT);
//            Global.awaitExecutionUntil(() -> Rs2Keyboard.keyRelease((char) KeyEvent.VK_LEFT),
//                    () -> Perspective.localToCanvas(Microbot.getClient(), localPoint, Microbot.getClient().getPlane()) != null, 600);
//        } else if (getAngleTo(degrees) < -5) {
//            Rs2Keyboard.keyHold(KeyEvent.VK_RIGHT);
//            Global.awaitExecutionUntil(() -> Rs2Keyboard.keyRelease((char) KeyEvent.VK_RIGHT),
//                    () -> Perspective.localToCanvas(Microbot.getClient(), localPoint, Microbot.getClient().getPlane()) != null, 600);
//        }
//    }

    public static void adjustPitch(float percentage) {
        float currentPitchPercentage = cameraPitchPercentage();

        if (currentPitchPercentage < percentage) {
            Rs2Keyboard.keyHold(KeyEvent.VK_UP);
            Global.sleepUntilTrue(() -> cameraPitchPercentage() >= percentage, 50, 5000);
            Rs2Keyboard.keyRelease(KeyEvent.VK_UP);
        } else {
            Rs2Keyboard.keyHold(KeyEvent.VK_DOWN);
            Global.sleepUntilTrue(() -> cameraPitchPercentage() <= percentage, 50, 5000);
            Rs2Keyboard.keyRelease(KeyEvent.VK_DOWN);
        }
    }

    public static int getPitch() {
        return Microbot.getClient().getCameraPitch();
    }

    // set camera pitch
    public static void setPitch(int pitch) {
        int minPitch = 128;
        int maxPitch = 383;
        // clamp pitch to avoid out of bounds
        pitch = Math.max(minPitch, Math.min(maxPitch, pitch));
        Microbot.getClient().setCameraPitchTarget(pitch);
    }

    public static float cameraPitchPercentage() {
        int minPitch = 128;
        int maxPitch = 383;
        int currentPitch = Microbot.getClient().getCameraPitch();

        int adjustedPitch = currentPitch - minPitch;
        int adjustedMaxPitch = maxPitch - minPitch;

        return (float) adjustedPitch / (float) adjustedMaxPitch;
    }

    public static int getAngleTo(int degrees) {
        int ca = getAngle();
        if (ca < degrees) {
            ca += 360;
        }
        int da = ca - degrees;
        if (da > 180) {
            da -= 360;
        }
        return da;
    }

    public static int getAngle() {
        // the client uses fixed point radians 0 - 2^14
        // degrees = yaw * 360 / 2^14 = yaw / 45.5111...
        // This leaves it on a scale of 45 versus a scale of 360 so we multiply it by 8 to fix that.
        return (int) Math.abs(Microbot.getClient().getCameraYaw() / 45.51 * 8);
    }

    /**
     * Calculates the CameraYaw based on the given NPC or object angle.
     *
     * @param npcAngle the angle of the NPC or object relative to the player (0-359 degrees)
     *
     * @return the calculated CameraYaw (0-2047)
     */
    public static int calculateCameraYaw(int npcAngle) {
        // Convert the NPC angle to CameraYaw using the derived formula
        return (1536 + (int) Math.round(npcAngle * (2048.0 / 360.0))) % 2048;
    }

    /**
     * Track the NPC with the camera
     *
     * @param npcId the ID of the NPC to track
     */
    public static void trackNpc(int npcId) {
        NPC_TRACKER.startTracking(npcId);
    }

    /**
     * Stop tracking the NPC with the camera
     */
    public static void stopTrackingNpc() {
        NPC_TRACKER.stopTracking();
    }

    /**
     * Checks if a NPC is being tracked
     *
     * @return true if a NPC is being tracked, false otherwise
     */
    public static boolean isTrackingNpc() {
        return NPC_TRACKER.isTracking();
    }

    public static boolean isTileOnScreen(TileObject tileObject) {
        int viewportHeight = Microbot.getClient().getViewportHeight();
        int viewportWidth = Microbot.getClient().getViewportWidth();


        Polygon poly = Perspective.getCanvasTilePoly(Microbot.getClient(), tileObject.getLocalLocation());

        if (poly == null) return false;

        return poly.getBounds2D().getX() <= viewportWidth && poly.getBounds2D().getY() <= viewportHeight;
    }

    public static boolean isTileOnScreen(LocalPoint localPoint) {
        int viewportHeight = Microbot.getClient().getViewportHeight();
        int viewportWidth = Microbot.getClient().getViewportWidth();


        Polygon poly = Perspective.getCanvasTilePoly(Microbot.getClient(), localPoint);

        if (poly == null) return false;

        return poly.getBounds2D().getX() <= viewportWidth && poly.getBounds2D().getY() <= viewportHeight;
    }

    // get the camera zoom
    public static int getZoom() {
        return Microbot.getClient().getVarcIntValue(VarClientInt.CAMERA_ZOOM_RESIZABLE_VIEWPORT);
    }

    public static void setZoom(int zoom) {
        Microbot.getClientThread().invokeLater(() -> {
            Microbot.getClient().runScript(ScriptID.CAMERA_DO_ZOOM, zoom, zoom);
        });
    }
}

