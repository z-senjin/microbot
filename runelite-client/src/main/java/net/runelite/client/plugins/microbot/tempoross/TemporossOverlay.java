package net.runelite.client.plugins.microbot.tempoross;

import com.google.inject.Inject;
import lombok.Setter;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.coords.Rs2WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import java.awt.*;
import java.util.List;

import static net.runelite.client.plugins.microbot.tempoross.TemporossScript.workArea;

public class TemporossOverlay extends Overlay {

    private final TemporossPlugin plugin;
    // Add a setter method to feed the list of NPCs
    @Setter
    private static List<NPC> npcList; // Add this field to store the list of NPCs
    @Setter
    private static List<NPC> fishList; // Add this field to store the list of NPCs
    @Setter
    private static List<GameObject> cloudList; // Add this field to store the list of NPCs

    @Inject
    public TemporossOverlay(TemporossPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(100f);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setNaughty();
    }

    @Override
    public Dimension render(Graphics2D graphics) {

        // Render NPC overlays if the list is not null
        if (npcList != null) {
            for (NPC npc : npcList) {
                Rs2WorldPoint npcLocation = new Rs2WorldPoint(npc.getWorldLocation());
                Rs2WorldPoint playerLocation = new Rs2WorldPoint(Microbot.getClient().getLocalPlayer().getWorldLocation());
                renderNpcOverlay(graphics, npc, Color.RED,    npcLocation.distanceToPath(playerLocation.getWorldPoint()) + " tiles");
            }
        }
        if (fishList != null) {
            for (NPC npc : fishList) {
                Rs2WorldPoint npcLocation = new Rs2WorldPoint(npc.getWorldLocation());
                renderNpcOverlay(graphics, npc, Color.RED,    cloudList.isEmpty() ? "Duck was here" :  "In Cloud " + TemporossScript.inCloud(npcLocation.getWorldPoint(),1));
            }
        }

        if (TemporossScript.isInMinigame() && workArea != null) {
            // draw each work area WorldPoint

            renderWorldPoint(graphics, workArea.exitNpc, Color.RED, "Exit NPC");
            renderWorldPoint(graphics, workArea.safePoint, Color.ORANGE, "Safe Point");
            renderWorldPoint(graphics, workArea.bucketPoint, Color.YELLOW, "Bucket Crate");
            renderWorldPoint(graphics, workArea.pumpPoint, Color.DARK_GRAY, "Water Pump");
            renderWorldPoint(graphics, workArea.ropePoint, Color.CYAN, "Rope Crate");
            renderWorldPoint(graphics, workArea.hammerPoint, Color.BLUE, "Hammer Crate");
            renderWorldPoint(graphics, workArea.harpoonPoint, Color.WHITE, "Harpoon Crate");
            renderWorldPoint(graphics, workArea.mastPoint, Color.PINK, "Mast Point");
            renderWorldPoint(graphics, workArea.totemPoint, Color.GREEN, "Totem Point");
            renderWorldPoint(graphics, workArea.rangePoint, Color.MAGENTA, "Range Point");
            renderWorldPoint(graphics, workArea.spiritPoolPoint, Color.ORANGE, "Spirit Pool");
        }

        return null;
    }

    private void renderWorldPoint(Graphics2D graphics, WorldPoint point, Color color, String label) {
        if (point == null) {
            return;
        }

        LocalPoint localPoint = LocalPoint.fromWorld(Microbot.getClient(), point);
        if (localPoint == null) {
            return;
        }

        Polygon poly = Perspective.getCanvasTilePoly(Microbot.getClient(), localPoint);
        if (poly != null) {
            OverlayUtil.renderPolygon(graphics, poly, color);

            // Draw the label
            Point textLocation = Perspective.getCanvasTextLocation(Microbot.getClient(), graphics, localPoint, label, 0);
            if (textLocation != null) {
                OverlayUtil.renderTextLocation(graphics, textLocation, label, Color.WHITE);
            }
        }
    }

    // render game objects
    private void renderGameObject(Graphics2D graphics, GameObject object, Color color, String label) {
        if (object == null) {
            return;
        }


        Shape objectClickbox = object.getCanvasTilePoly();
        if (objectClickbox != null) {
            OverlayUtil.renderPolygon(graphics, objectClickbox, color);

            // Draw the label
            Point textLocation = object.getCanvasTextLocation(graphics, label, 40);
            if (textLocation != null) {
                OverlayUtil.renderTextLocation(graphics, textLocation, label, Color.WHITE);
            }
        }
    }

    // Add this method to render overlays for NPCs
    private void renderNpcOverlay(Graphics2D graphics, NPC npc, Color color, String label) {
        if (npc == null || npc.getConvexHull() == null) {
            return;
        }

        // Draw the NPC outline
        Shape npcHull = npc.getConvexHull();
        OverlayUtil.renderPolygon(graphics, npcHull, color);

        // Draw the label above the NPC
        Point textLocation = npc.getCanvasTextLocation(graphics, label, npc.getLogicalHeight() + 40);
        if (textLocation != null) {
            OverlayUtil.renderTextLocation(graphics, textLocation, label, Color.WHITE);
        }
    }
}
