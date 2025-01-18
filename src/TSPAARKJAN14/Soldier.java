package TSPAARKJAN14;

import battlecode.common.*;

public class Soldier {
    public static final int EXPLORE = 0;
    public static final int BUILD_TOWER = 1;
    public static final int BUILD_RESOURCE = 2;
    public static final int EXPAND_RESOURCE = 3;
    public static final int ATTACK = 4;
    public static final int RETREAT = 5;
    public static int mode = EXPLORE;

    // ratio of paint necessary to exit retreat mode
    public static final double RETREAT_PAINT_RATIO = 0.85;
    // ratio to reduce retreat requirement by if building tower/srp
    public static final double RETREAT_REDUCED_RATIO = 0.5;
    // exploration weight multiplier
    public static final int EXPLORE_OPP_WEIGHT = 5;
    // controls rounds between visiting ruins
    public static final int VISIT_TIMEOUT = 40;
    // stop building towers if enemy paint interferes too much
    public static final int MAX_TOWER_ENEMY_PAINT = 10;
    public static final int MAX_TOWER_BLOCKED_TIME = 30;
    // max build time, max build time if no moppers/splashers to remove paint
    public static final int MAX_TOWER_TIME = 200;
    public static final int MAX_TOWER_TIME_NO_HELP = 80;
    // don't build SRP for first few rounds, prioritize towers
    public static final int MIN_SRP_ROUND = 10;
    // controls rounds between repairing/expanding SRP
    public static final int SRP_VISIT_TIMEOUT = 20;
    // balance exploring and building SRPs (don't SRP if near target)
    public static final int SRP_EXPAND_TIMEOUT = 20;
    public static final int SRP_EXP_OVERRIDE_DIST = 36;
    // have at most TOWER_CEIL for the first TOWER_CEIL rounds, if map small
    public static final int TOWER_CEIL = 3;
    public static final int TOWER_CEIL_MAP_AREA = 1600;
    public static final int TOWER_CEIL_ROUND = 75;
    // encourages building SRPs if waiting for chips on large maps initially
    public static final int INITIAL_SRP_ALT_MAP_AREA = 1600;
    public static final int INITIAL_SRP_ALT_TOWER_CAP = 6;
    public static final int INITIAL_SRP_ALT_CHIPS = 300;
    // ignore being near ruins for SRPs for some rounds, sometimes necessary
    public static final int INITIAL_SRP_RUIN_IGNORE = 50;
    // stop building SRP if enemy paint interferes too much
    public static final int MAX_SRP_ENEMY_PAINT = 2;
    public static final int MAX_SRP_BLOCKED_TIME = 10;
    // max build time
    public static final int MAX_SRP_TIME = 50;
    // don't expand SRP if low on paint, since very slow
    public static final int EXPAND_SRP_MIN_PAINT = 75;

    public static MapLocation exploreLocation = null; // EXPLORE mode
    public static MapLocation ruinLocation = null; // BUILD_TOWER mode
    public static int buildTowerType = 0;
    public static UnitType towerType = null; // ATTACK mode
    public static MapLocation towerLocation = null; // ATTACK mode
    public static MapLocation resourceLocation = null; // BUILD_RESOURCE mode

    public static boolean reducedRetreating = false;
    public static boolean avoidRetreating = false;

    public static int buildTowerBlockedTime = 0;

    // queue of next locations to check for expanding SRP
    // used in explore mode to mark initial build since needs centered for markers
    // (goes into expand mode, reaches the target location, and starts building)
    public static MapLocation[] srpCheckLocations = new MapLocation[] {};
    public static int srpCheckIndex = 0;
    public static int lastSrpExpansion = -SRP_EXPAND_TIMEOUT;
    public static int buildSrpBlockedTime = 0;

    // commonly used stuff
    public static MapLocation[] nearbyRuins;
    // map nearby map infos into 2d array in (y, x) form
    // used for tower building, SRP detection, expansion, building
    public static MapInfo[][] mapInfos = new MapInfo[9][9];

