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
            // make sure not stuck between exploring and building
            if (lastBuild + 10 < G.rc.getRoundNum()) {
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
        MapInfo[] mapInfos = G.rc.senseNearbyMapInfos();
        // will try to unpaint squares under opponent bots
        // but if no opponents, just move to paint and attack
        //store both the best empty tile and the best opponent bot tile
        MapLocation bestEmpty = null;
        MapLocation bestBot = null;
        double bestPaint = -1;
        for (int i = mapInfos.length; --i >= 0;) {
            MapInfo info = mapInfos[i];
            MapLocation loc = info.getMapLocation();
            PaintType p = info.getPaint();
            if (G.rc.canSenseRobotAtLocation(loc)) {
                if (!p.isAlly() && p != PaintType.EMPTY) {
                    RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                    if (bot.getType() == UnitType.MOPPER || bot.getType() == UnitType.SOLDIER || bot.getType() == UnitType.SPLASHER) {
                        double paint = bot.paintAmount / (double) bot.type.paintCapacity;
                        if (paint > bestPaint) {
                            bestPaint = paint;
                            bestBot = loc;
                        }
                    }
                }
            }
            if (!p.isAlly() && p != PaintType.EMPTY && (bestEmpty == null ||G.me.distanceSquaredTo(loc) < G.me.distanceSquaredTo(bestEmpty))) {
                bestEmpty = info.getMapLocation();
            }
        }
        if (bestBot != null) {
            Motion.bugnavAround(bestBot, 0, 1);
            if (G.rc.canAttack(bestBot)) {
                G.rc.attack(bestBot);
                G.rc.setIndicatorLine(G.me, bestBot, 255, 255, 255);
            }
        } else if (bestEmpty != null) {
            Motion.bugnavTowards(bestEmpty);
            if (G.rc.canAttack(bestEmpty)) {
                G.rc.canAttack(bestEmpty);
                G.rc.setIndicatorLine(G.me, bestEmpty, 128, 128, 128);
            }
        } else {
            Motion.spreadRandomly();
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
                if (info.getMapLocation().distanceSquaredTo(ruinLocation) <= 8 && !info.getPaint().isAlly()
                        && info.getPaint() != PaintType.EMPTY) {
                    // necessarily opponent paint
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
        float up = 0, down = 0, left = 0, right = 0;
        int up2 = 0, down2 = 0, left2 = 0, right2 = 0;
        RobotInfo r;
        if (G.rc.onTheMap(G.me.add(Direction.NORTH))) {
            r = G.rc.senseRobotAtLocation(G.me.add(Direction.NORTH));
            if (r != null && r.team == POI.opponentTeam) {
                up += r.paintAmount / (float) r.type.paintCapacity;
                up2++;
            }
        }
        if (G.rc.onTheMap(G.me.add(Direction.NORTHEAST))) {
            r = G.rc.senseRobotAtLocation(G.me.add(Direction.NORTHEAST));
            if (r != null && r.team == POI.opponentTeam) {
                up += r.paintAmount / (float) r.type.paintCapacity;
                right += r.paintAmount / (float) r.type.paintCapacity;
                up2++;
                right2++;
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
