package SPAARK;

import battlecode.common.*;
import java.util.*;

public class Mopper {
    public static boolean tempReachedCenter = false;
    public static final int EXPLORE = 0;
    public static final int RETREAT = 1;
    public static int mode = EXPLORE;

    public static void run() throws Exception {
        if (G.rc.getPaint() < G.rc.getType().paintCapacity / 3)
            mode = RETREAT;
        else
            mode = EXPLORE;
        switch (mode) {
            case EXPLORE:
                G.indicatorString.append("EXPLORE ");
                Motion.spreadRandomly();
                if (!mopSwingWithMicro()) {
                    // no bots attacked, look for paint
                    MapInfo[] mapInfos = G.rc.senseNearbyMapInfos();
                    for (MapInfo info : mapInfos) {
                        PaintType p = info.getPaint();
                        if ((p == PaintType.ENEMY_PRIMARY || p == PaintType.ENEMY_SECONDARY)
                                && G.rc.canSenseRobotAtLocation(info.getMapLocation())
                                && G.rc.senseRobotAtLocation(info.getMapLocation()).team == POI.opponentTeam) {
                            // if we didn't mop swing, prioritize unpainting squares under opponents
                            int distSq = info.getMapLocation().distanceSquaredTo(G.me);
                            if (distSq <= 2 && G.rc.isActionReady() &&
                                    G.rc.canAttack(info.getMapLocation())) {
                                G.rc.attack(info.getMapLocation());
                            }
                            Motion.bugnavAround(info.getMapLocation(), 0, 2);
                            break;
                        }
                    }
                }
                break;
            case RETREAT:
                G.indicatorString.append("RETREAT ");
                Robot.retreat();
                break;
        }
    }

    public static void attackPaintWithMicro() throws Exception {
        MapInfo[] mapInfos = G.rc.senseNearbyMapInfos();
        MapInfo best = null;
        float bestPaint = -1;
        MapLocation microDir = G.rc.getLocation();
        for (MapInfo info : mapInfos) {
            MapLocation loc = info.getMapLocation();
            if (G.rc.canSenseRobotAtLocation(loc)) {
                microDir.add(G.me.directionTo(loc));
                RobotInfo r = G.rc.senseRobotAtLocation(loc);
                float paint = r.paintAmount / (float) r.type.paintCapacity;
                if (paint > bestPaint) {

                }
            }
        }
    }

    /** Move this out later!!! */
    /**
     * Attempt mop swing with some microstrategy. Returns if a swing was executed.
     */
    public static boolean mopSwingWithMicro() throws Exception {
        // spaghetti copy paste
        float up = 0, down = 0, left = 0, right = 0;
        RobotInfo r;
        if (G.rc.onTheMap(G.me.add(Direction.NORTH))) {
            r = G.rc.senseRobotAtLocation(G.me.add(Direction.NORTH));
            if (r != null && r.team == POI.opponentTeam)
                up += r.paintAmount / (float) r.type.paintCapacity;
        }
        if (G.rc.onTheMap(G.me.add(Direction.NORTHEAST))) {
            r = G.rc.senseRobotAtLocation(G.me.add(Direction.NORTHEAST));
            if (r != null && r.team == POI.opponentTeam) {
                up += r.paintAmount / (float) r.type.paintCapacity;
                right += r.paintAmount / (float) r.type.paintCapacity;
            }
        }
        if (G.rc.onTheMap(G.me.add(Direction.EAST))) {
            r = G.rc.senseRobotAtLocation(G.me.add(Direction.EAST));
            if (r != null && r.team == POI.opponentTeam)
                right += r.paintAmount / (float) r.type.paintCapacity;
        }
        if (G.rc.onTheMap(G.me.add(Direction.SOUTHEAST))) {
            r = G.rc.senseRobotAtLocation(G.me.add(Direction.SOUTHEAST));
            if (r != null && r.team == POI.opponentTeam) {
                down += r.paintAmount / (float) r.type.paintCapacity;
                right += r.paintAmount / (float) r.type.paintCapacity;
            }
        }
        if (G.rc.onTheMap(G.me.add(Direction.SOUTH))) {
            r = G.rc.senseRobotAtLocation(G.me.add(Direction.SOUTH));
            if (r != null && r.team == POI.opponentTeam)
                down += r.paintAmount / (float) r.type.paintCapacity;
        }
        if (G.rc.onTheMap(G.me.add(Direction.SOUTHWEST))) {
            r = G.rc.senseRobotAtLocation(G.me.add(Direction.SOUTHWEST));
            if (r != null && r.team == POI.opponentTeam) {
                down += r.paintAmount / (float) r.type.paintCapacity;
                left += r.paintAmount / (float) r.type.paintCapacity;
            }
        }
        if (G.rc.onTheMap(G.me.add(Direction.WEST))) {
            r = G.rc.senseRobotAtLocation(G.me.add(Direction.WEST));
            if (r != null && r.team == POI.opponentTeam)
                left += r.paintAmount / (float) r.type.paintCapacity;
        }
        if (G.rc.onTheMap(G.me.add(Direction.NORTHWEST))) {
            r = G.rc.senseRobotAtLocation(G.me.add(Direction.NORTHWEST));
            if (r != null && r.team == POI.opponentTeam) {
                up += r.paintAmount / (float) r.type.paintCapacity;
                left += r.paintAmount / (float) r.type.paintCapacity;
            }
        }
        // SPAGHETITIITIUUITHREIHSIHDFSDF
        if (left + right + up + down == 0)
            return false;
        if (left > right) {
            if (up > left) {
                if (down > up)
                    G.rc.mopSwing(Direction.SOUTH);
                else
                    G.rc.mopSwing(Direction.NORTH);
            } else {
                if (down > left)
                    G.rc.mopSwing(Direction.SOUTH);
                else
                    G.rc.mopSwing(Direction.WEST);
            }
        } else {
            if (up > right) {
                if (down > up)
                    G.rc.mopSwing(Direction.SOUTH);
                else
                    G.rc.mopSwing(Direction.NORTH);
            } else {
                if (down > right)
                    G.rc.mopSwing(Direction.SOUTH);
                else
                    G.rc.mopSwing(Direction.EAST);
            }
        }
        return true;
    }
}
