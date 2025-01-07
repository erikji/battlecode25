package SPAARK;

import battlecode.common.*;
import java.util.*;

public class Mopper {
    public static RobotController rc;
    public static Random rng;
    public static MapLocation currLoc;

    public static boolean tempReachedCenter = false;

    public static void run() throws Exception {
        Motion.currLoc = currLoc = rc.getLocation();
        // idk just make it attack anything it sees
        RobotInfo best = null;
        if (rc.isActionReady()) {
            RobotInfo[] robots = rc.senseNearbyRobots(-1, POI.opponentTeam);
            for (RobotInfo r : robots) {
                if (r.location.distanceSquaredTo(currLoc) <= 2 && (best == null || r.health < best.health)) {
                    best = r;
                }
            }
        }
        if (best != null && rc.canAttack(best.location)) {
            rc.attack(best.location);
        } else {
            // no bots attacked, look for paint
            MapInfo[] mapInfos = rc.senseNearbyMapInfos();
            for (MapInfo map : mapInfos) {
                if ((map.getPaint() == PaintType.ENEMY_PRIMARY || map.getPaint() == PaintType.ENEMY_SECONDARY)) {
                    int distSq = map.getMapLocation().distanceSquaredTo(currLoc);
                    if (distSq <= 2 && rc.isActionReady() && rc.canAttack(map.getMapLocation())) {
                        rc.attack(map.getMapLocation());
                    }
                    Motion.bugnavAround(map.getMapLocation(), 0, 2);
                }
            }
        }
        // move i guess?
        if (currLoc.distanceSquaredTo(Motion.mapCenter) < 8) tempReachedCenter = true;
        if (tempReachedCenter) Motion.bugnavTowards(Motion.mapCenter);
        else Motion.spreadRandomly();
    }

    /** Move this out later!!! */
    public static void mopSwingMicroSpaghetti() throws Exception {
        // spaghetti copy paste
        int up = 0, down = 0, left = 0, right = 0;
        RobotInfo r = rc.senseRobotAtLocation(currLoc.add(Direction.NORTH));
        if (r != null && r.team == POI.opponentTeam)
            up++;
        r = rc.senseRobotAtLocation(currLoc.add(Direction.NORTHEAST));
        if (r != null && r.team == POI.opponentTeam) {
            up++;
            right++;
        }
        r = rc.senseRobotAtLocation(currLoc.add(Direction.EAST));
        if (r != null && r.team == POI.opponentTeam)
            right++;
        r = rc.senseRobotAtLocation(currLoc.add(Direction.SOUTHEAST));
        if (r != null && r.team == POI.opponentTeam) {
            down++;
            right++;
        }
        r = rc.senseRobotAtLocation(currLoc.add(Direction.SOUTH));
        if (r != null && r.team == POI.opponentTeam)
            down++;
        r = rc.senseRobotAtLocation(currLoc.add(Direction.SOUTHWEST));
        if (r != null && r.team == POI.opponentTeam) {
            down++;
            left++;
        }
        r = rc.senseRobotAtLocation(currLoc.add(Direction.WEST));
        if (r != null && r.team == POI.opponentTeam)
            left++;
        r = rc.senseRobotAtLocation(currLoc.add(Direction.NORTHWEST));
        if (r != null && r.team == POI.opponentTeam) {
            up++;
            left++;
        }
        // SPAGHETITIITIUUITHREIHSIHDFSDF
        // good code not needed
        if (left == 0 && right == 0 && up == 0 && down == 0)
            return;
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
    }
}
