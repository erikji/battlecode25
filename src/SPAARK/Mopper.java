package SPAARK;

import battlecode.common.*;
import java.util.*;

public class Mopper {
    public static MapLocation ruinLocation = null; // BUILD mode
    public static final int EXPLORE = 0;
    public static final int BUILD = 1;
    public static final int RETREAT = 2;
    public static int mode = EXPLORE;
    public static int lastBuild = -10; // last time we are in BUILD mode to prevent

    public static void run() throws Exception {
        if (G.rc.getPaint() < G.rc.getType().paintCapacity / 3) {
            mode = RETREAT;
        } else if (G.rc.getPaint() > G.rc.getType().paintCapacity - 40) {
            mode = EXPLORE;
        }
        // make sure not stuck between exploring and building
        if (mode == EXPLORE && lastBuild + 10 < G.rc.getRoundNum()) {
            MapLocation[] locs = G.rc.senseNearbyRuins(-1);
            for (MapLocation loc : locs) {
                if (G.rc.canSenseRobotAtLocation(loc)) {
                    continue; // tower already there
                }
                ruinLocation = loc;
                mode = BUILD;
                break;
            }
        }
        switch (mode) {
            case EXPLORE -> explore();
            case BUILD -> build();
            case RETREAT -> {
                G.indicatorString.append("RETREAT ");
                Robot.retreat();
            }
        }
    }

    public static void explore() throws Exception {
        G.indicatorString.append("EXPLORE ");
        int bt = Clock.getBytecodesLeft();
        mopSwingWithMicro();
        G.indicatorString.append((Clock.getBytecodesLeft() - bt) + " ");
        MapInfo[] mapInfos = G.rc.senseNearbyMapInfos();
        // will try to unpaint squares under opponent bots
        // but if no opponents, just move to paint and attack
        // store both the best empty tile and the best opponent bot tile
        MapLocation bestBot = null;
        MapLocation bestEmpty = null;
        double bestPaint = -1;
        int bestDist = -1;
        MapLocation microDir = G.me;
        for (int i = mapInfos.length; --i >= 0;) {
            MapInfo info = mapInfos[i];
            MapLocation loc = info.getMapLocation();
            PaintType p = info.getPaint();
            if (p.isEnemy()) {
                microDir = microDir.add(G.me.directionTo(loc));
                if (G.rc.canSenseRobotAtLocation(loc)) {
                    RobotInfo r = G.rc.senseRobotAtLocation(loc);
                    if (r.team == POI.opponentTeam) {
                        double paint = r.paintAmount / (double) r.type.paintCapacity;
                        if (paint > bestPaint) {
                            bestPaint = paint;
                            bestBot = loc;
                        }
                    }
                } else {
                    int dist = G.me.distanceSquaredTo(loc);
                    if (bestEmpty == null || G.rc.canAttack(loc) && dist < bestDist) {
                        bestEmpty = loc;
                        bestDist = dist;
                    }
                }
            }
        }
        G.indicatorString.append((Clock.getBytecodesLeft() - bt) + " ");
        // this is using all the bytecode???
        if (G.rc.onTheMap(microDir))
            G.rc.setIndicatorLine(G.me, microDir, 0, 200, 255);
        G.rc.setIndicatorString("sdf " + Clock.getBytecodesLeft());
        if (bestEmpty == null && bestBot == null) {
            if (G.me.distanceSquaredTo(microDir) >= 2) {
                G.rc.setIndicatorString("a " + Clock.getBytecodesLeft());
                Motion.bugnavTowards(microDir);
            } else {
                G.rc.setIndicatorString("b " + Clock.getBytecodesLeft());
                G.indicatorString.append("RAND ");
                Motion.spreadRandomly();
            }
        } else {
            if (bestBot != null)
                bestEmpty = bestBot;
            G.rc.setIndicatorLine(G.me, bestEmpty, 0, 0, 255);
            if (G.rc.canAttack(bestEmpty))
                G.rc.attack(bestEmpty);
            G.indicatorString.append(Clock.getBytecodesLeft());
            G.rc.setIndicatorString("c " + Clock.getBytecodesLeft());
            Motion.bugnavAround(bestEmpty, 1, 1);
        }
    }

