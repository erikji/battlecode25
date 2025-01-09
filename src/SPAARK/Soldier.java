package SPAARK;

import battlecode.common.*;

import java.util.*;

import battlecode.schema.RobotType;

public class Soldier {
    public static MapLocation ruinLocation = null; // BUILD_TOWER mode
    public static UnitType towerType = null; // ATTACK mode
    public static MapLocation towerLocation = null; // ATTACK mode
    public static MapLocation resourceLocation = null; // BUILD_RESOURCE mode
    public static final MapLocation invalidLoc = new MapLocation(-1, -1);
    // if already lots of soldiers near a ruin that needs to be built
    public static MapLocation[] excludedRuins = new MapLocation[] { invalidLoc, invalidLoc, invalidLoc, invalidLoc,
            invalidLoc, invalidLoc, invalidLoc, invalidLoc, invalidLoc, invalidLoc };
    public static int excludedRuinIndex = 0; // rotating exclusion list

    public static final int EXPLORE = 0;
    public static final int BUILD_TOWER = 1;
    public static final int BUILD_RESOURCE = 2;
    public static final int ATTACK = 3;
    public static final int RETREAT = 4;
    public static int mode = EXPLORE;

    public static void run() throws Exception {
        // occasionally clear excluded build ruins
        if (G.rc.getRoundNum() % 50 == 0) {
            excludedRuins[0] = invalidLoc;
            excludedRuins[1] = invalidLoc;
            excludedRuins[2] = invalidLoc;
            excludedRuins[3] = invalidLoc;
            excludedRuins[4] = invalidLoc;
            excludedRuins[5] = invalidLoc;
            excludedRuins[6] = invalidLoc;
            excludedRuins[7] = invalidLoc;
            excludedRuins[8] = invalidLoc;
            excludedRuins[9] = invalidLoc;
        }
        if (G.rc.getPaint() < G.rc.getType().paintCapacity / 3) {
            mode = RETREAT;
        } else if (mode == RETREAT && G.rc.getPaint() > G.rc.getType().paintCapacity * 3 / 4) {
            mode = EXPLORE;
        }
        runMode();
    }

    public static void runMode() throws Exception {
        switch (mode) {
            case EXPLORE -> explore();
            case BUILD_TOWER -> buildTower();
            // case BUILD_RESOURCE -> buildResource();
            case ATTACK -> attack();
            case RETREAT -> {
                G.indicatorString.append("RETREAT ");
                Robot.retreat();
            }
        }
    }

    public static void explore() throws Exception {
        G.indicatorString.append("EXPLORE ");
        // switch modes if seeing towers/ruins
        MapLocation[] locs = G.rc.senseNearbyRuins(-1);
        for (int i = locs.length; --i >= 0;) {
            if (G.rc.canSenseRobotAtLocation(locs[i])) {
                RobotInfo bot = G.rc.senseRobotAtLocation(locs[i]);
                if (bot.team == G.opponentTeam
                        && bot.type.actionRadiusSquared <= G.rc.getType().actionRadiusSquared) {
                    towerLocation = locs[i];
                    towerType = bot.type;
                    mode = ATTACK;
                    runMode();
                    break;
                }
            } else if (G.rc.getNumberTowers() < 25) {
                ruinLocation = locs[i];
                mode = BUILD_TOWER;
                runMode();
                break;
            }
        }
        // find towers to attack out of vision, doesn't switch modes
        MapLocation bestLoc = null;
        int bestDistanceSquared = 10000;
        searchTowers: for (int i = 144; --i >= 0;) {
            if (POI.towers[i] == -1) {
                break;
            }
            if (POI.parseTowerTeam(POI.towers[i]) != G.team) {
                MapLocation pos = POI.parseLocation(POI.towers[i]);
                if (G.me.isWithinDistanceSquared(pos, bestDistanceSquared)) {
                    for (int j = excludedRuins.length; --j >= 0;) {
                        if (excludedRuins[i] == invalidLoc)
                            continue;
                        if (pos.equals(excludedRuins[i])) {
                            continue searchTowers;
                        }
                    }
                    bestDistanceSquared = G.me.distanceSquaredTo(pos);
                    bestLoc = pos;
                }
            }
        }
        if (bestLoc == null) {
            Motion.spreadRandomly();
        } else {
            Motion.bugnavTowards(bestLoc);
            G.rc.setIndicatorLine(G.me, bestLoc, 255, 255, 0);
        }
        MapInfo me = G.rc.senseMapInfo(G.me);
        // place paint under self to avoid passive paint drain if possible
        if (me.getPaint() == PaintType.EMPTY && G.rc.canAttack(G.me)) {
            G.rc.attack(G.me);
        }
        // see if possible to build resource pattern here (or nearby)
        G.rc.setIndicatorDot(G.me, 0, 255, 0);
    }

