package org.millenaire.pathing;

import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.BlockPos;
import org.millenaire.VillageGeography;
import org.millenaire.entities.EntityMillVillager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AStarPathing {

    static public class CachedPath {

        Point2D[] points = null;
        long age = 0;

        CachedPath() {
            age = System.currentTimeMillis();
        }

        CachedPath(final CachedPath cp, final int from) {
            points = new Point2D[cp.points.length - from];

            int i = 0;
            for (final Point2D p : cp.points) {
                if (i >= from) {
                    points[i - from] = p;
                }
                i++;
            }

            age = System.currentTimeMillis();
        }

        CachedPath(final List<Point2D> v) {

            final List<Point2D> v2 = new ArrayList<>();

            for (int i = 0; i < v.size(); i++) {
                final Point2D p = v.get(i);

                // Now filling up intermediary points between the nodes:
                if (i > 0) {
                    final Point2D prevp = v2.get(v2.size() - 1);

                    List<Point2D> v3;
                    try {
                        v3 = fillPoints(prevp, p);
                        for (final Point2D fp : v3) {
                            v2.add(fp);
                        }
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }

                }

                v2.add(p);

            }

            points = new Point2D[v2.size()];

            int i = 0;
            for (final Point2D p : v2) {
                points[i] = p;
                i++;
            }

            age = System.currentTimeMillis();
        }

        CachedPath(final List<Point2D> v, final CachedPath cp) {
            points = new Point2D[v.size() + cp.points.length - 1];

            int i = 0;
            for (final Point2D p : v) {
                points[i] = p;
                i++;
            }

            boolean first = true;
            for (final Point2D p : cp.points) {
                if (first) {// already last node of v
                    first = false;
                } else {
                    points[i] = p;
                    i++;
                }
            }

            age = System.currentTimeMillis();
        }

        private List<Point2D> fillPoints(final Point2D p1, final Point2D p2) throws Exception {

            final List<Point2D> v = new ArrayList<>();

            final int xdist = p2.x - p1.x;
            final int zdist = p2.z - p1.z;

            if (xdist == 0 && zdist == 0) {
                return v;
            }

            int xsign = 1;
            int zsign = 1;

            if (xdist < 0) {
                xsign = -1;
            }
            if (zdist < 0) {
                zsign = -1;
            }

            int x = p1.x;
            int z = p1.z;

            int xdone = 0;
            int zdone = 0;

            // Log.debug(Log.Connections, "canSee: "+p1+" to "+p2);

            // Log.debug(Log.Connections, "To travel: "+xdist+", "+zdist);

            while (x != p2.x || z != p2.z) {
                int nx, nz;
                if (zdone != zdist && (xdist == 0 || zdone * 1.0f / zdist < xdone * 1.0f / xdist)) {
                    nz = z + zsign;
                    nx = x;
                    zdone += zsign;
                } else if (xdone != xdist) {
                    nx = x + xsign;
                    nz = z;
                    xdone += xsign;
                } else {
                    throw new IllegalStateException("Error in fillPoints: from " + p1 + " to " + p2 + " did " + xdone + "/" + zdone + " and could find nothing else to do.");
                }

                x = nx;
                z = nz;

                if (x != p2.x || z != p2.z) {
                    v.add(new Point2D(nx, nz));
                }
            }

            return v;
        }

        public String fullString() {
            String s = "";

            for (final Point2D p : points) {
                s += p + " ";
            }

            return s;
        }

        Point2D getEnd() {
            return points[points.length - 1];
        }

        PathKey getKey() {
            return new PathKey(points[0], points[points.length - 1]);
        }

        public Point2D getStart() {
            return points[0];
        }

        @Override
        public String toString() {
            return points.length + " - " + getStart() + " - " + getEnd();
        }

    }

    static protected class Node {
        Point2D pos;
        List<Node> neighbours;
        HashMap<Node, Integer> costs;
        Node from;
        int id, fromDist, toDist, cornerSide, region = 0;
        boolean temp = false;

        Node(final Point2D p, final int pid, final boolean ptemp) {
            pos = p;
            id = pid;
            cornerSide = 0;
            temp = ptemp;
            neighbours = new ArrayList<>();
            costs = new HashMap<>();
        }

        Node(final Point2D p, final int pid, final int cornerSide, final boolean ptemp) {
            pos = p;
            id = pid;
            temp = ptemp;
            this.cornerSide = cornerSide;
            neighbours = new ArrayList<>();
            costs = new HashMap<>();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj.getClass() != this.getClass()) {
                return false;
            }

            final Node n = (Node) obj;

            return n.hashCode() == hashCode();

        }

        @Override
        public int hashCode() {
            return pos.x + (pos.z << 16);
        }

        @Override
        public String toString() {
            return "Node " + id + ": " + pos + " group: " + region + " neighbours: " + neighbours.size() + "(fromDist: " + fromDist + ", toDist: " + toDist + ")";
        }
    }

    public static class PathingException extends Exception {

        private static final long serialVersionUID = 6212915693102946545L;

        public static int UNREACHABLE_START = 0;
        public static int INVALID_GOAL = 1;

        public int errorCode;

        PathingException(final String message, final int code) {
            super(message);

            errorCode = code;
        }

    }

    public class PathingWorker extends Thread {

        private static final int NODE_WARNING_LEVEL = 100;
        private static final int MAX_NODE_VISIT = 1500;
        EntityMillVillager villager;
        int pStartX, pStartZ, pDestX, pDestZ;

        private PathingWorker(final EntityMillVillager villager, final int pStartX, final int pStartZ, final int pDestX, final int pDestZ) {

            this.villager = villager;
            this.pStartX = pStartX;
            this.pStartZ = pStartZ;
            this.pDestX = pDestX;
            this.pDestZ = pDestZ;
        }

        public List<PathPoint> getPathViaNodes() throws Exception {
            return getPathViaNodes(pStartX, pStartZ, pDestX, pDestZ);
        }

        List<PathPoint> getPathViaNodes(final int pStartX, final int pStartZ, final int pDestX, final int pDestZ) throws Exception {

            final long currentAge = System.currentTimeMillis();

            final int startX = pStartX - geography.mapStartX;
            final int startZ = pStartZ - geography.mapStartZ;
            final int destX = pDestX - geography.mapStartX;
            final int destZ = pDestZ - geography.mapStartZ;

            Node start = new Node(new Point2D(startX, startZ), 0, true);
            Node end = new Node(new Point2D(destX, destZ), 0, true);

            final Point2D originalDest = new Point2D(destX, destZ);

            final PathKey key = new PathKey(start.pos, end.pos);
            if (cache.containsKey(key) && currentAge - cache.get(key).age < 30000) {
                if (cache.get(key).points != null) {
                    return buildFinalPath(cache.get(key));
                } else {
                    return null;
                }
            }

            if (canSee(start.pos, end.pos)) {
                end.from = start;

                final CachedPath path = new CachedPath(buildPointsNode(end));
                cache.put(new PathKey(start.pos, end.pos), path);

                return buildFinalPath(path);
            }

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

            for (int i = nodes.size() - 1; i >= 0; i--) {
                if (nodes.get(i).temp) {
                    nodes.remove(i);
                }
            }

            synchronized (nodes) {
                for (final Node n : nodes) {
                    if (canSee(n.pos, start.pos)) {
                        start.neighbours.add(n);
                        start.region = n.region;
                        n.neighbours.add(start);
                        final int dist = start.pos.distanceTo(n.pos);
                        n.costs.put(start, dist);
                        start.costs.put(n, dist);
                    }
                    if (canSee(n.pos, end.pos)) {
                        end.neighbours.add(n);
                        end.region = n.region;
                        n.neighbours.add(end);
                        final int dist = end.pos.distanceTo(n.pos);
                        n.costs.put(end, dist);
                        end.costs.put(n, dist);
                    }

                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                }
            }

            if (start.region != end.region) {
                System.err.println("Start and end nodes in different groups: " + start + "/" + end);
                end.neighbours.clear();
            }

            if (start.neighbours.size() == 0) {

                boolean foundStartNode = false;

                for (int i = -1; i < 2 && !foundStartNode; i++) {
                    for (int j = -1; j < 2 && !foundStartNode; j++) {
                        if (i == 0 || j == 0) {// no diagonals
                            if (startX + i >= 0 && startZ + j >= 0 && startX + i < geography.length && startZ + j < geography.width) {
                                if (geography.topGround[startX][startZ] - geography.topGround[startX + i][startZ + j] < 3 && geography.topGround[startX][startZ] - geography.topGround[startX + i][startZ + j] > -3) {
                                    start = new Node(new Point2D(startX + i, startZ + j), 0, true);

                                    for (final Node n : nodes) {
                                        if (canSee(n.pos, start.pos)) {
                                            start.neighbours.add(n);
                                            start.region = n.region;
                                            n.neighbours.add(start);
                                            final int dist = start.pos.distanceTo(n.pos);
                                            n.costs.put(start, dist);
                                            start.costs.put(n, dist);
                                        }
                                    }
                                    if (start.neighbours.size() > 0) {
                                        foundStartNode = true;
                                    }
                                }
                            }
                        }
                        if (Thread.interrupted()) {
                            throw new InterruptedException();
                        }
                    }
                }

                if (!foundStartNode) {

                    cache.put(new PathKey(start.pos, end.pos), new CachedPath());

                    throw new PathingException("Start node " + start + " unreachable.", PathingException.UNREACHABLE_START);
                }
            }
            if (end.neighbours.size() == 0) {
                boolean foundEndNode = false;
                boolean foundEndNodeInOtherGroup = false;

                final List<Node> testedNodes = new ArrayList<>();

                for (int i = 0; i < 4 && !foundEndNode; i++) {
                    for (int j = 0; j <= i && !foundEndNode; j++) {
                        if (i == 0 || j == 0) {
                            for (int cpt = 0; cpt < 8; cpt++) {
                                if (cpt == 0) {
                                    end = new Node(new Point2D(destX + i, destZ + j), 0, true);
                                } else if (cpt == 1) {
                                    end = new Node(new Point2D(destX - i, destZ + j), 0, true);
                                } else if (cpt == 2) {
                                    end = new Node(new Point2D(destX + i, destZ - j), 0, true);
                                } else if (cpt == 3) {
                                    end = new Node(new Point2D(destX - i, destZ - j), 0, true);
                                } else if (cpt == 4) {
                                    end = new Node(new Point2D(destX + j, destZ + i), 0, true);
                                } else if (cpt == 5) {
                                    end = new Node(new Point2D(destX - j, destZ + i), 0, true);
                                } else if (cpt == 6) {
                                    end = new Node(new Point2D(destX + j, destZ - i), 0, true);
                                } else if (cpt == 7) {
                                    end = new Node(new Point2D(destX + j, destZ - i), 0, true);
                                }

                                if (!testedNodes.contains(end)) {

                                    for (final Node n : nodes) {
                                        if (canSee(n.pos, end.pos)) {
                                            end.neighbours.add(n);
                                            end.region = n.region;
                                            n.neighbours.add(end);
                                            final int dist = end.pos.distanceTo(n.pos);
                                            n.costs.put(end, dist);
                                            end.costs.put(n, dist);
                                        }
                                    }
                                    if (end.neighbours.size() > 0 && end.neighbours.get(0).region == start.region) {
                                        foundEndNode = true;
                                    } else if (end.neighbours.size() > 0) {
                                        foundEndNodeInOtherGroup = true;
                                    }

                                    testedNodes.add(end);
                                }
                            }
                        }
                        if (Thread.interrupted()) {
                            throw new InterruptedException();
                        }
                    }
                }

                if (!foundEndNode) {

                    cache.put(new PathKey(start.pos, end.pos), new CachedPath());

                    if (foundEndNodeInOtherGroup) {
                        return null;
                    } else {
                        throw new PathingException("End pos not connected to any node", PathingException.INVALID_GOAL);
                    }
                }
            }

            nodes.add(start);
            nodes.add(end);

            for (final Node n : nodes) {
                n.from = null;
                n.fromDist = -1;
                n.toDist = -1;
            }

            final List<Node> open = new ArrayList<>();
            final HashMap<Node, Node> closed = new HashMap<>();

            open.add(start);
            start.fromDist = 0;
            start.toDist = start.pos.distanceTo(end.pos);

            int nbNodesVisited = 0;

            Node closest = start;

            while (open.size() > 0) {

                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                nbNodesVisited++;

                if (nbNodesVisited > MAX_NODE_VISIT) {
                    final CachedPath cpath = new CachedPath(buildPointsNode(closest));

                    storeInCache(cpath, originalDest);

                    return buildFinalPath(cpath);
                }

                Node cn = null;
                int bestdistance = -1;
                for (final Node n : open) {

                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }

                    if (n.toDist == -1) {
                        n.toDist = n.pos.distanceTo(end.pos);
                    }
                    final int distance = n.fromDist + n.toDist;

                    if (n.equals(end)) {

                        final CachedPath cpath = new CachedPath(buildPointsNode(n));

                        storeInCache(cpath, originalDest);
                        cache.put(new PathKey(start.pos, end.pos), cpath);
                        cache.put(new PathKey(new Point2D(startX, startZ), new Point2D(destX, destZ)), cpath);

                        return buildFinalPath(cpath);
                    }

                    if (bestdistance == -1 || distance < bestdistance) {
                        cn = n;
                        bestdistance = distance;
                    }

                    if (n.toDist < closest.toDist) {
                        closest = n;
                    }
                }

                open.remove(cn);
                closed.put(cn, cn);

                for (final Node n : cn.neighbours) {
                    final Integer cost = cn.costs.get(n);

                    if (closed.containsKey(n)) {
                        // Log.debug(Log.getPath,
                        // n+" has already been visited.");
                    } else {
                        if (!open.contains(n)) {
                            n.fromDist = cn.fromDist + cost;
                            n.toDist = n.pos.distanceTo(end.pos);
                            n.from = cn;
                            open.add(n);
                        } else if (cn.fromDist + cost < n.fromDist) {
                            n.fromDist = cn.fromDist + cost;
                            n.from = cn;
                        }
                    }
                }

            }

            cache.put(new PathKey(start.pos, end.pos), new CachedPath());
            cache.put(new PathKey(new Point2D(startX, startZ), new Point2D(destX, destZ)), new CachedPath());

            return null;

        }

        @Override
        public void run() {
            synchronized (AStarPathing.this) {
                try {

                    final List<PathPoint> result = getPathViaNodes(pStartX, pStartZ, pDestX, pDestZ);
                    villager.registerNewPath(result);
                } catch (final InterruptedException e) {
                    //villager.registerNewPathInterrupt(this); //TODO
                    e.printStackTrace();
                } catch(PathingException e) {
                    if(e.errorCode != PathingException.UNREACHABLE_START) System.out.println(e.getMessage());
                } catch (final Exception e) {
                    //e.printStackTrace();
                }
            }
        }
    }

    public static class PathKey {

        Point2D start, end;

        PathKey(final Point2D start, final Point2D end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public boolean equals(final Object obj) {
            if (!(obj instanceof PathKey)) {
                return false;
            }

            final PathKey p = (PathKey) obj;
            return start.equals(p.start) && end.equals(p.end);

        }

        @Override
        public int hashCode() {
            return start.x + (start.z << 8) + (end.x << 16) + (end.z << 24);
        }

        @Override
        public String toString() {
            return start + ", " + end;
        }

    }

    static public class Point2D {

        int x, z;

        Point2D(final int px, final int pz) {
            x = px;
            z = pz;
        }

        public int distanceTo(final Point2D p) {

            final int d = p.x - x;
            final int d1 = p.z - z;

            return (int) Math.sqrt(d * d + d1 * d1);

        }

        @Override
        public boolean equals(final Object obj) {

            if (!(obj instanceof Point2D)) {
                return false;
            }

            final Point2D p = (Point2D) obj;
            return x == p.x && z == p.z;

        }

        @Override
        public int hashCode() {
            return x << 16 & z;
        }

        @Override
        public String toString() {
            return x + "/" + z;
        }
    }

    private static List<Point2D> buildPointsNode(final Node end) {

        if (end.from != null) {
            List<Point2D> path;

            path = buildPointsNode(end.from);

            path.add(end.pos);
            return path;
        } else {
            final List<Point2D> path = new ArrayList<>();

            path.add(end.pos);
            return path;
        }
    }

    private VillageGeography geography;
    public boolean[][] top;

    public boolean[][] bottom;

    private boolean[][] left;

    private boolean[][] right;

    private short[][] topGround;

    private byte[][] regions;

    private final List<Node> nodes = new ArrayList<>();

    private HashMap<PathKey, CachedPath> cache = new HashMap<>();

    public AStarPathing() {

    }

    private List<PathPoint> buildFinalPath(final CachedPath path) {

        final List<PathPoint> ppoints = new ArrayList<>();

        for (int i = 0; i < path.points.length; i++) {
            final Point2D p = path.points[i];

            int oldGround = topGround[p.x][p.z];
            final int newGround = geography.topGround[p.x][p.z];

            // if the ground has shifted but not too much use the new ground
            if (newGround < oldGround + 3 && newGround > oldGround - 3) {
                oldGround = newGround;
            }

            final PathPoint np = new PathPoint(p.x + geography.mapStartX, oldGround, p.z + geography.mapStartZ);
            ppoints.add(np);

        }

        return ppoints;

    }

    private void buildNodes() {
        synchronized (nodes) {
            nodes.clear();
            for (int i = 0; i < geography.length; i++) {
                for (int j = 0; j < geography.width; j++) {
                    boolean isNode = false;
                    int cornerSide = 0;

                    if (i > 0 && j > 0) {
                        if (top[i][j] && left[i][j] && (!left[i - 1][j] || !top[i][j - 1])) {
                            isNode = true;
                            cornerSide = cornerSide | 1;
                        }
                    }
                    if (i < geography.length - 1 && j > 0) {
                        if (bottom[i][j] && left[i][j] && (!left[i + 1][j] || !bottom[i][j - 1])) {
                            isNode = true;
                            cornerSide += 2;
                            cornerSide = cornerSide | 2;
                        }
                    }
                    if (i > 0 && j < geography.width - 1) {
                        if (top[i][j] && right[i][j] && (!right[i - 1][j] || !top[i][j + 1])) {
                            isNode = true;
                            cornerSide = cornerSide | 4;
                        }
                    }
                    if (i < geography.length - 1 && j < geography.width - 1) {
                        if (bottom[i][j] && right[i][j] && (!right[i + 1][j] || !bottom[i][j + 1])) {
                            isNode = true;
                            cornerSide = cornerSide | 8;
                        }
                    }

                    if (isNode) {
                        nodes.add(new Node(new Point2D(i, j), nodes.size(), cornerSide, false));
                    }
                }
            }

            // "Pushing" nodes away from walls if possible
            // Lessens units bumping into them
            for (final Node n : nodes) {
                // "Simple" corners:
                if (n.cornerSide == 1 && n.pos.x < geography.length - 1 && n.pos.z < geography.width - 1) {
                    if (bottom[n.pos.x][n.pos.z] && right[n.pos.x][n.pos.z] && bottom[n.pos.x][n.pos.z + 1] && right[n.pos.x + 1][n.pos.z]) {// next
                        // diagonal
                        // available
                        final int tx = n.pos.x + 1;
                        final int tz = n.pos.z + 1;
                        if (tx < geography.length - 1 && tz < geography.width - 1 && bottom[tx][tz] && right[tx][tz]) {
                            n.pos.x = tx;
                            n.pos.z = tz;
                        }
                    }
                }
                if (n.cornerSide == 2 && n.pos.x > 0 && n.pos.z < geography.width - 1) {
                    if (top[n.pos.x][n.pos.z] && right[n.pos.x][n.pos.z] && top[n.pos.x][n.pos.z + 1] && right[n.pos.x - 1][n.pos.z]) {
                        final int tx = n.pos.x - 1;
                        final int tz = n.pos.z + 1;
                        if (tx > 0 && tz < geography.width - 1 && top[tx][tz] && right[tx][tz]) {
                            n.pos.x = tx;
                            n.pos.z = tz;
                        }
                    }
                }
                if (n.cornerSide == 4 && n.pos.x < geography.length - 1 && n.pos.z > 0) {
                    if (bottom[n.pos.x][n.pos.z] && left[n.pos.x][n.pos.z] && bottom[n.pos.x][n.pos.z - 1] && left[n.pos.x + 1][n.pos.z]) {// next
                        // diagonal
                        // available
                        final int tx = n.pos.x + 1;
                        final int tz = n.pos.z - 1;
                        if (tx < geography.length - 1 && tz > 0 && bottom[tx][tz] && left[tx][tz]) {
                            n.pos.x = tx;
                            n.pos.z = tz;
                        }
                    }
                }
                if (n.cornerSide == 8 && n.pos.x > 0 && n.pos.z > 0) {
                    if (top[n.pos.x][n.pos.z] && left[n.pos.x][n.pos.z] && top[n.pos.x][n.pos.z - 1] && left[n.pos.x - 1][n.pos.z]) {// next
                        // diagonal
                        // available
                        final int tx = n.pos.x - 1;
                        final int tz = n.pos.z - 1;
                        if (tx > 0 && tz > 0 && top[tx][tz] && left[tx][tz]) {
                            n.pos.x = tx;
                            n.pos.z = tz;
                        }
                    }
                }

                // "Double" corners:
                if (n.cornerSide == 3 && n.pos.z < geography.width - 1) {
                    if (right[n.pos.x][n.pos.z]) {
                        final int tx = n.pos.x;
                        final int tz = n.pos.z + 1;
                        if (tz < geography.width - 1 && bottom[tx][tz] && right[tx][tz] && top[tx][tz]) {
                            n.pos.x = tx;
                            n.pos.z = tz;
                        }
                    }
                }
                if (n.cornerSide == 5 && n.pos.x < geography.length - 1) {
                    if (bottom[n.pos.x][n.pos.z]) {
                        final int tx = n.pos.x + 1;
                        final int tz = n.pos.z;
                        if (tx < geography.length - 1 && bottom[tx][tz] && right[tx][tz] && left[tx][tz]) {
                            n.pos.x = tx;
                            n.pos.z = tz;
                        }
                    }
                }
                if (n.cornerSide == 10 && n.pos.x > 0) {
                    if (top[n.pos.x][n.pos.z]) {
                        final int tx = n.pos.x - 1;
                        final int tz = n.pos.z;
                        if (tx > 0 && top[tx][tz] && right[tx][tz] && left[tx][tz]) {
                            n.pos.x = tx;
                            n.pos.z = tz;
                        }
                    }
                }
                if (n.cornerSide == 12 && n.pos.z > 0) {
                    if (left[n.pos.x][n.pos.z]) {
                        final int tx = n.pos.x;
                        final int tz = n.pos.z - 1;
                        if (tx > 0 && top[tx][tz] && bottom[tx][tz] && left[tx][tz]) {
                            n.pos.x = tx;
                            n.pos.z = tz;
                        }
                    }
                }
            }

            // Checking for redundant nodes
            for (int i = nodes.size() - 1; i > -1; i--) {
                for (int j = i - 1; j > -1; j--) {
                    if (nodes.get(i).equals(nodes.get(j))) {
                        nodes.remove(i);
                        break;
                    }
                }
            }
        }
    }

    private boolean canSee(final Point2D p1, final Point2D p2) {

        final int xdist = p2.x - p1.x;
        final int zdist = p2.z - p1.z;

        if (xdist == 0 && zdist == 0) {
            return true;
        }

        int xsign = 1;
        int zsign = 1;

        if (xdist < 0) {
            xsign = -1;
        }
        if (zdist < 0) {
            zsign = -1;
        }

        int x = p1.x;
        int z = p1.z;

        int xdone = 0;
        int zdone = 0;

        // Log.debug(Log.Connections, "canSee: "+p1+" to "+p2);

        // Log.debug(Log.Connections, "To travel: "+xdist+", "+zdist);

        while (x != p2.x || z != p2.z) {
            int nx, nz;

            if (xdist == 0 || zdist != 0 && xdone * 1000 / xdist > zdone * 1000 / zdist) {
                nz = z + zsign;
                nx = x;
                zdone += zsign;

                // Log.debug(Log.Connections, "Moving Z: "+zsign);

                if (zsign == 1 && !right[x][z]) {
                    return false;
                } else if (zsign == -1 && !left[x][z]) {
                    return false;
                }
            } else {
                nx = x + xsign;
                nz = z;
                xdone += xsign;

                // Log.debug(Log.Connections, "Moving X: "+xsign);

                if (xsign == 1 && !bottom[x][z]) {
                    return false;
                } else if (xsign == -1 && !top[x][z]) {
                    return false;
                }
            }
            x = nx;
            z = nz;
        }

        // Log.debug(Log.Connections, "Finished, returning true.");
        return true;
    }

    public boolean createConnectionsTable(final VillageGeography geography, final BlockPos thStanding) throws Exception {

        long startTime = System.nanoTime();
        final long totalStartTime = startTime;

        this.geography = geography;

        top = new boolean[geography.length][geography.width];
        bottom = new boolean[geography.length][geography.width];
        left = new boolean[geography.length][geography.width];
        right = new boolean[geography.length][geography.width];
        regions = new byte[geography.length][geography.width];

        /*
         * Deep copy of the topGround array from world info because it is
         * updated more often than pathing And since the pathing system uses it
         * to find the height of the final path, if it has been updated in the
         * meantime it can produce weird result (such as a path point at the top
         * of a tree that has grown in the meantime)
         */

        topGround = VillageGeography.shortArrayDeepClone(geography.topGround);

        for (int i = 0; i < geography.length; i++) {
            for (int j = 0; j < geography.width; j++) {
                final int y = geography.topGround[i][j];
                final int space = geography.spaceAbove[i][j];

                if (space > 1) {// if not, block is not passable anyway

                    if (i > 0) {
                        final int ny = geography.topGround[i - 1][j];
                        final int nspace = geography.spaceAbove[i - 1][j];

                        boolean connected = false;

                        if (ny == y && nspace > 1) {
                            connected = true;
                        } else if (ny == y - 1 && nspace > 2) {
                            connected = true;
                        } else if (ny == y + 1 && nspace > 1 && space > 2) {
                            connected = true;
                        }

                        if (connected) {
                            top[i][j] = true;
                            bottom[i - 1][j] = true;
                        }
                    }
                    if (j > 0) {
                        final int ny = geography.topGround[i][j - 1];
                        final int nspace = geography.spaceAbove[i][j - 1];

                        boolean connected = false;

                        if (ny == y && nspace > 1) {
                            connected = true;
                        } else if (ny == y - 1 && nspace > 2) {
                            connected = true;
                        } else if (ny == y + 1 && nspace > 1 && space > 2) {
                            connected = true;
                        }

                        if (connected) {
                            left[i][j] = true;
                            right[i][j - 1] = true;
                        }
                    }
                }
            }
        }

        startTime = System.nanoTime();

        System.out.println("Building nodes...");
        buildNodes();
        System.out.println(nodes.size() + " nodes built. ");

        startTime = System.nanoTime();

        for (final Node n : nodes) {
            for (final Node n2 : nodes) {
                if (n.id < n2.id) {
                    if (canSee(n.pos, n2.pos)) {
                        final Integer distance = n.pos.distanceTo(n2.pos);
                        n.costs.put(n2, distance);
                        n.neighbours.add(n2);
                        n2.costs.put(n, distance);
                        n2.neighbours.add(n);
                    }
                }
            }
        }

        startTime = System.nanoTime();

        findRegions(thStanding);

        return true;
    }

    public PathingWorker createWorkerForPath(final EntityMillVillager villager, final int pStartX, final int pStartZ, final int pDestX, final int pDestZ) {
        final PathingWorker worker = new PathingWorker(villager, pStartX, pStartZ, pDestX, pDestZ);
        worker.start();
        return worker;
    }

    private void findRegions(final BlockPos thStanding) throws Exception {

        int nodesMarked = 0, nodeGroup = 0;

        while (nodesMarked < nodes.size()) {

            nodeGroup++;

            final List<Node> toVisit = new ArrayList<>();

            Node fn = null;

            int i = 0;
            while (fn == null) {
                if (nodes.get(i).region == 0) {
                    fn = nodes.get(i);
                }
                i++;
            }

            fn.region = nodeGroup;
            nodesMarked++;

            toVisit.add(fn);

            while (toVisit.size() > 0) {

                for (final Node n : toVisit.get(0).neighbours) {
                    if (n.region == 0) {
                        n.region = nodeGroup;
                        toVisit.add(n);
                        nodesMarked++;
                    } else if (n.region != nodeGroup) {
                        throw new IllegalStateException("Node belongs to group " + n.region + " but reached from " + nodeGroup);
                    }
                }
                toVisit.remove(0);
            }
        }

        for (int i = 0; i < geography.length; i++) {
            for (int j = 0; j < geography.width; j++) {
                regions[i][j] = -1;
            }
        }

        for (final Node n : nodes) {
            regions[n.pos.x][n.pos.z] = (byte) n.region;
        }

        boolean spreaddone = true;

        while (spreaddone) {
            spreaddone = false;
            for (int i = 0; i < geography.length; i++) {
                for (int j = 0; j < geography.width; j++) {
                    if (regions[i][j] > 0) {
                        final byte regionid = regions[i][j];
                        int x = i;
                        while (x > 1 && top[x][j] && regions[x - 1][j] == -1) {
                            x--;
                            regions[x][j] = regionid;
                            spreaddone = true;
                        }
                        x = i;
                        while (x < geography.length - 1 && bottom[x][j] && regions[x + 1][j] == -1) {
                            x++;
                            regions[x][j] = regionid;
                            spreaddone = true;
                        }
                        x = j;
                        while (x > 1 && left[i][x] && regions[i][x - 1] == -1) {
                            x--;
                            regions[i][x] = regionid;
                            spreaddone = true;
                        }
                        x = j;
                        while (x < geography.width - 1 && right[i][x] && regions[i][x + 1] == -1) {
                            x++;
                            regions[i][x] = regionid;
                            spreaddone = true;
                        }

                    }
                }
            }
        }

    }

    private boolean isInArea(final BlockPos p) {
        return !(p.getX() < geography.mapStartX || p.getX() >= geography.mapStartX + geography.length || p.getZ() < geography.mapStartZ || p.getZ() >= geography.mapStartZ + geography.width);
    }

    public boolean isValidPoint(final BlockPos p) {

        if (!isInArea(p)) {
            return false;
        }

        return geography.spaceAbove[p.getX() - geography.mapStartX][p.getZ() - geography.mapStartZ] > 1;

    }

    private void storeInCache(final CachedPath path, final Point2D dest) throws Exception {

        for (final Point2D p : path.points) {
            if (p == null) {
                throw new Exception("Null node in path to cache: " + path);
            }
        }

        cache.put(path.getKey(), path);

        if (!dest.equals(path.getEnd())) {
            cache.put(new PathKey(path.getStart(), dest), path);
        }

        // MLLog.major(null, MLLog.Pathing,
        // "Storing main path: "+path+" key: "+path.getKey());

        for (int i = 1; i < path.points.length - 2; i++) {
            final CachedPath cp = new CachedPath(path, i);

            for (final Point2D p : cp.points) {
                if (p == null) {
                    throw new Exception("Null node in path to cache: " + path);
                }
            }

            cache.put(cp.getKey(), cp);

            if (!dest.equals(cp.getEnd())) {
                cache.put(new PathKey(cp.getStart(), dest), cp);
                // MLLog.major(null, MLLog.Pathing,
                // "Storing secondary path: "+cp+" key: "+cp.getKey());
            }
        }
    }

}
