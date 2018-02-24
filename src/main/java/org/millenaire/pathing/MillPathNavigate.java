package org.millenaire.pathing;

import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.millenaire.entities.EntityMillVillager;
import org.millenaire.pathing.atomicstryker.AS_PathEntity;

import java.util.List;

public class MillPathNavigate extends PathNavigateGround {

    private static final Object[] pathingLock = new Object[]{};

    public EntityMillVillager villager;
    private int pathRange = 256;
    private boolean needsRegenConnections = true;

    public MillPathNavigate(EntityLiving entitylivingIn, World worldIn) {
        super(entitylivingIn, worldIn);

        villager = (EntityMillVillager) theEntity;

        this.setBreakDoors(true);
        this.setAvoidsWater(true);
        this.setAvoidSun(false);
    }

    public void invalidateConnections() {
        needsRegenConnections = true;
    }

    @Override
    public PathEntity getPathToPos(BlockPos pos) {
        if (!villager.useNewPathingAtThisPoint) return super.getPathToPos(pos);

        //System.out.println("Using A* Pathing to get to " + pos + "");
        try {
            synchronized (pathingLock) {
                if (needsRegenConnections)
                    villager.pathing.createConnectionsTable(villager.village.geography);

                needsRegenConnections = false;

                AStarPathing.PathingWorker workerForPath = villager.pathing.createWorkerForPath(villager, villager.getPosition().getX(), villager.getPosition().getZ(), pos.getX(), pos.getZ());

                List<PathPoint> pathViaNodes = workerForPath.getPathViaNodes();

                if (pathViaNodes == null) {
                    //System.out.println("Null path returned");
                    return null;
                }

                PathPoint[] points = pathViaNodes.toArray(new PathPoint[pathViaNodes.size()]);

                //System.out.println("Path found");
                return new AS_PathEntity(points);
            }
        } catch (AStarPathing.PathingException e) {
            if (e.errorCode != AStarPathing.PathingException.UNREACHABLE_START) {
                System.out.println(e.getMessage());
            }
            return null;
        } catch (StackOverflowError ignored) {
            //Genuinely, the stack overflows at org.millenaire.pathing.AStarPathing.buildPointsNode(AStarPathing.java:644)
            //It happens, apparently when pathfinding is not possible in a very specific way.
            //Meh. Suppress it.
            System.out.println("(Suppressed a multi-hundred line StackOverflow. Pathfinding was impossible)");
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public float getPathSearchRange() {
        return pathRange;
    }

    public void setSearchRange(int range) {
        pathRange = range;
    }

    @Override
    protected PathFinder getPathFinder() {
        this.nodeProcessor = new MillWalkNodeProcessor();
        this.nodeProcessor.setEnterDoors(true);
        this.nodeProcessor.setBreakDoors(true);
        this.nodeProcessor.setAvoidsWater(true);
        return new PathFinder(this.nodeProcessor);
    }
}