    public static void buildTower() throws Exception {
        G.indicatorString.append("BUILD_T ");
        // if lots of soldiers nearby or tower already built leave build tower mode
        if (!G.me.isWithinDistanceSquared(ruinLocation, 8)) {
            // don't leave the tower if you're close to the tower
            int existingSoldiers = 0;
            for (int i = G.allyRobots.length; --i >= 0;) {
                if (G.allyRobots[i].type == UnitType.SOLDIER
                        && G.allyRobots[i].location.isWithinDistanceSquared(ruinLocation, 8)) {
                    existingSoldiers++;
                }
            }
            if (existingSoldiers > 3) {
                mode = EXPLORE;
                excludedRuins[excludedRuinIndex = (excludedRuinIndex + 1) % excludedRuins.length] = ruinLocation;
                ruinLocation = null;
                runMode();
            }
        }
        if (!G.rc.canSenseLocation(ruinLocation) || G.rc.canSenseRobotAtLocation(ruinLocation)) {
            mode = EXPLORE;
            excludedRuins[excludedRuinIndex = (excludedRuinIndex + 1) % excludedRuins.length] = ruinLocation;
            ruinLocation = null;
            runMode();
        } else {
            MapInfo[] infos = G.rc.senseNearbyMapInfos(UnitType.SOLDIER.actionRadiusSquared);
            MapLocation paintLocation = null; // so indicator drawn to bot instead of previous position
            for (int i = infos.length; --i >= 0;) {
                if (G.me.isWithinDistanceSquared(infos[i].getMapLocation(),
                        UnitType.SOLDIER.actionRadiusSquared)
                        && infos[i].getMapLocation().isWithinDistanceSquared(ruinLocation, 8)) {
                    int dx = infos[i].getMapLocation().x - ruinLocation.x + 2;
                    int dy = infos[i].getMapLocation().y - ruinLocation.y + 2;
                    if (dx != 2 || dy != 2) {
                        boolean paint = Robot.towerPatterns[predictTowerType(ruinLocation)][dx][dy];
                        if (G.rc.canAttack(infos[i].getMapLocation()) && (infos[i].getPaint() == PaintType.EMPTY
                                || infos[i].getPaint() == (paint ? PaintType.ALLY_PRIMARY
                                        : PaintType.ALLY_SECONDARY))) {
                            G.rc.attack(infos[i].getMapLocation(), paint);
                            paintLocation = infos[i].getMapLocation();
                            break;
                        }
                    }
                }
            }
            int t = predictTowerType(ruinLocation);
            if (G.rc.canCompleteTowerPattern(Robot.towers[t], ruinLocation) && G.rc.getPaint() > 50) {
                G.rc.completeTowerPattern(Robot.towers[t], ruinLocation);
                POI.addTower(-1,
                        POI.intifyTower(G.team, Robot.towers[t]) | POI.intifyLocation(ruinLocation));
                mode = EXPLORE;
                ruinLocation = null;
                Motion.bugnavTowards(G.mapCenter);
            } else {
                Motion.bugnavAround(ruinLocation, 1, 2);
            }
            if (paintLocation != null)
                G.rc.setIndicatorLine(G.me, paintLocation, 0, 255, 255);
        }
        G.rc.setIndicatorLine(G.rc.getLocation(), ruinLocation, 255, 255, 0);
        G.rc.setIndicatorDot(G.me, 0, 0, 255);
    }