    public static void build() throws Exception {
        G.indicatorString.append("BUILD ");
        // get 2 best locations to build stuff on
        // so if the first one is already there just go to the next one
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
                if (info.getMapLocation().distanceSquaredTo(ruinLocation) <= 8 && info.getPaint().isEnemy()) {
                    int distanceSquared = info.getMapLocation().distanceSquaredTo(G.me);
                    if (distanceSquared < bestDistanceSquared) {
                        bestDistanceSquared2 = bestDistanceSquared;
                        bestLoc2 = bestLoc;
                        bestDistanceSquared = distanceSquared;
                        bestLoc = info.getMapLocation();
                    } else if (distanceSquared < bestDistanceSquared2) {
                        bestDistanceSquared2 = distanceSquared;
                        bestLoc2 = info.getMapLocation();
                    }
                }
            }
            if (bestLoc != null) {
                G.rc.setIndicatorLine(G.me, bestLoc, 255, 255, 255);
                if (G.rc.canAttack(bestLoc)) {
                    G.rc.attack(bestLoc);
                    if (bestLoc2 != null) {
                        G.rc.setIndicatorLine(G.me, bestLoc2, 128, 128, 128);
                        Motion.bugnavTowards(bestLoc2);
                    } else if (G.me.distanceSquaredTo(ruinLocation) <= 4) {
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
            } else if (G.me.distanceSquaredTo(ruinLocation) <= 4) {
                mode = EXPLORE;
                ruinLocation = null;
                lastBuild = G.rc.getRoundNum();
                Motion.spreadRandomly();
            } else {
                Motion.bugnavTowards(ruinLocation);
            }
        }
    }

    /**
     * Attempt mop swing with some microstrategy. Returns if a swing was executed.
     */
    public static boolean mopSwingWithMicro() throws Exception {
        // spaghetti copy paste
        int up = 0, down = 0, left = 0, right = 0;
        RobotInfo r;
        if (G.rc.onTheMap(G.me.add(Direction.NORTH))) {
            r = G.rc.senseRobotAtLocation(G.me.add(Direction.NORTH));
            if (r != null && r.team == POI.opponentTeam)
                up++;
        }
        if (G.rc.onTheMap(G.me.add(Direction.NORTHEAST))) {
            r = G.rc.senseRobotAtLocation(G.me.add(Direction.NORTHEAST));
            if (r != null && r.team == POI.opponentTeam) {
                up++;
                right++;
            }
        }
        if (G.rc.onTheMap(G.me.add(Direction.EAST))) {
            r = G.rc.senseRobotAtLocation(G.me.add(Direction.EAST));
            if (r != null && r.team == POI.opponentTeam)
                right++;
        }
        if (G.rc.onTheMap(G.me.add(Direction.SOUTHEAST))) {
            r = G.rc.senseRobotAtLocation(G.me.add(Direction.SOUTHEAST));
            if (r != null && r.team == POI.opponentTeam) {
                down++;
                right++;
            }
        }
        if (G.rc.onTheMap(G.me.add(Direction.SOUTH))) {
            r = G.rc.senseRobotAtLocation(G.me.add(Direction.SOUTH));
            if (r != null && r.team == POI.opponentTeam)
                down++;
        }
        if (G.rc.onTheMap(G.me.add(Direction.SOUTHWEST))) {
            r = G.rc.senseRobotAtLocation(G.me.add(Direction.SOUTHWEST));
            if (r != null && r.team == POI.opponentTeam) {
                down++;
                left++;
            }
        }
        if (G.rc.onTheMap(G.me.add(Direction.WEST))) {
            r = G.rc.senseRobotAtLocation(G.me.add(Direction.WEST));
            if (r != null && r.team == POI.opponentTeam)
                left++;
        }
        if (G.rc.onTheMap(G.me.add(Direction.NORTHWEST))) {
            r = G.rc.senseRobotAtLocation(G.me.add(Direction.NORTHWEST));
            if (r != null && r.team == POI.opponentTeam) {
                up++;
                left++;
            }
        }
        if (up >= 3)
            G.rc.mopSwing(Direction.NORTH);
        else if (down >= 3)
            G.rc.mopSwing(Direction.SOUTH);
        else if (left >= 3)
            G.rc.mopSwing(Direction.WEST);
        else if (right >= 3)
            G.rc.mopSwing(Direction.EAST);
        else
            return false;
        return true;
    }
}
