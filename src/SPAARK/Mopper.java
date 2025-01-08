package SPAARK;

import battlecode.common.*;
import java.util.*;

public class Mopper {
    public static MapLocation ruinLocation = null; //BUILD mode
    public static final int EXPLORE = 0;
    public static final int BUILD = 1;
    public static final int RETREAT = 2;
    public static int mode = EXPLORE;
    public static int lastBuild = -10; //last time we are in BUILD mode to prevent 

    public static void run() throws Exception {
        if (G.rc.getPaint() < G.rc.getType().paintCapacity / 3) {
            mode = RETREAT;
        } else {
            mode = EXPLORE;
            if (lastBuild + 10 < G.rc.getRoundNum()) {
                MapLocation[] locs = G.rc.senseNearbyRuins(-1);
                for (MapLocation loc : locs) {
                    if (G.rc.canSenseRobotAtLocation(loc)) {
                        continue; //tower already there
                    }
                    ruinLocation = loc;
                    mode = BUILD;
                    break;
                }
            }
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
            case BUILD:
                G.indicatorString.append("BUILD ");
                G.rc.setIndicatorLine(G.rc.getLocation(), ruinLocation, 255, 255, 0);
                if (!G.rc.canSenseLocation(ruinLocation) || G.rc.canSenseRobotAtLocation(ruinLocation)) {
                    mode = EXPLORE;
                    ruinLocation = null;
                } else {
                    MapInfo[] infos = G.rc.senseNearbyMapInfos();
                    MapLocation bestLoc = null;
                    MapLocation bestLoc2 = null;
                    int bestDistanceSquared = 10000;
                    int bestDistanceSquared2 = 10001;
                    for (MapInfo info : infos) {
                        if (info.getMapLocation().distanceSquaredTo(ruinLocation) <= 8 && !info.getPaint().isAlly() && info.getPaint() != PaintType.EMPTY) {
                            //necessarily opponent paint
                            int distanceSquared = info.getMapLocation().distanceSquaredTo(Motion.currLoc);
                            if (distanceSquared<bestDistanceSquared){
                                bestDistanceSquared2 = bestDistanceSquared;
                                bestLoc2 = bestLoc;
                                bestDistanceSquared = distanceSquared;
                                bestLoc = info.getMapLocation();
                            }
                            else if (distanceSquared<bestDistanceSquared2){
                                bestDistanceSquared2 = distanceSquared;
                                bestLoc2 = info.getMapLocation();
                            }
                        }
                    }
                    if (bestLoc != null) {
                        G.rc.setIndicatorLine(Motion.currLoc, bestLoc, 128, 128, 128);
                        if (G.rc.canAttack(bestLoc)) {
                            G.rc.attack(bestLoc);
                            if (bestLoc2 != null) {
                                G.rc.setIndicatorLine(Motion.currLoc, bestLoc2, 255, 255, 255);
                                Motion.bugnavTowards(bestLoc2);
                            } else if (Motion.currLoc.distanceSquaredTo(ruinLocation) <= 4) {
                                mode = EXPLORE;
                                ruinLocation = null;
                                lastBuild = G.rc.getRoundNum();
                                Motion.spreadRandomly();
                            } else {
                                Motion.bugnavTowards(ruinLocation);
                            }
                        } else {
                            Motion.bugnavTowards(bestLoc);
                        }
                    } else if (Motion.currLoc.distanceSquaredTo(ruinLocation) <= 4) {
                        mode = EXPLORE;
                        ruinLocation = null;
                        lastBuild = G.rc.getRoundNum();
                        Motion.spreadRandomly();
                    } else {
                        Motion.bugnavTowards(ruinLocation);
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