    /**
     * Always:
     * If low on paint (reduceRetreating halves paint), retreat
     * Default to explore mode
     * - All movement not in attack mode uses special movement micro that
     * paints neutral tiles if the micro weights it high - always attack
     * BEFORE moving or may interfere
     * - Movement micro also attempts to paint empty squares to help with territory
     * expansion (just painting wherever)
     * 
     * Explore:
     * Run around randomly while painting below self, pick towers from POI
     * If seeing opponent tower or ruin, go to attack/tower build mode
     * - tower build mode then checks if actually needed, may switch back to explore
     * - exception for tower ceiling, to place SRPs to help build towers later
     * If can build or repair SRP, queue location and enter SRP expand mode
     * - SRPs won't be built overlapping with ruin 5x5 squares (but can with towers)
     * 
     * Build tower:
     * Automatically make reducedRetreating true
     * If pattern is complete but tower not completed, leave lowest ID to complete
     * - avoidRetreating true if is lowest ID, don't abandon the tower
     * Place stuff
     * If can't complete tower, low coins, few towers, large map, return to explore,
     * other bot will finish tower later (see if SRP is possible)
     * If tower pattern obstructed by enemy paint long enougn, return to explore
     * Complete tower, return to explore
     * 
     * Build SRP:
     * Automatically make reducedRetreating true
     * Place SRP
     * If SRP obstructed by enemy paint long enougn, return to explore
     * Complete SRP, queue 8 optimal expansion locations and enter SRP expand mode
     * - 4 expansions for 2 chiralities (flipped pattern too)
     * 
     * Expand SRP:
     * Go to queued locations of expansion and see if can build (race conditions)
     * If can build
     * - Place 4 secondary markers in box around center and primary marker at center
     * - Enter SRP build mode
     * 
     * Attack:
     * Attack tower until ded lmao
     * Attempt to repair SRPs if not in range to attack tower
     * 
     * Retreat:
     * Use Robot retreat
     * Try to paint under self when near tower
     */
    public static void run() throws Exception {
        if (!avoidRetreating
                && G.rc.getPaint() < Robot.getRetreatPaint() * (reducedRetreating ? RETREAT_REDUCED_RATIO : 1)) {
            mode = RETREAT;
        } else if (mode == RETREAT && G.rc.getPaint() > G.rc.getType().paintCapacity * RETREAT_PAINT_RATIO) {
            mode = EXPLORE;
        }
        nearbyRuins = G.rc.senseNearbyRuins(-1);
        // map
        int miDx = 4 - G.me.x, miDy = 4 - G.me.y;
        for (int i = G.nearbyMapInfos.length; --i >= 0;) {
            MapLocation loc = G.nearbyMapInfos[i].getMapLocation().translate(miDx, miDy);
            mapInfos[loc.y][loc.x] = G.nearbyMapInfos[i];
        }
        int a = Clock.getBytecodeNum();
        reducedRetreating = false;
        avoidRetreating = false;
        switch (mode) {
            case EXPLORE -> exploreCheckMode();
            case BUILD_TOWER -> buildTowerCheckMode();
            case BUILD_RESOURCE -> buildResourceCheckMode();
            case EXPAND_RESOURCE -> expandResourceCheckMode();
            case ATTACK -> attackCheckMode();
        }
        int b = Clock.getBytecodeNum();
        G.indicatorString.append((b - a) + " ");
        switch (mode) {
            case EXPLORE -> explore();
            case BUILD_TOWER -> buildTower();
            case BUILD_RESOURCE -> buildResource();
            case EXPAND_RESOURCE -> expandResource();
            case ATTACK -> attack();
            case RETREAT -> {
                G.indicatorString.append("RETREAT ");
                if (Robot.retreatTower != -1) {
                    if (G.me.isWithinDistanceSquared(POI.towerLocs[Robot.retreatTower], 8))
                        Robot.retreat(moveWithPaintMicro);
                    else
                        Robot.retreat();
                } else {
                    Robot.retreat();
                }
            }
        }
        G.indicatorString.append((Clock.getBytecodeNum() - b) + " ");
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
            } else if (G.rc.getNumberTowers() < 25
                    && (G.round > TOWER_CEIL_ROUND || G.rc.getNumberTowers() <= TOWER_CEIL)) {
                // TOWER_CEIL encourages building SRPs to help build more towers
                ruinLocation = loc;
                mode = BUILD_TOWER;
                // if the tower doesn't need more help it'll return to explore mode
                // need to do this because of lowest ID check - bot may leave if other bot with
                // lower ID comes, but may not swap roles to build the tower
                buildTowerCheckMode();
                return;
            }
        }
        if (lastSrpExpansion + SRP_EXPAND_TIMEOUT >= G.round
                || (exploreLocation != null && G.me.isWithinDistanceSquared(exploreLocation, SRP_EXP_OVERRIDE_DIST))) {
            G.indicatorString.append("SKIP_CHK_RP ");
        } else if (G.rc.getPaint() > EXPAND_SRP_MIN_PAINT) {
            // TODO: detect nearby SRPs? might not be needed

            // see if SRP is possible nearby
            if (G.round > MIN_SRP_ROUND) {
                for (int i = 8; --i >= 0;) {
                    MapLocation loc = G.me.add(G.ALL_DIRECTIONS[i]);
                    if (canBuildSRPAtLocation(loc) && G.getLastVisited(loc) + SRP_VISIT_TIMEOUT < G.round) {
                        srpCheckLocations = new MapLocation[] { loc };
                        srpCheckIndex = 0;
                        mode = EXPAND_RESOURCE;
                        // do this or bugs
                        expandResourceCheckMode();
                        return;
                    }
                }
            }
        }
    }

    public static void buildTowerCheckMode() throws Exception {
        G.indicatorString.append("CHK_BTW ");
        G.setLastVisited(ruinLocation, G.round);
        reducedRetreating = true;
        buildTowerType = predictTowerType(ruinLocation);
        // if tower already built leave tower build mode
        if (!G.rc.canSenseLocation(ruinLocation) || G.rc.canSenseRobotAtLocation(ruinLocation)
                || G.rc.getNumberTowers() == 25) {
            mode = EXPLORE;
            return;
        }
        // if pattern complete leave lowest bot ID to complete
        int enemyPaint = 0;
        boolean isPatternComplete = true;
        int ox = ruinLocation.x - G.me.x + 2;
        int oy = ruinLocation.y - G.me.y + 2;
        boolean[][] pattern = Robot.towerPatterns[buildTowerType];
        PaintType paint;
        PaintType curr;
        checkPattern: for (int dx = -1; dx++ < 4;) {
            for (int dy = -1; dy++ < 4;) {
                if (dx == 2 && dy == 2)
                    continue;
                paint = pattern[dx][dy] ? PaintType.ALLY_SECONDARY : PaintType.ALLY_PRIMARY;
                // make sure not out of vision radius
                if (G.rc.canSenseLocation(ruinLocation.translate(dx - 2, dy - 2))) {
                    curr = mapInfos[oy + dy][ox + dx].getPaint();
                    if (curr != paint)
                        isPatternComplete = false;
                    // stop building if there's lots of enemy paint within the SRP
                    if (curr.isEnemy()) {
                        enemyPaint++;
                        if (enemyPaint >= MAX_TOWER_ENEMY_PAINT) {
                            buildTowerBlockedTime++;
                            G.indicatorString.append("BLOCK ");
                            // if pattern has been blocked for a long time just give up
                            if (buildTowerBlockedTime > MAX_TOWER_BLOCKED_TIME) {
                                mode = EXPLORE;
                                buildTowerBlockedTime = 0; // forgot to reset
                                return;
                            }
                            break checkPattern;
                        }
                    }
                }
            }
        }
        // pattern not blocked (no return above)
        if (enemyPaint < MAX_TOWER_ENEMY_PAINT)
            buildTowerBlockedTime = 0;
        // leave only the lowest ID robot to complete the pattern
        if (isPatternComplete) {
            for (int i = G.allyRobots.length; --i >= 0;) {
                if (G.allyRobots[i].getLocation().isWithinDistanceSquared(ruinLocation, 8)) {
                    if (G.allyRobots[i].ID < G.rc.getID()) {
                        // not lowest ID, leave
                        mode = EXPLORE;
                        return;
                    }
                }
            }
            // is lowest id
            avoidRetreating = true;
            // maybe try later if chips low, towers few, and map large
            if (G.rc.getChips() <= INITIAL_SRP_ALT_CHIPS && G.rc.getNumberTowers() <= INITIAL_SRP_ALT_TOWER_CAP
                    && G.mapArea >= INITIAL_SRP_ALT_MAP_AREA) {
                mode = EXPLORE;
            }
        }
    }

    public static void buildResourceCheckMode() throws Exception {
        G.indicatorString.append("CHK_BRP ");
        reducedRetreating = true;
        // shouldn't interfere with towers here either, same as expand RP
        G.setLastVisited(resourceLocation, G.round);
        // if the SRP has been blocked for a long time just give up
        // stop building if there's lots of enemy paint within the SRP
        int enemyPaint = 0;
        int ox = resourceLocation.x - G.me.x + 2;
        int oy = resourceLocation.y - G.me.y + 2;
        for (int dx = -1; dx++ < 4;) {
            for (int dy = -1; dy++ < 4;) {
                // make sure not out of vision radius
                if (G.rc.canSenseLocation(resourceLocation.translate(dx - 2, dy - 2))
                        && mapInfos[oy + dy][ox + dx].getPaint().isEnemy()) {
                    enemyPaint++;
                    if (enemyPaint >= MAX_SRP_ENEMY_PAINT) {
                        buildSrpBlockedTime++;
                        G.indicatorString.append("BLOCK ");
                        // if pattern has been blocked for a long time just give up
                        if (buildSrpBlockedTime > MAX_SRP_BLOCKED_TIME) {
                            mode = EXPLORE;
                            buildSrpBlockedTime = 0; // forgot to reset
                        }
                        return;
                    }
                }
            }
        }
        // SRP not blocked
        buildSrpBlockedTime = 0;
        // POSSIBLY HAVE TO REMOVE MARKERS IF INTERFERING?
        // shouldn't happen though?
    }

    public static void expandResourceCheckMode() throws Exception {
        G.indicatorString.append("CHK_ERP ");
        lastSrpExpansion = G.round;
        MapLocation target = srpCheckLocations[srpCheckIndex];
        // keep disqualifying locations in a loop
        // done ASAP, don't waste time going to SRPs that can be disqualified
        while (!G.rc.onTheMap(target) || G.getLastVisited(target) + SRP_VISIT_TIMEOUT >= G.round
                || cannotBuildSRPAtLocation(target)) {
            if (G.rc.onTheMap(target))
              // G.rc.setIndicatorDot(target, 255, 100, 0);
            if (++srpCheckIndex >= srpCheckLocations.length) {
                mode = EXPLORE;
                // don't waste turns
                if (Clock.getBytecodesLeft() > 10000)
                    exploreCheckMode();
                return;
            }
            target = srpCheckLocations[srpCheckIndex];
        }
        // markers
        if (G.me.equals(target) && canBuildSRPAtLocation(G.me)) {
            resourceLocation = G.me;
            // secondary used for detection, primary marks center
            G.rc.mark(G.me.add(Direction.NORTHWEST), true);
            G.rc.mark(G.me.add(Direction.NORTHEAST), true);
            G.rc.mark(G.me.add(Direction.SOUTHWEST), true);
            G.rc.mark(G.me.add(Direction.SOUTHEAST), true);
            G.rc.mark(G.me, false);
            G.indicatorString.append("MK_SRP ");
            mode = BUILD_RESOURCE;
        }
    }

    public static void attackCheckMode() throws Exception {
        G.indicatorString.append("CHK_ATK ");
        // nothing for now
    }

    public static void explore() throws Exception {
        G.indicatorString.append("EXPLORE ");
        // here instead of checkMode since checkMode may skip this if tower/SRP checked
        // find towers from POI to attack/build out of vision
        exploreLocation = null;
        // TODO: EXPLORE BLANK AREAS AWAY FROM TOWERS AND POI
        // TODO: we leave too much map empty
        // TODO: try using random explore more
        // TODO: maybe even edit random explore to avoid known POI
        // TODO: since POI will be checked on without random
        // TOWER_CEIL encourages building SRPs to help build more towers
        if (G.round > TOWER_CEIL_ROUND || G.rc.getNumberTowers() <= TOWER_CEIL) {
            int bestDistanceSquared = 10000;
            for (int i = POI.numberOfTowers; --i >= 0;) {
                if (POI.towerTeams[i] == G.opponentTeam) {
                    // attack these
                    MapLocation pos = POI.towerLocs[i];
                    if (G.me.isWithinDistanceSquared(pos, bestDistanceSquared)
                            && G.getLastVisited(pos) + VISIT_TIMEOUT < G.round) {
                        bestDistanceSquared = G.me.distanceSquaredTo(pos);
                        exploreLocation = pos;
                    }
                } else if (POI.towerTeams[i] == Team.NEUTRAL && G.rc.getNumberTowers() < 25) {
                    // having 25 towers otherwise just softlocks the bots
                    MapLocation pos = POI.towerLocs[i];
                    // prioritize opponent towers more than ruins, so it has to be REALLY close
                    if (G.me.isWithinDistanceSquared(pos, bestDistanceSquared / EXPLORE_OPP_WEIGHT)
                            && G.getLastVisited(pos) + VISIT_TIMEOUT < G.round) {
                        bestDistanceSquared = G.me.distanceSquaredTo(pos) * EXPLORE_OPP_WEIGHT; // lol
                        exploreLocation = pos;
                    }
                }
            }
        }
        if (exploreLocation == null) {
            Motion.exploreRandomly(moveWithPaintMicro);
        } else {
            Motion.bugnavTowards(exploreLocation, moveWithPaintMicro);
          // G.rc.setIndicatorLine(G.me, exploreLocation, 255, 255, 0);
        }
      // G.rc.setIndicatorDot(G.me, 0, 255, 0);
    }

    public static void buildTower() throws Exception {
        G.indicatorString.append("BUILD_TW ");
        // MapLocation paintLocation = null;
        int ox = ruinLocation.x - G.me.x + 2;
        int oy = ruinLocation.y - G.me.y + 2;
        boolean[][] pattern = Robot.towerPatterns[buildTowerType];
        G.indicatorString.append("TYPE=" + buildTowerType + " ");
        boolean paint;
        PaintType exists;
        MapLocation loc;
        for (int dx = -1; dx++ < 4;) {
            for (int dy = -1; dy++ < 4;) {
                if (dx == 2 && dy == 2)
                    continue;
                // location guaranteed to be on the map, unless ruinLocation isn't a ruin
                // guaranteed within vision radius if can attack there
                loc = ruinLocation.translate(dx - 2, dy - 2);
                if (G.rc.canAttack(loc)) {
                    paint = pattern[dx][dy];
                    exists = mapInfos[oy + dy][ox + dx].getPaint();
                    // can't paint enemy paint
                    if (!exists.isEnemy() && (paint ? PaintType.ALLY_SECONDARY : PaintType.ALLY_PRIMARY) != exists) {
                        G.rc.attack(loc, paint);
                        // paintLocation = loc;
                        break;
                    }
                }
            }
        }
        if (G.rc.canCompleteTowerPattern(Robot.towers[buildTowerType], ruinLocation) && G.rc.getPaint() > 50) {
            G.rc.completeTowerPattern(Robot.towers[buildTowerType], ruinLocation);
            POI.addTower(-1, ruinLocation, G.team, Robot.towers[buildTowerType]);
            mode = EXPLORE;
            Motion.exploreRandomly(moveWithPaintMicro);
            // dot to signal building complete
          // G.rc.setIndicatorDot(ruinLocation, 255, 200, 0);
        } else {
            Motion.bugnavAround(ruinLocation, 1, 1, moveWithPaintMicro);
          // G.rc.setIndicatorLine(G.rc.getLocation(), ruinLocation, 255, 200, 0);
        }
        // if (paintLocation != null)
          // G.rc.setIndicatorLine(G.me, paintLocation, 200, 100, 0);
      // G.rc.setIndicatorDot(G.me, 0, 0, 255);
    }

    public static void buildResource() throws Exception {
        G.indicatorString.append("BUILD_RP ");
        // MUCH IS IDENTICAL TO TOWER BUILD CODE
        // MapLocation paintLocation = null;
        int ox = resourceLocation.x - G.me.x + 2;
        int oy = resourceLocation.y - G.me.y + 2;
        boolean paint;
        PaintType exists;
        MapLocation loc;
        for (int dx = -1; dx++ < 4;) {
            for (int dy = -1; dy++ < 4;) {
                // location guaranteed to be on the map by canBuildSrpHere
                // guaranteed within vision radius if can attack there
                loc = resourceLocation.translate(dx - 2, dy - 2);
                if (G.rc.canAttack(loc)) {
                    paint = Robot.resourcePattern[dx][dy];
                    exists = mapInfos[oy + dy][ox + dx].getPaint();
                    // can't paint enemy paint
                    if (!exists.isEnemy() && (paint ? PaintType.ALLY_SECONDARY : PaintType.ALLY_PRIMARY) != exists) {
                        G.rc.attack(loc, paint);
                        // paintLocation = loc;
                        break;
                    }
                }
            }
        }
        if (G.rc.canCompleteResourcePattern(resourceLocation)) {
            G.rc.completeResourcePattern(resourceLocation);
            if (G.rc.getPaint() < EXPAND_SRP_MIN_PAINT) {
                // early retreat since painting more is very slow
                mode = RETREAT;
            } else {
                // put the 4 optimal locations of next pattern into a queue
                // that the bot then pathfinds to and checks if can build pattern
                mode = EXPAND_RESOURCE;
                // checks both chiralities
                srpCheckLocations = new MapLocation[] {
                        resourceLocation.translate(3, 1), resourceLocation.translate(3, -1),
                        resourceLocation.translate(1, -3), resourceLocation.translate(-1, -3),
                        resourceLocation.translate(-3, -1), resourceLocation.translate(-3, 1),
                        resourceLocation.translate(-1, 3), resourceLocation.translate(1, 3)
                };
                srpCheckIndex = 0;
            }
            Motion.exploreRandomly(moveWithPaintMicro);
            // dot to signal building complete
          // G.rc.setIndicatorDot(resourceLocation, 255, 200, 0);
        } else {
            // just sit in the middle of the SRP
            Motion.bugnavTowards(resourceLocation, moveWithPaintMicro);
          // G.rc.setIndicatorLine(G.rc.getLocation(), resourceLocation, 255, 100, 0);
        }
        // if (paintLocation != null)
          // G.rc.setIndicatorLine(G.me, paintLocation, 200, 100, 0);
      // G.rc.setIndicatorDot(G.me, 0, 200, 255);
    }

    public static void expandResource() throws Exception {
        G.indicatorString.append("EXPAND_RP ");
        Motion.bugnavTowards(srpCheckLocations[srpCheckIndex], moveWithPaintMicro);
        // show the queue and current target
        for (int i = srpCheckLocations.length; --i >= srpCheckIndex;) {
            // dots guaranteed to be on map because of expandResourceCheckMode
          // G.rc.setIndicatorDot(srpCheckLocations[i], 200, 100, 150);
        }
      // G.rc.setIndicatorLine(G.me, srpCheckLocations[srpCheckIndex], 255, 0, 150);
      // G.rc.setIndicatorDot(G.me, 0, 200, 255);
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
                        towerType.actionRadiusSquared + 1, moveWithPaintMicro);
            }
        }
      // G.rc.setIndicatorDot(G.me, 255, 0, 0);
    }

    /**
     * Checks if an SRP cannot be built at a location. If `false`, doesn't
     * necessarily mean *can* build, only *can't not* build. Allows non-aligned
     * checkerboarding if not overlapping.
     */
    public static boolean cannotBuildSRPAtLocation(MapLocation center) throws Exception {
        // check for towers that could interfere
        // ignore at first for better SRPs, then only avoid ruins (built towers ok)
        if (G.round > INITIAL_SRP_RUIN_IGNORE) {
            for (int i = nearbyRuins.length; --i >= 0;) {
                if (!G.rc.canSenseRobotAtLocation(nearbyRuins[i])
                        && Math.abs(G.me.x - nearbyRuins[i].x) <= 4 && Math.abs(G.me.y - nearbyRuins[i].y) <= 4) {
                    return true;
                }
            }
        }
        // only checks 7x7 square for markers, since any with markers outside can't
        // interfere (non-aligned checkerboards ignored if not overlapping)
        // any collision in 5x5 area means can't build
        // any SRP centers in 5x5 area means too much overlap
        // misaligned checkerboard within 7x7 means interference
        int ox = center.x - G.me.x + 4;
        int oy = center.y - G.me.y + 4;
        MapLocation loc;
        PaintType mark;
        // check 5x5
        for (int dy = -3; ++dy <= 2;) {
            for (int dx = -3; ++dx <= 2;) {
                loc = center.translate(dx, dy);
                if (!G.rc.onTheMap(loc))
                    return true;
                if (G.rc.canSenseLocation(loc)) {
                    mark = mapInfos[dy + oy][dx + ox].getMark();
                    if (!G.rc.sensePassability(loc) || mark == PaintType.ALLY_PRIMARY)
                        return true;
                    if (mark == PaintType.ALLY_SECONDARY && Math.abs(dy + dx) % 2 == 1)
                        return true;
                }
            }
        }
        // check edges of 7x7
        for (int dy = -4; ++dy <= 3;) {
            if (G.rc.canSenseLocation(center.translate(-3, dy))
                    && mapInfos[dy + oy][-3 + ox].getMark() == PaintType.ALLY_SECONDARY && Math.abs(dy + -3) % 2 == 1) {
                return true;
            }
            if (G.rc.canSenseLocation(center.translate(3, dy))
                    && mapInfos[dy + oy][3 + ox].getMark() == PaintType.ALLY_SECONDARY && Math.abs(dy + 3) % 2 == 1) {
                return true;
            }
        }
        for (int dx = -3; ++dx <= 2;) {
            if (G.rc.canSenseLocation(center.translate(dx, -3))
                    && mapInfos[-3 + oy][dx + ox].getMark() == PaintType.ALLY_SECONDARY && Math.abs(-3 + dx) % 2 == 1) {
                return true;
            }
            if (G.rc.canSenseLocation(center.translate(dx, 3))
                    && mapInfos[3 + oy][dx + ox].getMark() == PaintType.ALLY_SECONDARY && Math.abs(3 + dx) % 2 == 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if an SRP can be built or repaired at location.
     * MUST be called while at or adjacent (distance^2 <= 1) to location!
     */
    public static boolean canBuildSRPAtLocation(MapLocation center) throws Exception {
        // if on top of a current SRP, yes
        if (mapInfos[4][4].getMark() == PaintType.ALLY_PRIMARY)
            return true;
        else
            return !cannotBuildSRPAtLocation(center);
    }

    // paint neutral tiles if bugnav says to go to it
    // prevents bots taking dumb paths without painting
    // also preserves nearby checkerboards
    MapLocation mapInfoCenter = new MapLocation(4, 4);
    public static Micro moveWithPaintMicro = new Micro() {
        @Override
        public int[] micro(Direction d, MapLocation dest) throws Exception {
            int[] scores = new int[8];
            int score, best = 0;
            MapLocation nxt, bestLoc = null;
            PaintType p;
            boolean canPaint, canPaintBest = false;
            for (int i = 8; --i >= 0;) {
                if (!G.rc.canMove(G.DIRECTIONS[i]))
                    continue;
                score = 0;
                canPaint = false;
                nxt = G.me.add(G.DIRECTIONS[i]);
                p = G.rc.senseMapInfo(nxt).getPaint();
                if (p.isEnemy()) {
                    score -= 10;
                } else if (p == PaintType.EMPTY) {
                    if (G.rc.canAttack(nxt))
                        canPaint = true;
                    else
                        score -= 5;
                }
                if (G.DIRECTIONS[i] == d) {
                    score += 20;
                } else if (G.DIRECTIONS[i].rotateLeft() == d || G.DIRECTIONS[i].rotateRight() == d) {
                    score += 16;
                }
                scores[i] = score;
                if (score > best || bestLoc == null) {
                    best = score;
                    bestLoc = nxt;
                    canPaintBest = canPaint;
                }
            }
            if (canPaintBest) {
                // determine which checkerboard pattern to copy
                int[] cnt = new int[] { 0, 0 };
                MapLocation loc;
                for (int i = G.nearbyMapInfos.length; --i >= 0;) {
                    if (G.nearbyMapInfos[i].getPaint() == PaintType.ALLY_SECONDARY) {
                        loc = G.nearbyMapInfos[i].getMapLocation();
                        cnt[(loc.x + loc.y) & 1]++;
                    }
                }
                G.rc.attack(bestLoc, cnt[(bestLoc.x + bestLoc.y) & 1] > cnt[(1 + bestLoc.x + bestLoc.y) & 1]);
            } else if (G.rc.getActionCooldownTurns() < GameConstants.COOLDOWN_LIMIT) {
                // try to paint nearby
                MapLocation loc;
                for (int dx = -2; ++dx <= 2;) {
                    for (int dy = -2; ++dy <= 2;) {
                        loc = G.me.translate(dx, dy);
                        if (G.rc.onTheMap(loc) && mapInfos[dy + 4][dx + 4].getPaint() == PaintType.EMPTY) {
                            // still have to check if on map
                            if (G.rc.canAttack(loc))
                                G.rc.attack(loc);
                        }
                    }
                }
                loc = G.me.translate(-3, 0);
                if (G.rc.onTheMap(loc) && mapInfos[4][1].getPaint() == PaintType.EMPTY) {
                    // still have to check if on map
                    if (G.rc.canAttack(loc))
                        G.rc.attack(loc);
                }
                loc = G.me.translate(0, 3);
                if (G.rc.onTheMap(loc) && mapInfos[7][4].getPaint() == PaintType.EMPTY) {
                    // still have to check if on map
                    if (G.rc.canAttack(loc))
                        G.rc.attack(loc);
                }
                loc = G.me.translate(3, 0);
                if (G.rc.onTheMap(loc) && mapInfos[4][7].getPaint() == PaintType.EMPTY) {
                    // still have to check if on map
                    if (G.rc.canAttack(loc))
                        G.rc.attack(loc);
                }
                loc = G.me.translate(0, -3);
                if (G.rc.onTheMap(loc) && mapInfos[1][4].getPaint() == PaintType.EMPTY) {
                    // still have to check if on map
                    if (G.rc.canAttack(loc))
                        G.rc.attack(loc);
                }
            }
            return scores;
        }
    };

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
        // int paint = 0;
        // int money = 0;
        // if (G.me.isWithinDistanceSquared(xy, 4)) {

        // if (mapInfos[xy.y + 2 - G.me.y][xy.x + 2 - G.me.x].getPaint() ==
        // PaintType.ALLY_PRIMARY) {
        // money++;
        // }
        // else {
        // paint++;
        // }
        // if (mapInfos[xy.y + 2 - G.me.y][xy.x + 6 - G.me.x].getPaint() ==
        // PaintType.ALLY_PRIMARY) {
        // money++;
        // }
        // else {
        // paint++;
        // }
        // if (mapInfos[xy.y + 6 - G.me.y][xy.x + 2 - G.me.x].getPaint() ==
        // PaintType.ALLY_PRIMARY) {
        // money++;
        // }
        // else {
        // paint++;
        // }
        // if (mapInfos[xy.y + 6 - G.me.y][xy.x + 6 - G.me.x].getPaint() ==
        // PaintType.ALLY_PRIMARY) {
        // money++;
        // }
        // else {
        // paint++;
        // }
        // }
        // if (G.me.isWithinDistanceSquared(xy, 5)) {
        // if (mapInfos[xy.y + 2 - G.me.y][xy.x + 3 - G.me.x].getPaint() ==
        // PaintType.ALLY_PRIMARY) {
        // paint++;
        // }
        // else {
        // money++;
        // }
        // if (mapInfos[xy.y + 2 - G.me.y][xy.x + 4 - G.me.x].getPaint() ==
        // PaintType.ALLY_PRIMARY) {
        // paint++;
        // }
        // else {
        // money++;
        // }
        // if (mapInfos[xy.y + 2 - G.me.y][xy.x + 5 - G.me.x].getPaint() ==
        // PaintType.ALLY_PRIMARY) {
        // paint++;
        // }
        // else {
        // money++;
        // }
        // if (mapInfos[xy.y + 3 - G.me.y][xy.x + 2 - G.me.x].getPaint() ==
        // PaintType.ALLY_PRIMARY) {
        // paint++;
        // }
        // else {
        // money++;
        // }
        // if (mapInfos[xy.y + 3 - G.me.y][xy.x + 6 - G.me.x].getPaint() ==
        // PaintType.ALLY_PRIMARY) {
        // paint++;
        // }
        // else {
        // money++;
        // }
        // if (mapInfos[xy.y + 4 - G.me.y][xy.x + 2 - G.me.x].getPaint() ==
        // PaintType.ALLY_PRIMARY) {
        // paint++;
        // }
        // else {
        // money++;
        // }
        // if (mapInfos[xy.y + 4 - G.me.y][xy.x + 6 - G.me.x].getPaint() ==
        // PaintType.ALLY_PRIMARY) {
        // paint++;
        // }
        // else {
        // money++;
        // }
        // if (mapInfos[xy.y + 5 - G.me.y][xy.x + 2 - G.me.x].getPaint() ==
        // PaintType.ALLY_PRIMARY) {
        // paint++;
        // }
        // else {
        // money++;
        // }
        // if (mapInfos[xy.y + 5 - G.me.y][xy.x + 6 - G.me.x].getPaint() ==
        // PaintType.ALLY_PRIMARY) {
        // paint++;
        // }
        // else {
        // money++;
        // }
        // if (mapInfos[xy.y + 6 - G.me.y][xy.x + 3 - G.me.x].getPaint() ==
        // PaintType.ALLY_PRIMARY) {
        // paint++;
        // }
        // else {
        // money++;
        // }
        // if (mapInfos[xy.y + 6 - G.me.y][xy.x + 4 - G.me.x].getPaint() ==
        // PaintType.ALLY_PRIMARY) {
        // paint++;
        // }
        // else {
        // money++;
        // }
        // if (mapInfos[xy.y + 6 - G.me.y][xy.x + 5 - G.me.x].getPaint() ==
        // PaintType.ALLY_PRIMARY) {
        // paint++;
        // }
        // else {
        // money++;
        // }
        // if (paint > money) {
        // if (paint >= 5) {
        // return 2;
        // }
        // }
        // else {
        // if (money >= 5) {
        // return 1;
        // }
        // }
        // }
        // if (POI.moneyTowers > POI.paintTowers * 2) {
        G.indicatorString.append("(M=" + POI.moneyTowers + ", P=" + POI.paintTowers + ") ");
        if (POI.moneyTowers > POI.paintTowers) {
            return 2;
        }
        return 1;
        // return (G.rc.getNumberTowers() % 2) + 1;
    }
}