    public static void attack() throws Exception {
        G.indicatorString.append("ATTACK ");
        // attack micro moment
        if (towerLocation.isWithinDistanceSquared(G.me, towerType.actionRadiusSquared)) {
            if (G.rc.canAttack(towerLocation))
                G.rc.attack(towerLocation);
            Motion.bugnavAway(towerLocation, attackMicro);
        } else {
            if (G.rc.isActionReady()) {
                Motion.bugnavTowards(towerLocation, attackMicro);
                if (G.rc.canAttack(towerLocation))
                    G.rc.attack(towerLocation);
            } else {
                Motion.bugnavAround(towerLocation, towerType.actionRadiusSquared + 1,
                        towerType.actionRadiusSquared + 1);
            }
        }
    }

    public static Micro attackMicro = new Micro() {
        @Override
        public int[] micro(Direction d, MapLocation dest) throws Exception {
            // try to stay out of range if on cd, otherwise try to get in range
            Direction best = d;
            int bestScore = Integer.MIN_VALUE;
            int scores[] = new int[8];
            if (G.rc.isActionReady()) {
                for (int i = 8; --i >= 0;) {
                    if (!G.rc.canMove(G.DIRECTIONS[i])) {
                        scores[i] = 0;
                        continue;
                    }
                    int score = 0;
                    MapLocation nxt = G.me.add(G.DIRECTIONS[i]);
                    MapInfo info = G.rc.senseMapInfo(nxt);
                    if (info.getPaint().isEnemy())
                        score -= 10;
                    else if (info.getPaint() == PaintType.EMPTY)
                        score -= 5;
                    if (G.DIRECTIONS[i] == d) {
                        score += 20;
                    } else if (G.DIRECTIONS[i].rotateLeft() == d || G.DIRECTIONS[i].rotateRight() == d) {
                        score += 16;
                    }
                    if (nxt.isWithinDistanceSquared(towerLocation, G.rc.getType().actionRadiusSquared)) {
                        score += 40;
                    }
                    if (score > bestScore) {
                        best = G.DIRECTIONS[i];
                        bestScore = score;
                    }
                    scores[i] = score;
                }
            } else {
                for (int i = 8; --i >= 0;) {
                    if (!G.rc.canMove(G.DIRECTIONS[i])) {
                        scores[i] = 0;
                        continue;
                    }
                    int score = 0;
                    MapLocation nxt = G.me.add(G.DIRECTIONS[i]);
                    MapInfo info = G.rc.senseMapInfo(nxt);
                    if (info.getPaint().isEnemy())
                        score -= 10;
                    else if (info.getPaint() == PaintType.EMPTY)
                        score -= 5;
                    if (G.DIRECTIONS[i] == d) {
                        score += 20;
                    } else if (G.DIRECTIONS[i].rotateLeft() == d || G.DIRECTIONS[i].rotateRight() == d) {
                        score += 16;
                    }
                    if (!nxt.isWithinDistanceSquared(towerLocation, towerType.actionRadiusSquared)) {
                        score += 40;
                    }
                    if (score > bestScore) {
                        best = G.DIRECTIONS[i];
                        bestScore = score;
                    }
                    scores[i] = score;
                }
            }
            Motion.move(best);
            return scores;
        }
    };

    public static int predictTowerType(MapLocation xy) {
        Random rng = new Random(xy.x * 0x67f176e2 + xy.y);
        return rng.nextInt(10) > 5 ? 2 : 1;
    }
}