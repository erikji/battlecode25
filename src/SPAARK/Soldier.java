package SPAARK;

import battlecode.common.*;

public class Soldier {
    public static MapLocation ruinLocation = null; // BUILD_TOWER mode
    public static int buildTowerType = 0;
    public static UnitType towerType = null; // ATTACK mode
    public static MapLocation towerLocation = null; // ATTACK mode
    public static MapLocation resourceLocation = null; // BUILD_RESOURCE mode

    // allowed marker locations
    // fills entire vision range
    public static final boolean[][] allowedSrpMarkerLocations = new boolean[][] {
            { false, false, false, false, false, false, false, false, false, false, false },
            { false, false, false, true, false, true, false, true, false, false, false },
            { false, false, true, false, true, false, true, false, true, false, false },
            { false, true, false, true, false, true, false, true, false, true, false },
            { false, false, true, false, false, false, false, false, true, false, false },
            { false, true, false, true, false, false, false, true, false, true, false },
            { false, false, true, false, false, false, false, false, true, false, false },
            { false, true, false, true, false, true, false, true, false, true, false },
            { false, false, true, false, true, false, true, false, true, false, false },
            { false, false, false, true, false, true, false, true, false, false, false },
            { false, false, false, false, false, false, false, false, false, false, false },
    };
    // queue of next locations to check for expanding SRP
    // used in explore mode to mark initial build since has to be in center to place
    // markers
    // (goes into expand mode, reaches the target location, and starts building)
    public static MapLocation[] srpCheckLocations = new MapLocation[] {};
    public static int srpCheckIndex = 0;

    public static final int EXPLORE = 0;
    public static final int BUILD_TOWER = 1;
    public static final int BUILD_RESOURCE = 2;
    public static final int EXPAND_RESOURCE = 3;
    public static final int ATTACK = 4;
    public static final int RETREAT = 5;
    public static int mode = EXPLORE;
    // controls round between visiting ruins (G.lastVisited)
    public static final int VISIT_TIMEOUT = 75;
    public static final int MIN_SRP_ROUND = 50;

    public static MapLocation[] nearbyRuins;

