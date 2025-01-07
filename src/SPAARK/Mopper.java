package SPAARK;

import battlecode.common.*;
import java.util.*;

public class Mopper {
    public static RobotController rc;
    public static Random rng;
    public static MapLocation currLoc;
    public static boolean[][] resourcePattern;
    public static boolean[][][] towerPatterns;

    public static boolean tempReachedCenter = false;

    public static void run() throws Exception {
        Motion.currLoc = currLoc = rc.getLocation();
        // idk just make it attack anything it sees
        if (!trySwing()) {
            // no bots attacked, look for paint
            MapInfo[] mapInfos = rc.senseNearbyMapInfos();
            MapInfo best = null;
            for (MapInfo info : mapInfos) {
                if (((info.getPaint() == PaintType.ENEMY_PRIMARY || info.getPaint() == PaintType.ENEMY_SECONDARY) && (best == null || (rc.canSenseRobotAtLocation(info.getMapLocation()) && rc.senseRobotAtLocation(info.getMapLocation()).team == POI.opponentTeam)))) {
                    //if we didn't mop swing, prioritize unpainting squares under opponents
                    int distSq = info.getMapLocation().distanceSquaredTo(currLoc);
                    if (distSq <= 2 && rc.isActionReady() && rc.canAttack(info.getMapLocation())) {
                        rc.attack(info.getMapLocation());
                    }
                    Motion.bugnavAround(info.getMapLocation(), 0, 2);
                    break;
                }
            }
        }
        // move i guess?
        if (currLoc.distanceSquaredTo(Motion.mapCenter) < 8) tempReachedCenter = true;
        if (tempReachedCenter) Motion.bugnavTowards(Motion.mapCenter);
        else Motion.spreadRandomly();
    }

    /** Move this out later!!! */
    /**
     * Attempt mop swing with some microstrategy. Returns if a swing was executed.
     */
    public static boolean trySwing() throws Exception {
        // spaghetti copy paste
        int up = 0, down = 0, left = 0, right = 0;
        RobotInfo r;
        if (rc.onTheMap(currLoc.add(Direction.NORTH))) {
            r = rc.senseRobotAtLocation(currLoc.add(Direction.NORTH));
            if (r != null && r.team == POI.opponentTeam)
                up += r.paintAmount;
        }
        if (rc.onTheMap(currLoc.add(Direction.NORTHEAST))) {
            r = rc.senseRobotAtLocation(currLoc.add(Direction.NORTHEAST));
            if (r != null && r.team == POI.opponentTeam) {
                up += r.paintAmount;
                right += r.paintAmount;
            }
        }
        if (rc.onTheMap(currLoc.add(Direction.EAST))) {
            r = rc.senseRobotAtLocation(currLoc.add(Direction.EAST));
            if (r != null && r.team == POI.opponentTeam)
                right += r.paintAmount;
        }
        if (rc.onTheMap(currLoc.add(Direction.SOUTHEAST))) {
            r = rc.senseRobotAtLocation(currLoc.add(Direction.SOUTHEAST));
            if (r != null && r.team == POI.opponentTeam) {
                down += r.paintAmount;
                right += r.paintAmount;
            }
        }
        if (rc.onTheMap(currLoc.add(Direction.SOUTH))) {
            r = rc.senseRobotAtLocation(currLoc.add(Direction.SOUTH));
            if (r != null && r.team == POI.opponentTeam)
                down += r.paintAmount;
        }
        if (rc.onTheMap(currLoc.add(Direction.SOUTHWEST))) {
            r = rc.senseRobotAtLocation(currLoc.add(Direction.SOUTHWEST));
            if (r != null && r.team == POI.opponentTeam) {
                down += r.paintAmount;
                left += r.paintAmount;
            }
        }
        if (rc.onTheMap(currLoc.add(Direction.WEST))) {
            r = rc.senseRobotAtLocation(currLoc.add(Direction.WEST));
            if (r != null && r.team == POI.opponentTeam)
                left += r.paintAmount;
        }
        if (rc.onTheMap(currLoc.add(Direction.NORTHWEST))) {
            r = rc.senseRobotAtLocation(currLoc.add(Direction.NORTHWEST));
            if (r != null && r.team == POI.opponentTeam) {
                up += r.paintAmount;
                left += r.paintAmount;
            }
        }
        // SPAGHETITIITIUUITHREIHSIHDFSDF
        // good code not needed
        if (left + right + up + down == 0) return false;
        if (left > right) {
            if (up > left) {
                if (down > up)
                    rc.mopSwing(Direction.SOUTH);
                else
                    rc.mopSwing(Direction.NORTH);
            } else {
                if (down > left)
                    rc.mopSwing(Direction.SOUTH);
                else
                    rc.mopSwing(Direction.WEST);
            }
        } else {
            if (up > right) {
                if (down > up)
                    rc.mopSwing(Direction.SOUTH);
                else
                    rc.mopSwing(Direction.NORTH);
            } else {
                if (down > right)
                    rc.mopSwing(Direction.SOUTH);
                else
                    rc.mopSwing(Direction.EAST);
            }
        }
        return true;
    }
}
