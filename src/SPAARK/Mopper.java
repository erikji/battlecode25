package SPAARK;

import battlecode.common.*;
import java.util.*;

public class Mopper {
    public static boolean tempReachedCenter = false;
    public static final int EXPLORE = 0;
    public static final int RETREAT = 1;
    public static int mode = EXPLORE;

    public static void run() throws Exception {
        if (G.rc.getPaint() < G.rc.getType().paintCapacity / 3) {
            mode = RETREAT;
        }
        else {
            mode = EXPLORE;
        }
        switch (mode) {
            case EXPLORE:
                G.indicatorString.append("EXPLORE ");
                Motion.spreadRandomly();
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
                break;
            case RETREAT:
                G.indicatorString.append("RETREAT ");
                Robot.retreat();
                break;
        }
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
        if (left == 3) G.rc.mopSwing(Direction.WEST);
        else if (right == 3) G.rc.mopSwing(Direction.EAST);
        else if (up == 3) G.rc.mopSwing(Direction.NORTH);
        else if (down == 3) G.rc.mopSwing(Direction.SOUTH);
        else return false;
        return true;
    }
}