    /**
     * Always:
     * If low on paint, retreat
     * Default to explore mode
     * 
     * Explore:
     * Run around randomly while painting below self, maybe check sus POI stuff
     * If seeing opponent tower or ruin, go to attack/tower build mode
     * - tower build mode then checks if actually needed, may switch back to explore
     * If can build SRP nearby (adjacent), queue location and enter SRP expand mode
     * - SRPs won't be built overlapping with tower 5x5 squares
     * Attempt to repair SRPs
     * 
     * Build tower:
     * If pattern is complete but tower not completed, leave lowest ID to complete
     * Place stuff
     * Complete tower, return to explore
     * 
     * Build SRP:
     * Place SRP
     * Complete SRP, queue 4 optimal expansion locations and enter SRP expand mode
     * 
     * Expand SRP:
     * Go to queued locations of SRP expansion and see if can build (race condition
     * thing)
     * Enter SRP build mode
     * 
     * Attack:
     * Attack tower until ded lmao
     * Attempt to repair SRPs if not in range to attack tower
     */
    public static void run() throws Exception {
        if (G.rc.getPaint() < Robot.getRetreatPaint()) {
            mode = RETREAT;
        } else if (mode == RETREAT && G.rc.getPaint() > G.rc.getType().paintCapacity * 3 / 4) {
            mode = EXPLORE;
        }
        nearbyRuins = G.rc.senseNearbyRuins(-1);
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
        G.indicatorString.append("CHK_E ");
        // switch modes if seeing towers/ruins
        for (int i = nearbyRuins.length; --i >= 0;) {
            MapLocation loc = nearbyRuins[i];
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                if (bot.team == G.opponentTeam
                        && bot.type.actionRadiusSquared <= G.rc.getType().actionRadiusSquared) {
                    towerLocation = loc;
                    towerType = bot.type;
                    mode = ATTACK;
                    return;
                }
            } else if (G.rc.getNumberTowers() < 25) {
                ruinLocation = loc;
                mode = BUILD_TOWER;
                // if the tower doesn't need more help it'll return to explore mode
                buildTowerCheckMode();
                return;
            }
        }
        // search for SRP markers for repairing/building/expanding
        // TODO: BOT SEARCHES FOR MARKERS AND HELPS BUILD IF NO OTHER BOTS BUILDING
        // TODO: ALSO REPAIRING (enter build mode if is messed up)
        // TODO: EXTRAPOLATE EXISTING PATTERNS TO PERFECTLY TILE - MORE EFFICIENT
        // don't make it remove and rebuild patterns that interfere?
        // see if SRP is possible nearby
        // for (int i = 8; --i >= 0;) {
        // MapLocation loc = G.me.add(G.ALL_DIRECTIONS[i]);
        // if (canBuildSRPHere(loc)) {
        // // TOOD: prioritize lining up checkerboards
        // srpCheckLocations = new MapLocation[] { loc };
        // mode = EXPAND_RESOURCE;
        // break;
        // }
        // }
        if (G.rc.getRoundNum() > MIN_SRP_ROUND && canBuildSRPHere(G.me)) {
            // place markers
            resourceLocation = G.me;
            G.rc.mark(G.me.add(Direction.NORTHWEST), true);
            G.rc.mark(G.me.add(Direction.NORTHEAST), true);
            G.rc.mark(G.me.add(Direction.SOUTHWEST), true);
            G.rc.mark(G.me.add(Direction.SOUTHEAST), true);
            G.indicatorString.append("MK_SRP ");
            mode = BUILD_RESOURCE;
        }
    }

    public static void buildTowerCheckMode() throws Exception {
        G.indicatorString.append("CHK_BTW ");
        buildTowerType = predictTowerType(ruinLocation);
        // if tower already built leave tower build mode
        if (!G.rc.canSenseLocation(ruinLocation) || G.rc.canSenseRobotAtLocation(ruinLocation)
                || G.rc.getNumberTowers() == 25) {
            mode = EXPLORE;
            ruinLocation = null;
            return;
        }
        // if pattern complete leave lowest bot ID to complete
        boolean isPatternComplete = true;
        // do this instead of iterating through nearby map infos
        checkPattern: for (int dx = 0; dx++ < 4;) {
            for (int dy = 0; dy++ < 4;) {
                if (dx == 2 && dy == 2)
                    continue;
                MapLocation loc = ruinLocation.translate(dx - 2, dy - 2);
                // location guaranteed to be on the map, unless ruinLocation isn't a ruin
                // also guaranteed can be sensed if can action there
                if (G.rc.canSenseLocation(loc)
                        && (Robot.towerPatterns[buildTowerType][dx][dy] ? PaintType.ALLY_SECONDARY
                                : PaintType.ALLY_PRIMARY) != G.rc.senseMapInfo(loc).getPaint()) {
                    isPatternComplete = false;
                    break checkPattern;
                }
            }
        }
        if (isPatternComplete) {
            for (int i = G.allyRobots.length; --i >= 0;) {
                if (G.allyRobots[i].getLocation().isWithinDistanceSquared(ruinLocation, 8)) {
                    if (G.allyRobots[i].ID < G.rc.getID()) {
                        // not lowest ID, leave
                        mode = EXPLORE;
                        ruinLocation = null;
                        return;
                    }
                }
            }
        }
    }

    public static void buildResourceCheckMode() throws Exception {
        G.indicatorString.append("CHK_BRP ");
        // POSSIBLY HAVE TO REMOVE MARKERS IF INTERFERING?
        // shouldn't happen though?
    }

    public static void expandResourceCheckMode() throws Exception {
        G.indicatorString.append("CHK_ERP ");
        // if can see optimal queued location and can't build SRP set to go to next
        // location
        // if queue empty go back to explore mode
    }

    public static void attackCheckMode() throws Exception {
        G.indicatorString.append("CHK_ATK ");
        // nothing for now
    }

    public static void explore() throws Exception {
        G.indicatorString.append("EXPLORE ");
        // find towers from POI to attack/build out of vision
        MapLocation bestLoc = null;
        int bestDistanceSquared = 10000;
        for (int i = 144; --i >= 0;) {
            if (POI.towers[i] == -1) {
                break;
            }
            if (POI.parseTowerTeam(POI.towers[i]) == G.opponentTeam) {
                MapLocation pos = POI.parseLocation(POI.towers[i]);
                if (G.me.isWithinDistanceSquared(pos, bestDistanceSquared)
                        && G.lastVisited[pos.y][pos.x] + 75 < G.rc.getRoundNum()) {
                    bestDistanceSquared = G.me.distanceSquaredTo(pos);
                    bestLoc = pos;
                }
            } else if (POI.parseTowerTeam(POI.towers[i]) == Team.NEUTRAL) {
                MapLocation pos = POI.parseLocation(POI.towers[i]);
                // prioritize opponent towers more than neutral towers, so it has to be REALLY
                // close
                if (G.me.isWithinDistanceSquared(pos, bestDistanceSquared / 5)
                        && G.lastVisited[pos.y][pos.x] + 75 < G.rc.getRoundNum()) {
                    bestDistanceSquared = G.me.distanceSquaredTo(pos) * 5; // lol
                    bestLoc = pos;
                }
            }
        }
        if (bestLoc == null) {
            Motion.exploreRandomly();
        } else {
            Motion.bugnavTowards(bestLoc);
            G.rc.setIndicatorLine(G.me, bestLoc, 255, 255, 0);
        }
        MapInfo me = G.rc.senseMapInfo(G.me);
        // place paint under self to avoid passive paint drain if possible
        if (me.getPaint() == PaintType.EMPTY && G.rc.canAttack(G.me)) {
            // determine which checkerboard pattern to copy
            int[] cnt = new int[] { 0, 0 };
            for (int i = G.nearbyMapInfos.length; --i >= 0;) {
                if (G.nearbyMapInfos[i].getPaint() == PaintType.ALLY_SECONDARY) {
                    cnt[(G.nearbyMapInfos[i].getMapLocation().x + G.nearbyMapInfos[i].getMapLocation().y) & 1]++;
                }
            }
            G.rc.attack(G.me, cnt[(G.me.x + G.me.y) & 1] > cnt[(1 + G.me.x + G.me.y) & 1]);
        } else {
            // repair nearby SRP if possible (may use a lot of bytecode?)
            attemptRepairSRP();
        }
        G.rc.setIndicatorDot(G.me, 0, 255, 0);
    }

    public static void buildTower() throws Exception {
        G.indicatorString.append("BUILD_TW ");
        MapLocation paintLocation = null; // so indicator drawn to bot instead of previous position
        // spam paint
        for (int i = G.nearbyMapInfos.length; --i >= 0;) {
            MapLocation loc = G.nearbyMapInfos[i].getMapLocation();
            if (G.rc.canAttack(loc) && loc.isWithinDistanceSquared(ruinLocation, 8)) {
                int dx = loc.x - ruinLocation.x + 2;
                int dy = loc.y - ruinLocation.y + 2;
                if (dx != 2 || dy != 2) {
                    boolean paint = Robot.towerPatterns[buildTowerType][dx][dy];
                    PaintType exist = G.rc.senseMapInfo(loc).getPaint();
                    // can't paint enemy paint
                    // might need !exist.isEnemy()
                    if ((paint ? PaintType.ALLY_SECONDARY : PaintType.ALLY_PRIMARY) != exist) {
                        G.rc.attack(loc, paint);
                        paintLocation = loc;
                        break;
                    }
                }
            }
        }
        if (G.rc.canCompleteTowerPattern(Robot.towers[buildTowerType], ruinLocation) && G.rc.getPaint() > 50) {
            G.rc.completeTowerPattern(Robot.towers[buildTowerType], ruinLocation);
            POI.addTower(-1, POI.intifyTower(G.team, Robot.towers[buildTowerType]) | POI.intifyLocation(ruinLocation));
            mode = EXPLORE;
            Motion.exploreRandomly();
            // dot to signal building complete
            G.rc.setIndicatorDot(ruinLocation, 255, 200, 0);
            ruinLocation = null;
        } else {
            Motion.bugnavAround(ruinLocation, 1, 2);
            G.rc.setIndicatorLine(G.rc.getLocation(), ruinLocation, 255, 200, 0);
        }
        if (paintLocation != null)
            G.rc.setIndicatorLine(G.me, paintLocation, 200, 100, 0);
        G.rc.setIndicatorDot(G.me, 0, 0, 255);
    }

    public static void buildResource() throws Exception {
        G.indicatorString.append("BUILD_RP ");
        // MUCH IS IDENTICAL TO TOWER BUILD CODE
        MapLocation paintLocation = null; // so indicator drawn to bot instead of previous position
        // do this instead of iterating through nearby map infos
        for (int dx = 0; dx++ < 4;) {
            for (int dy = 0; dy++ < 4;) {
                if (dx == 2 && dy == 2)
                    continue;
                MapLocation loc = resourceLocation.translate(dx - 2, dy - 2);
                // location guaranteed to be on the map, unless ruinLocation isn't a ruin
                // also guaranteed can be sensed if can action there
                if (G.rc.canAttack(loc)) {
                    boolean paint = Robot.resourcePattern[dx][dy];
                    PaintType exist = G.rc.senseMapInfo(loc).getPaint();
                    if (exist == PaintType.EMPTY
                            || exist == (paint ? PaintType.ALLY_PRIMARY : PaintType.ALLY_SECONDARY)) {
                        G.rc.attack(loc, paint);
                        paintLocation = loc;
                        break;
                    }
                }
            }
        }
        for (int i = G.nearbyMapInfos.length; --i >= 0;) {
            MapLocation loc = G.nearbyMapInfos[i].getMapLocation();
            if (G.me.isWithinDistanceSquared(loc, UnitType.SOLDIER.actionRadiusSquared)
                    && loc.isWithinDistanceSquared(resourceLocation, 8)) {
                boolean paint = Robot.resourcePattern[loc.x - resourceLocation.x + 2][loc.y - resourceLocation.y + 2];
                PaintType exist = G.nearbyMapInfos[i].getPaint();
                if (G.rc.canAttack(loc) && (exist == PaintType.EMPTY
                        || exist == (paint ? PaintType.ALLY_PRIMARY : PaintType.ALLY_SECONDARY))) {
                    G.rc.attack(loc, paint);
                    paintLocation = loc;
                    break;
                }
            }
        }
        if (G.rc.canCompleteResourcePattern(resourceLocation) && G.rc.getPaint() > 50) {
            G.rc.completeResourcePattern(resourceLocation);
            // TODO: PUT BOT INTO "EXPAND_RP" MODE THAT TRIES TO EXPAND PATTERN
            // put the 4 optimal locations of next pattern into a queue
            // that the bot then pathfinds to and checks if can build pattern
            mode = EXPLORE;
            Motion.exploreRandomly();
            // dot to signal building complete
            G.rc.setIndicatorDot(resourceLocation, 255, 200, 0);
            resourceLocation = null;
        } else {
            Motion.bugnavAround(resourceLocation, 0, 2);
            G.rc.setIndicatorLine(G.rc.getLocation(), resourceLocation, 255, 100, 0);
        }
        if (paintLocation != null)
            G.rc.setIndicatorLine(G.me, paintLocation, 200, 100, 0);
        G.rc.setIndicatorDot(G.me, 0, 200, 255);
    }

    public static void expandResource() throws Exception {
        // go to queued optimal locations
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
        G.rc.setIndicatorDot(G.me, 255, 0, 0);
    }

    /**
     * Can build map at location - DOES NOT CHECK IF SENSE LOCATIONS ARE VALID!!!
     * Only call this with adjacent locations to bot!!! (any 8 cardinal directions)
     */
    public static boolean canBuildSRPHere(MapLocation me) throws Exception {
        // make sure 5x5 square clear first
        for (int i = -3; ++i <= 2;) {
            for (int j = -3; ++j <= 2;) {
                MapLocation loc = me.translate(j, i);
                if (!G.rc.onTheMap(loc) || !G.rc.sensePassability(loc))
                    return false;
            }
        }
        // check for towers that could interfere
        for (int i = nearbyRuins.length; --i >= 0;) {
            if (Math.abs(G.me.x - nearbyRuins[i].x) <= 4 && Math.abs(G.me.y - nearbyRuins[i].y) <= 4)
                return false;
        }
        // check meshing with nearby SRPs
        for (int i = G.nearbyMapInfos.length; --i >= 0;) {
            // can't have markers without spots but can have spots without markers
            if ((G.nearbyMapInfos[i].getMark() == PaintType.ALLY_SECONDARY)) {
                MapLocation loc = G.nearbyMapInfos[i].getMapLocation();
                if (!allowedSrpMarkerLocations[loc.y - me.y + 5][loc.x - me.x + 5])
                    return false;
            }
        }
        return true;
    }

    public static void attemptRepairSRP() throws Exception {
        // TODO: check nearbyMapInfos for SRP markers and repair
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
        if (G.rc.getNumberTowers() % 2 == 1)
            return 2;
        return 1;
    }
}