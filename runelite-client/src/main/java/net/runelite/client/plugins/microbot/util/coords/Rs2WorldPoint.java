package net.runelite.client.plugins.microbot.util.coords;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.tile.Rs2Tile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Rs2WorldPoint {
    @Setter
    private WorldPoint worldPoint;

    // Constructor accepting coordinates
    public Rs2WorldPoint(int x, int y, int plane) {
        this.worldPoint = new WorldPoint(x, y, plane);
    }

    // Constructor accepting a WorldPoint instance
    public Rs2WorldPoint(WorldPoint worldPoint) {
        this.worldPoint = worldPoint;

    }

    // Getter methods
    public int getX() {
        return worldPoint.getX();
    }

    public int getY() {
        return worldPoint.getY();
    }

    public int getPlane() {
        return worldPoint.getPlane();
    }

    public WorldPoint getWorldPoint() {
        return worldPoint;
    }

    public List<WorldPoint> pathTo(WorldPoint other)
    {
        Client client = Microbot.getClient();
        if (getPlane() != other.getPlane())
        {
            return null;
        }

        LocalPoint sourceLp = LocalPoint.fromWorld(client.getTopLevelWorldView(), getX(), getY());
        LocalPoint targetLp = LocalPoint.fromWorld(client.getTopLevelWorldView(), other.getX(), other.getY());
        if (sourceLp == null || targetLp == null)
        {
            return null;
        }

        int thisX = sourceLp.getSceneX();
        int thisY = sourceLp.getSceneY();
        int otherX = targetLp.getSceneX();
        int otherY = targetLp.getSceneY();

        Tile[][][] tiles = client.getTopLevelWorldView().getScene().getTiles();
        Tile sourceTile = tiles[getPlane()][thisX][thisY];

        Tile targetTile = tiles[getPlane()][otherX][otherY];
        List<Tile> checkpointTiles = Rs2Tile.pathTo(sourceTile,targetTile);
        if (checkpointTiles == null)
        {
            return null;
        }
        List<WorldPoint> checkpointWPs = new ArrayList<>();
        for (Tile checkpointTile : checkpointTiles)
        {
            if (checkpointTile == null)
            {
                break;
            }
            checkpointWPs.add(checkpointTile.getWorldLocation());
        }
        return checkpointWPs;
    }

    public int distanceToPath(WorldPoint other)
    {
        if(other == null)
        {
            return Integer.MAX_VALUE;
        }
        List<WorldPoint> checkpointWPs = this.pathTo(other);
        if (checkpointWPs == null)
        {
            // No path found
            return Integer.MAX_VALUE;
        }

        WorldPoint destinationPoint = checkpointWPs.get(checkpointWPs.size() - 1);
        if (other.getX() != destinationPoint.getX() || other.getY() != destinationPoint.getY())
        {
            // Path found but not to the requested tile
            return Integer.MAX_VALUE;
        }
        WorldPoint Point1 = getWorldPoint();
        int distance = 0;
        for (WorldPoint Point2 : checkpointWPs)
        {
            distance += Point1.distanceTo2D(Point2);
            Point1 = Point2;
        }
        return distance;
    }

    // Override equals, hashCode, and toString if necessary
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Rs2WorldPoint) {
            Rs2WorldPoint other = (Rs2WorldPoint) obj;
            return worldPoint.equals(other.worldPoint);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return worldPoint.hashCode();
    }

    @Override
    public String toString() {
        return worldPoint.toString();
    }
}

