package SPAARK;

import battlecode.common.*;
import java.util.*;

public class Mopper {
    public static boolean tempReachedCenter = false;

    public static void run() throws Exception {
        Motion.currLoc = G.rc.getLocation();
        // idk just make it attack anything it sees
        if (!trySwing()) {
            // no bots attacked, look for paint
            MapInfo[] mapInfos = G.rc.senseNearbyMapInfos();
            MapInfo best = null;
            for (MapInfo info : mapInfos) {
                if (((info.getPaint() == PaintType.ENEMY_PRIMARY || info.getPaint() == PaintType.ENEMY_SECONDARY) && (best == null || (G.rc.canSenseRobotAtLocation(info.getMapLocation()) && G.rc.senseRobotAtLocation(info.getMapLocation()).team == POI.opponentTeam)))) {
                    //if we didn't mop swing, prioritize unpainting squares under opponents
                    int distSq = info.getMapLocation().distanceSquaredTo(Motion.currLoc);
                    if (distSq <= 2 && G.rc.isActionReady() && G.rc.canAttack(info.getMapLocation())) {
                        G.rc.attack(info.getMapLocation());
                    }
                    Motion.bugnavAround(info.getMapLocation(), 0, 2);
                    break;
                }
            }
        }
        // move i guess?
        if (Motion.currLoc.distanceSquaredTo(Motion.mapCenter) < 8) tempReachedCenter = true;
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
        if (G.rc.onTheMap(Motion.currLoc.add(Direction.NORTH))) {
            r = G.rc.senseRobotAtLocation(Motion.currLoc.add(Direction.NORTH));
            if (r != null && r.team == POI.opponentTeam)
                up += r.paintAmount;
        }
        if (G.rc.onTheMap(Motion.currLoc.add(Direction.NORTHEAST))) {
            r = G.rc.senseRobotAtLocation(Motion.currLoc.add(Direction.NORTHEAST));
            if (r != null && r.team == POI.opponentTeam) {
                up += r.paintAmount;
                right += r.paintAmount;
            }
        }
        if (G.rc.onTheMap(Motion.currLoc.add(Direction.EAST))) {
            r = G.rc.senseRobotAtLocation(Motion.currLoc.add(Direction.EAST));
            if (r != null && r.team == POI.opponentTeam)
                right += r.paintAmount;
        }
        if (G.rc.onTheMap(Motion.currLoc.add(Direction.SOUTHEAST))) {
            r = G.rc.senseRobotAtLocation(Motion.currLoc.add(Direction.SOUTHEAST));
            if (r != null && r.team == POI.opponentTeam) {
                down += r.paintAmount;
                right += r.paintAmount;
            }
        }
        if (G.rc.onTheMap(Motion.currLoc.add(Direction.SOUTH))) {
            r = G.rc.senseRobotAtLocation(Motion.currLoc.add(Direction.SOUTH));
            if (r != null && r.team == POI.opponentTeam)
                down += r.paintAmount;
        }
        if (G.rc.onTheMap(Motion.currLoc.add(Direction.SOUTHWEST))) {
            r = G.rc.senseRobotAtLocation(Motion.currLoc.add(Direction.SOUTHWEST));
            if (r != null && r.team == POI.opponentTeam) {
                down += r.paintAmount;
                left += r.paintAmount;
            }
        }
        if (G.rc.onTheMap(Motion.currLoc.add(Direction.WEST))) {
            r = G.rc.senseRobotAtLocation(Motion.currLoc.add(Direction.WEST));
            if (r != null && r.team == POI.opponentTeam)
                left += r.paintAmount;
        }
        if (G.rc.onTheMap(Motion.currLoc.add(Direction.NORTHWEST))) {
            r = G.rc.senseRobotAtLocation(Motion.currLoc.add(Direction.NORTHWEST));
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
