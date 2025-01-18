package TSPAARKJAN09;

import battlecode.common.*;
import java.util.Random;

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

    // allowed marker locations
    // fills entire vision range
    public static final boolean[][] allowedSrpMarkerLocations = new boolean[][] {
            { false, false, false, true, false, true, false, false, false },
            { false, false, true, false, true, false, true, false, false },
            { false, true, false, false, false, false, false, true, false },
            { true, false, false, false, false, false, false, false, true },
            { false, true, false, false, false, false, false, true, false },
            { true, false, false, false, false, false, false, false, true },
            { false, true, false, false, false, false, false, true, false },
            { false, false, true, false, true, false, true, false, false },
            { false, false, false, true, false, true, false, false, false },
    };

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
        switch (mode) {
            case EXPLORE -> exploreCheckMode();
            case BUILD_TOWER -> buildTowerCheckMode();
            case BUILD_RESOURCE -> buildResourceCheckMode();
            case ATTACK -> attackCheckMode();
        }
        switch (mode) {
            case EXPLORE -> explore();
            case BUILD_TOWER -> buildTower();
            case BUILD_RESOURCE -> buildResource();
            case ATTACK -> attack();
            case RETREAT -> {
                G.indicatorString.append("RETREAT ");
                Robot.retreat();
            }
        }
    }

    public static void exploreCheckMode() throws Exception {
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
                    return;
                }
            } else if (G.rc.getNumberTowers() < 25) {
                ruinLocation = locs[i];
                mode = BUILD_TOWER;
                return;
            }
        }
        // search for SRP markers
        // : BOT SEARCHES FOR MARKERS AND HELPS BUILD IF NO OTHER BOTS BUILDING
        // : ALSO REPAIRING
        // : EXTRAPOLATE EXISTING PATTERNS TO PERFECTLY TILE - MORE EFFICIENT
        // don't make it remove and rebuild patterns that interfere?
        // see if SRP on current square is possible
        boolean canBuildSrpHere = true;
        // definitely can try more spots but also bytecode limits
        // make sure 5x5 square clear first
        clearCheck: for (int i = -3; ++i <= 2;) {
            for (int j = -3; ++j <= 2;) {
                MapLocation loc = G.me.translate(j, i);
                if (!G.rc.onTheMap(loc) || !G.rc.sensePassability(loc)) {
                    canBuildSrpHere = false;
                    break clearCheck;
                }
            }
        }
        MapInfo[] infos = G.rc.senseNearbyMapInfos();
        for (int i = infos.length; --i >= 0;) {
            // can't have markers without spots but can have spots without markers
            if ((infos[i].getMark() == PaintType.ALLY_SECONDARY)) {
                MapLocation loc = infos[i].getMapLocation();
                if (!allowedSrpMarkerLocations[loc.y - G.me.y + 4][loc.x - G.me.x + 4]) {
                    // try shifting the pattern? very complicated and lots of code, not worth it
                    canBuildSrpHere = false;
                    break;
                }
            }
        }
        if (canBuildSrpHere) {
            // place markers
            resourceLocation = G.me;
            G.rc.mark(G.me.add(Direction.NORTH), true);
            G.rc.mark(G.me.add(Direction.EAST), true);
            G.rc.mark(G.me.add(Direction.SOUTH), true);
            G.rc.mark(G.me.add(Direction.WEST), true);
            G.indicatorString.append("MK_SRP ");
            mode = BUILD_RESOURCE;
        }
    }

    public static void buildTowerCheckMode() throws Exception {
        // if lots of soldiers nearby or tower already built leave build tower mode
        // don't leave the tower if you're close to the tower
        if (!G.me.isWithinDistanceSquared(ruinLocation, 8)) {
            int existingSoldiers = 0;
            for (int i = G.allyRobots.length; --i >= 0;) {
                if (G.allyRobots[i].type == UnitType.SOLDIER
                        && G.allyRobots[i].location.isWithinDistanceSquared(ruinLocation, 8)) {
                    existingSoldiers++;
                }
            }
            if (existingSoldiers > 4) {
                mode = EXPLORE;
                excludedRuins[excludedRuinIndex = (excludedRuinIndex + 1) % excludedRuins.length] = ruinLocation;
                ruinLocation = null;
                return;
            }
        }
        if (!G.rc.canSenseLocation(ruinLocation) || G.rc.canSenseRobotAtLocation(ruinLocation)
                || G.rc.getNumberTowers() == 25) {
            mode = EXPLORE;
            excludedRuins[excludedRuinIndex = (excludedRuinIndex + 1) % excludedRuins.length] = ruinLocation;
            ruinLocation = null;
        }
    }

    public static void buildResourceCheckMode() throws Exception {
        // if lots of soldiers nearby or resource pattern already built leave
        // don't leave if you're close
        if (!G.me.isWithinDistanceSquared(resourceLocation, 8)) {
            int existingSoldiers = 0;
            for (int i = G.allyRobots.length; --i >= 0;) {
                if (G.allyRobots[i].type == UnitType.SOLDIER
                        && G.allyRobots[i].location.isWithinDistanceSquared(resourceLocation, 4)) {
                    existingSoldiers++;
                }
            }
            if (existingSoldiers > 3) {
                mode = EXPLORE;
                resourceLocation = null;
            }
        }
        // POSSIBLY HAVE TO REMOVE MARKERS IF INTERFERING?
        // shouldn't happen though?
    }

    public static void attackCheckMode() throws Exception {
        // nothing for now
    }

    public static void explore() throws Exception {
        G.indicatorString.append("EXPLORE ");
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
                        if (excludedRuins[j] == invalidLoc)
                            continue;
                        if (pos.equals(excludedRuins[j])) {
                            continue searchTowers;
                        }
                    }
                    bestDistanceSquared = G.me.distanceSquaredTo(pos);
                    bestLoc = pos;
                }
            }
        }
        if (bestLoc == null) {
            Motion.exploreRandomly();
        } else {
            Motion.bugnavTowards(bestLoc);
            // // G.rc.setIndicatorLine(G.me, bestLoc, 255, 255, 0);
        }
        MapInfo me = G.rc.senseMapInfo(G.me);
        // place paint under self to avoid passive paint drain if possible
        if (me.getPaint() == PaintType.EMPTY && G.rc.canAttack(G.me)) {
            G.rc.attack(G.me);
        }
        // // G.rc.setIndicatorDot(G.me, 0, 255, 0);
    }

    public static void buildTower() throws Exception {
        G.indicatorString.append("BUILD_TW ");
        MapInfo[] infos = G.rc.senseNearbyMapInfos(UnitType.SOLDIER.actionRadiusSquared);
        int t = predictTowerType(ruinLocation);
        // MapLocation paintLocation = null; // so indicator drawn to bot instead of previous position
        for (int i = infos.length; --i >= 0;) {
            MapLocation loc = infos[i].getMapLocation();
            if (G.me.isWithinDistanceSquared(loc, UnitType.SOLDIER.actionRadiusSquared)
                    && loc.isWithinDistanceSquared(ruinLocation, 8)) {
                int dx = loc.x - ruinLocation.x + 2;
                int dy = loc.y - ruinLocation.y + 2;
                if (dx != 2 || dy != 2) {
                    boolean paint = Robot.towerPatterns[t][dx][dy];
                    PaintType exist = infos[i].getPaint();
                    if (G.rc.canAttack(loc) && (exist == PaintType.EMPTY
                            || exist == (paint ? PaintType.ALLY_PRIMARY : PaintType.ALLY_SECONDARY))) {
                        G.rc.attack(loc, paint);
                        // paintLocation = loc;
                        break;
                    }
                }
            }
        }
        // G.indicatorString.append(t);
        // G.indicatorString.append(ruinLocation.toString());
        // G.indicatorString.append(G.rc.canCompleteTowerPattern(Robot.towers[t],
        // ruinLocation));
        // G.indicatorString.append(G.rc.getPaint());
        if (G.rc.canCompleteTowerPattern(Robot.towers[t], ruinLocation) && G.rc.getPaint() > 50) {
            G.rc.completeTowerPattern(Robot.towers[t], ruinLocation);
            POI.addTower(-1,
                    POI.intifyTower(G.team, Robot.towers[t]) | POI.intifyLocation(ruinLocation));
            mode = EXPLORE;
            Motion.exploreRandomly();
            // // G.rc.setIndicatorDot(ruinLocation, 255, 200, 0);
            ruinLocation = null;
        } else {
            Motion.bugnavAround(ruinLocation, 1, 2);
            // // G.rc.setIndicatorLine(G.rc.getLocation(), ruinLocation, 255, 200, 0);
        }
        // if (paintLocation != null)
            // // G.rc.setIndicatorLine(G.me, paintLocation, 200, 100, 0);
        // // G.rc.setIndicatorDot(G.me, 0, 0, 255);
    }

    public static void buildResource() throws Exception {
        G.indicatorString.append("BUILD_RP ");
        // MUCH IS IDENTICAL TO TOWER BUILD CODE
        MapInfo[] infos = G.rc.senseNearbyMapInfos(UnitType.SOLDIER.actionRadiusSquared);
        // MapLocation paintLocation = null; // so indicator drawn to bot instead of previous position
        for (int i = infos.length; --i >= 0;) {
            MapLocation loc = infos[i].getMapLocation();
            if (G.me.isWithinDistanceSquared(loc, UnitType.SOLDIER.actionRadiusSquared)
                    && loc.isWithinDistanceSquared(resourceLocation, 8)) {
                boolean paint = Robot.resourcePattern[loc.x - resourceLocation.x + 2][loc.y - resourceLocation.y + 2];
                PaintType exist = infos[i].getPaint();
                if (G.rc.canAttack(loc) && (exist == PaintType.EMPTY
                        || exist == (paint ? PaintType.ALLY_PRIMARY : PaintType.ALLY_SECONDARY))) {
                    G.rc.attack(loc, paint);
                    // paintLocation = loc;
                    break;
                }
            }
        }
        if (G.rc.canCompleteResourcePattern(resourceLocation) && G.rc.getPaint() > 50) {
            G.rc.completeResourcePattern(resourceLocation);
            mode = EXPLORE;
            Motion.exploreRandomly();
            // // G.rc.setIndicatorDot(resourceLocation, 255, 200, 0);
            resourceLocation = null;
        } else {
            Motion.bugnavAround(resourceLocation, 0, 2);
            // // G.rc.setIndicatorLine(G.rc.getLocation(), resourceLocation, 255, 100, 0);
        }
        // if (paintLocation != null)
            // // G.rc.setIndicatorLine(G.me, paintLocation, 200, 100, 0);
        // // G.rc.setIndicatorDot(G.me, 0, 200, 255);
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
        // // G.rc.setIndicatorDot(G.me, 255, 0, 0);
    }

    public static Micro attackMicro = new Micro() {
        @Override
        public int[] micro(Direction d, MapLocation dest) throws Exception {
            // try to stay out of range if on cd, otherwise try to get in range
            int[] scores = Motion.defaultMicro.micro(d, dest);
            if (G.rc.isActionReady()) {
                for (int i = 8; --i >= 0;) {
                    if (G.me.add(G.DIRECTIONS[i]).isWithinDistanceSquared(towerLocation,
                            G.rc.getType().actionRadiusSquared)) {
                        scores[i] += 40;
                    }
                }
            } else {
                for (int i = 8; --i >= 0;) {
                    if (!G.me.add(G.DIRECTIONS[i]).isWithinDistanceSquared(towerLocation,
                            towerType.actionRadiusSquared)) {
                        scores[i] += 40;
                    }
                }
            }
            return scores;
        }
    };

    public static int predictTowerType(MapLocation xy) {
        Random rng = new Random(xy.x * 0x67f176e2 + xy.y);
        return rng.nextInt(10) > 5 ? 2 : 1;
    }
}