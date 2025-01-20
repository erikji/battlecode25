package mopperfollow;

import battlecode.common.*;

public class Mopper {
    public static final int EXPLORE = 0;
    public static final int BUILD = 1;
    public static final int RETREAT = 2;
    public static int mode = EXPLORE;

    public static final int BUILD_TIMEOUT = 10;

    public static MapLocation target = null;

    public static int lastBuild = -BUILD_TIMEOUT;

    public static int[] moveScores = new int[9];
    public static int[] attackScores = new int[25]; // mopping
    public static int[] swingScores = new int[36]; // swinging
    // [south, west, east, north] for each swingScore

    /**
     * If low on paint, retreat
     * Default to explore mode
     * 
     * Explore:
     * Use exploreRandomly, deleting enemy paint if it sees it
     * - Prefers to delete paint in the following importance: enemy/ally moppers,
     * enemies/allies, no bots (this stuff is codegen'd)
     * If near a ruin, go to BUILD mode
     * Mop swings are only better if there are very few nearby enemy bots, they are
     * all standing in a line, and there is no enemy standing on enemy paint
     * 
     * Build:
     * Help soldiers mop enemy paint around ruins
     */
    public static void run() throws Exception {
        if (mode == RETREAT) {
            Motion.tryTransferPaint();
        }
        if (G.rc.isActionReady()) {
            tryTransferPaint();
        }
        if (G.rc.getPaint() < Motion.getRetreatPaint()) {
            mode = RETREAT;
        } else if (G.rc.getPaint() > G.rc.getType().paintCapacity * 3 / 4 && mode == RETREAT) {
            mode = EXPLORE;
        }
        Motion.paintNeededToStopRetreating = G.rc.getType().paintCapacity * 3 / 4;
        int a = Clock.getBytecodeNum();
        switch (mode) {
            case EXPLORE:
                exploreCheckMode();
                break;
            case BUILD:
                buildCheckMode();
                break;
            case RETREAT: 
                Motion.setRetreatLoc();
                if (Motion.retreatLoc.x == -1) {
                    mode = EXPLORE;
                    exploreCheckMode();
                }
                break;
        }
        int b = Clock.getBytecodeNum();
        G.indicatorString.append((b - a) + " ");
        // grab directions for micro
        swingScores[0] = swingScores[1] = swingScores[2] = swingScores[3] = swingScores[4] = swingScores[5] = swingScores[6] = swingScores[7] = swingScores[8] = swingScores[9] = swingScores[10] = swingScores[11] = swingScores[12] = swingScores[13] = swingScores[14] = swingScores[15] = swingScores[16] = swingScores[17] = swingScores[18] = swingScores[19] = swingScores[20] = swingScores[21] = swingScores[22] = swingScores[23] = swingScores[24] = swingScores[25] = swingScores[26] = swingScores[27] = swingScores[28] = swingScores[29] = swingScores[30] = swingScores[31] = swingScores[32] = swingScores[33] = swingScores[34] = swingScores[35] = attackScores[0] = attackScores[1] = attackScores[2] = attackScores[3] = attackScores[4] = attackScores[5] = attackScores[6] = attackScores[7] = attackScores[8] = attackScores[9] = attackScores[10] = attackScores[11] = attackScores[12] = attackScores[13] = attackScores[14] = attackScores[15] = attackScores[16] = attackScores[17] = attackScores[18] = attackScores[19] = attackScores[20] = attackScores[21] = attackScores[22] = attackScores[23] = attackScores[24] = moveScores[0] = moveScores[1] = moveScores[2] = moveScores[3] = moveScores[4] = moveScores[5] = moveScores[6] = moveScores[7] = moveScores[8] = 0;
        switch (mode) {
            case EXPLORE -> {
                G.indicatorString.append("EXPLORE ");
                if (G.rc.isMovementReady()) {
                    exploreMoveScores();
                }
                if (G.rc.isActionReady()) {
                    exploreAttackScores();
                    exploreSwingScores();
                }
            }
            case BUILD -> {
                G.indicatorString.append("BUILD ");
                lastBuild = G.round;
                if (G.rc.isMovementReady()) {
                    buildMoveScores();
                }
                if (G.rc.isActionReady()) {
                    buildAttackScores();
                    buildSwingScores();
                }
            }
            case RETREAT -> {
                G.indicatorString.append("RETREAT ");
                if (G.rc.isMovementReady()) {
                    retreatMoveScores();
                }
                if (G.rc.isActionReady()) {
                    retreatAttackScores();
                    retreatSwingScores();
                }
            }
        }
        if (G.rc.isActionReady()) {
            tryTransferPaint();
        }
        boolean swing = false; // whether our attack will be a swing
        int cmax = attackScores[0];
        int cx = 0; // if it's a swing, then cx stores index of swing direction
        int cy = 0;
        // check every tile within sqrt2 radius
        // don't need to set swing=false here since it defaults to false
        if (attackScores[1] > cmax) {
            cmax = attackScores[1];
            cx = -1;
            cy = 0;
        }
        if (attackScores[2] > cmax) {
            cmax = attackScores[2];
            cx = 0;
            cy = -1;
        }
        if (attackScores[3] > cmax) {
            cmax = attackScores[3];
            cx = 0;
            cy = 1;
        }
        if (attackScores[4] > cmax) {
            cmax = attackScores[4];
            cx = 1;
            cy = 0;
        }
        if (attackScores[5] > cmax) {
            cmax = attackScores[5];
            cx = -1;
            cy = -1;
        }
        if (attackScores[6] > cmax) {
            cmax = attackScores[6];
            cx = -1;
            cy = 1;
        }
        if (attackScores[7] > cmax) {
            cmax = attackScores[7];
            cx = 1;
            cy = -1;
        }
        if (attackScores[8] > cmax) {
            cmax = attackScores[8];
            cx = 1;
            cy = 1;
        }
        if (swingScores[32] > cmax) {
            cmax = swingScores[32];
            cx = 1;
            swing = true;
        }
        if (swingScores[33] > cmax) {
            cmax = swingScores[33];
            cx = 7;
            swing = true;
        }
        if (swingScores[34] > cmax) {
            cmax = swingScores[34];
            cx = 3;
            swing = true;
        }
        if (swingScores[35] > cmax) {
            cmax = swingScores[35];
            cx = 5;
            swing = true;
        }
        // store total score and best attack location for each direction (incl
        // Direction.CENTER)
        int[] allmax = new int[] {
                cmax, cmax, cmax, cmax, cmax, cmax, cmax, cmax, cmax
        };
        // if it's a swing, then allx stores index of swing direction
        int[] allx = new int[] {
                cx, cx, cx, cx, cx, cx, cx, cx, cx
        };
        int[] ally = new int[] {
                cy, cy, cy, cy, cy, cy, cy, cy, cy
        };
        boolean[] allswing = new boolean[] {
                swing, swing, swing, swing, swing, swing, swing, swing, swing
        };
        if (attackScores[21] > allmax[0]) {
            allmax[0] = attackScores[21];
            allx[0] = -2;
            ally[0] = -2;
            allswing[0] = false;
        }
        if (attackScores[13] > allmax[0]) {
            allmax[0] = attackScores[13];
            allx[0] = -2;
            ally[0] = -1;
            allswing[0] = false;
        }
        if (attackScores[9] > allmax[0]) {
            allmax[0] = attackScores[9];
            allx[0] = -2;
            ally[0] = 0;
            allswing[0] = false;
        }
        if (attackScores[15] > allmax[0]) {
            allmax[0] = attackScores[15];
            allx[0] = -1;
            ally[0] = -2;
            allswing[0] = false;
        }
        if (attackScores[10] > allmax[0]) {
            allmax[0] = attackScores[10];
            allx[0] = 0;
            ally[0] = -2;
            allswing[0] = false;
        }
        if (attackScores[15] > allmax[1]) {
            allmax[1] = attackScores[15];
            allx[1] = -1;
            ally[1] = -2;
            allswing[1] = false;
        }
        if (attackScores[10] > allmax[1]) {
            allmax[1] = attackScores[10];
            allx[1] = 0;
            ally[1] = -2;
            allswing[1] = false;
        }
        if (attackScores[17] > allmax[1]) {
            allmax[1] = attackScores[17];
            allx[1] = 1;
            ally[1] = -2;
            allswing[1] = false;
        }
        if (attackScores[10] > allmax[2]) {
            allmax[2] = attackScores[10];
            allx[2] = 0;
            ally[2] = -2;
            allswing[2] = false;
        }
        if (attackScores[17] > allmax[2]) {
            allmax[2] = attackScores[17];
            allx[2] = 1;
            ally[2] = -2;
            allswing[2] = false;
        }
        if (attackScores[23] > allmax[2]) {
            allmax[2] = attackScores[23];
            allx[2] = 2;
            ally[2] = -2;
            allswing[2] = false;
        }
        if (attackScores[19] > allmax[2]) {
            allmax[2] = attackScores[19];
            allx[2] = 2;
            ally[2] = -1;
            allswing[2] = false;
        }
        if (attackScores[12] > allmax[2]) {
            allmax[2] = attackScores[12];
            allx[2] = 2;
            ally[2] = 0;
            allswing[2] = false;
        }
        if (attackScores[19] > allmax[3]) {
            allmax[3] = attackScores[19];
            allx[3] = 2;
            ally[3] = -1;
            allswing[3] = false;
        }
        if (attackScores[12] > allmax[3]) {
            allmax[3] = attackScores[12];
            allx[3] = 2;
            ally[3] = 0;
            allswing[3] = false;
        }
        if (attackScores[20] > allmax[3]) {
            allmax[3] = attackScores[20];
            allx[3] = 2;
            ally[3] = 1;
            allswing[3] = false;
        }
        if (attackScores[11] > allmax[4]) {
            allmax[4] = attackScores[11];
            allx[4] = 0;
            ally[4] = 2;
            allswing[4] = false;
        }
        if (attackScores[18] > allmax[4]) {
            allmax[4] = attackScores[18];
            allx[4] = 1;
            ally[4] = 2;
            allswing[4] = false;
        }
        if (attackScores[12] > allmax[4]) {
            allmax[4] = attackScores[12];
            allx[4] = 2;
            ally[4] = 0;
            allswing[4] = false;
        }
        if (attackScores[20] > allmax[4]) {
            allmax[4] = attackScores[20];
            allx[4] = 2;
            ally[4] = 1;
            allswing[4] = false;
        }
        if (attackScores[24] > allmax[4]) {
            allmax[4] = attackScores[24];
            allx[4] = 2;
            ally[4] = 2;
            allswing[4] = false;
        }
        if (attackScores[16] > allmax[5]) {
            allmax[5] = attackScores[16];
            allx[5] = -1;
            ally[5] = 2;
            allswing[5] = false;
        }
        if (attackScores[11] > allmax[5]) {
            allmax[5] = attackScores[11];
            allx[5] = 0;
            ally[5] = 2;
            allswing[5] = false;
        }
        if (attackScores[18] > allmax[5]) {
            allmax[5] = attackScores[18];
            allx[5] = 1;
            ally[5] = 2;
            allswing[5] = false;
        }
        if (attackScores[9] > allmax[6]) {
            allmax[6] = attackScores[9];
            allx[6] = -2;
            ally[6] = 0;
            allswing[6] = false;
        }
        if (attackScores[14] > allmax[6]) {
            allmax[6] = attackScores[14];
            allx[6] = -2;
            ally[6] = 1;
            allswing[6] = false;
        }
        if (attackScores[22] > allmax[6]) {
            allmax[6] = attackScores[22];
            allx[6] = -2;
            ally[6] = 2;
            allswing[6] = false;
        }
        if (attackScores[16] > allmax[6]) {
            allmax[6] = attackScores[16];
            allx[6] = -1;
            ally[6] = 2;
            allswing[6] = false;
        }
        if (attackScores[11] > allmax[6]) {
            allmax[6] = attackScores[11];
            allx[6] = 0;
            ally[6] = 2;
            allswing[6] = false;
        }
        if (attackScores[13] > allmax[7]) {
            allmax[7] = attackScores[13];
            allx[7] = -2;
            ally[7] = -1;
            allswing[7] = false;
        }
        if (attackScores[9] > allmax[7]) {
            allmax[7] = attackScores[9];
            allx[7] = -2;
            ally[7] = 0;
            allswing[7] = false;
        }
        if (attackScores[14] > allmax[7]) {
            allmax[7] = attackScores[14];
            allx[7] = -2;
            ally[7] = 1;
            allswing[7] = false;
        }
        if (swingScores[0] > allmax[0]) {
            allmax[0] = swingScores[0];
            allx[0] = 1;
            allswing[0] = true;
        }
        if (swingScores[1] > allmax[0]) {
            allmax[0] = swingScores[1];
            allx[0] = 7;
            allswing[0] = true;
        }
        if (swingScores[2] > allmax[0]) {
            allmax[0] = swingScores[2];
            allx[0] = 3;
            allswing[0] = true;
        }
        if (swingScores[3] > allmax[0]) {
            allmax[0] = swingScores[3];
            allx[0] = 5;
            allswing[0] = true;
        }
        if (swingScores[4] > allmax[1]) {
            allmax[1] = swingScores[4];
            allx[1] = 1;
            allswing[1] = true;
        }
        if (swingScores[5] > allmax[1]) {
            allmax[1] = swingScores[5];
            allx[1] = 7;
            allswing[1] = true;
        }
        if (swingScores[6] > allmax[1]) {
            allmax[1] = swingScores[6];
            allx[1] = 3;
            allswing[1] = true;
        }
        if (swingScores[7] > allmax[1]) {
            allmax[1] = swingScores[7];
            allx[1] = 5;
            allswing[1] = true;
        }
        if (swingScores[8] > allmax[2]) {
            allmax[2] = swingScores[8];
            allx[2] = 1;
            allswing[2] = true;
        }
        if (swingScores[9] > allmax[2]) {
            allmax[2] = swingScores[9];
            allx[2] = 7;
            allswing[2] = true;
        }
        if (swingScores[10] > allmax[2]) {
            allmax[2] = swingScores[10];
            allx[2] = 3;
            allswing[2] = true;
        }
        if (swingScores[11] > allmax[2]) {
            allmax[2] = swingScores[11];
            allx[2] = 5;
            allswing[2] = true;
        }
        if (swingScores[12] > allmax[3]) {
            allmax[3] = swingScores[12];
            allx[3] = 1;
            allswing[3] = true;
        }
        if (swingScores[13] > allmax[3]) {
            allmax[3] = swingScores[13];
            allx[3] = 7;
            allswing[3] = true;
        }
        if (swingScores[14] > allmax[3]) {
            allmax[3] = swingScores[14];
            allx[3] = 3;
            allswing[3] = true;
        }
        if (swingScores[15] > allmax[3]) {
            allmax[3] = swingScores[15];
            allx[3] = 5;
            allswing[3] = true;
        }
        if (swingScores[16] > allmax[4]) {
            allmax[4] = swingScores[16];
            allx[4] = 1;
            allswing[4] = true;
        }
        if (swingScores[17] > allmax[4]) {
            allmax[4] = swingScores[17];
            allx[4] = 7;
            allswing[4] = true;
        }
        if (swingScores[18] > allmax[4]) {
            allmax[4] = swingScores[18];
            allx[4] = 3;
            allswing[4] = true;
        }
        if (swingScores[19] > allmax[4]) {
            allmax[4] = swingScores[19];
            allx[4] = 5;
            allswing[4] = true;
        }
        if (swingScores[20] > allmax[5]) {
            allmax[5] = swingScores[20];
            allx[5] = 1;
            allswing[5] = true;
        }
        if (swingScores[21] > allmax[5]) {
            allmax[5] = swingScores[21];
            allx[5] = 7;
            allswing[5] = true;
        }
        if (swingScores[22] > allmax[5]) {
            allmax[5] = swingScores[22];
            allx[5] = 3;
            allswing[5] = true;
        }
        if (swingScores[23] > allmax[5]) {
            allmax[5] = swingScores[23];
            allx[5] = 5;
            allswing[5] = true;
        }
        if (swingScores[24] > allmax[6]) {
            allmax[6] = swingScores[24];
            allx[6] = 1;
            allswing[6] = true;
        }
        if (swingScores[25] > allmax[6]) {
            allmax[6] = swingScores[25];
            allx[6] = 7;
            allswing[6] = true;
        }
        if (swingScores[26] > allmax[6]) {
            allmax[6] = swingScores[26];
            allx[6] = 3;
            allswing[6] = true;
        }
        if (swingScores[27] > allmax[6]) {
            allmax[6] = swingScores[27];
            allx[6] = 5;
            allswing[6] = true;
        }
        if (swingScores[28] > allmax[7]) {
            allmax[7] = swingScores[28];
            allx[7] = 1;
            allswing[7] = true;
        }
        if (swingScores[29] > allmax[7]) {
            allmax[7] = swingScores[29];
            allx[7] = 7;
            allswing[7] = true;
        }
        if (swingScores[30] > allmax[7]) {
            allmax[7] = swingScores[30];
            allx[7] = 3;
            allswing[7] = true;
        }
        if (swingScores[31] > allmax[7]) {
            allmax[7] = swingScores[31];
            allx[7] = 5;
            allswing[7] = true;
        }
        // copyspaghetti from Motion.microMove but whatever
        if (G.rc.isActionReady()) {
            int best = 8;
            int numBest = 1;
            for (int i = 8; --i >= 0;) {
                if (allmax[i] + moveScores[i] > allmax[best] + moveScores[best]) {
                    best = i;
                    numBest = 1;
                } else if (allmax[i] + moveScores[i] == allmax[best] + moveScores[best]
                        && Random.rand() % ++numBest == 0) {
                    best = i;
                }
            }
            // try attack then move then attack again
            if (allswing[best]) {
                if (allmax[best] > 0) {
                    if (allmax[best] == cmax) {
                        if (G.rc.canMopSwing(G.DIRECTIONS[allx[best]])) {
                            G.rc.mopSwing(G.DIRECTIONS[allx[best]]);
                        }
                    }
                    Motion.move(G.ALL_DIRECTIONS[best]);
                    if (allmax[best] != cmax) {
                        if (G.rc.canMopSwing(G.DIRECTIONS[allx[best]])) {
                            G.rc.mopSwing(G.DIRECTIONS[allx[best]]);
                        }
                    }
                }
            } else {
                if (allmax[best] > 0) {
                    MapLocation attackLoc = G.me.translate(allx[best], ally[best]);
                    if (G.rc.canAttack(attackLoc)) {
                        G.rc.attack(attackLoc);
                    }
                    Motion.move(G.ALL_DIRECTIONS[best]);
                    if (G.rc.canAttack(attackLoc)) {
                        G.rc.attack(attackLoc);
                    }
                }
            }
        }
        if (G.rc.isMovementReady()) {
            int best = 8;
            int numBest = 1;
            for (int i = 8; --i >= 0;) {
                if (moveScores[i] > moveScores[best]) {
                    best = i;
                    numBest = 1;
                } else if (moveScores[i] == moveScores[best] && Random.rand() % ++numBest == 0) {
                    best = i;
                }
            }
            Motion.move(G.ALL_DIRECTIONS[best]);
        }
        if (mode == RETREAT) {
            Motion.tryTransferPaint();
        }
        switch (mode) {
            case EXPLORE -> G.rc.setIndicatorDot(G.me, 0, 255, 0);
            case BUILD -> G.rc.setIndicatorDot(G.me, 0, 0, 255);
            case RETREAT -> G.rc.setIndicatorDot(G.me, 255, 0, 255);
        }
        G.indicatorString.append((Clock.getBytecodeNum() - b) + " ");
    }

    public static void exploreCheckMode() throws Exception {
        G.indicatorString.append("CHK_E ");
        // make sure not stuck between exploring and building
        if (lastBuild + BUILD_TIMEOUT < G.round && G.rc.getNumberTowers() < 25) {
            MapLocation[] locs = G.rc.senseNearbyRuins(-1);
            for (int i = locs.length; --i >= 0;) {
                if (G.rc.canSenseRobotAtLocation(locs[i])) {
                    continue;
                }
                target = locs[i];
                mode = BUILD;
                break;
            }
        }
    }

    public static void buildCheckMode() throws Exception {
        G.indicatorString.append("CHK_B ");
        if (!G.rc.canSenseLocation(target) || G.rc.canSenseRobotAtLocation(target) || G.rc.getNumberTowers() == 25) {
            mode = EXPLORE;
            target = null;
            return;
        }
        // if we don't see anything to mop, then leave
        for (int i = G.nearbyMapInfos.length; --i >= 0;) {
            if (G.nearbyMapInfos[i].getMapLocation().distanceSquaredTo(target) <= 8
                    && G.nearbyMapInfos[i].getPaint().isEnemy())
                return;
        }
        mode = EXPLORE;
        target = null;
    }

    public static void exploreMoveScores() throws Exception {
        MapLocation bestBot = null;
        MapLocation bestEmpty = null;
        MapLocation microDir = G.me;
        for (int i = G.nearbyMapInfos.length; --i >= 0;) {
            MapInfo info = G.nearbyMapInfos[i];
            MapLocation loc = info.getMapLocation();
            // stuff we can't hit immediately cuz thats considered in attack micro
            if (G.me.distanceSquaredTo(loc) > 2 && info.getPaint().isEnemy()) {
                microDir = microDir.add(G.me.directionTo(loc));
                if (G.rc.canSenseRobotAtLocation(loc)) {
                    if (G.rc.senseRobotAtLocation(loc).team == G.opponentTeam
                            && (bestBot == null || G.me.distanceSquaredTo(loc) < G.me.distanceSquaredTo(bestBot))) {
                        bestBot = loc;
                    }
                } else {
                    if (bestEmpty == null || G.me.distanceSquaredTo(loc) < G.me.distanceSquaredTo(bestEmpty)) {
                        bestEmpty = loc;
                    }
                }
            }
        }
        MapLocation bestAllyBot = null;
        int lowestAllyPaint = 0;
        for (int i = G.allyRobots.length; --i >= 0;) {
            if (G.allyRobots[i].type.isTowerType()) {
                continue;
            }
            if (G.allyRobots[i].type == UnitType.MOPPER) {
                continue;
            }
            if (bestAllyBot == null || G.allyRobots[i].paintAmount < lowestAllyPaint) {
                bestAllyBot = G.allyRobots[i].location;
                lowestAllyPaint = G.allyRobots[i].paintAmount;
            }
        }
        Direction dir = Direction.CENTER;
        if (bestAllyBot != null) {
            dir = Motion.bug2Helper(G.me, bestAllyBot, Motion.TOWARDS, 0, 0);
        }
        else if (bestEmpty == null && bestBot == null) {
            G.indicatorString.append("RAND ");
            dir = Motion.bug2Helper(G.me, Motion.exploreRandomlyLoc(), Motion.TOWARDS, 0, 0);
        } else {
            if (bestBot != null)
                bestEmpty = bestBot;
            dir = Motion.bug2Helper(G.me, bestEmpty, Motion.AROUND, 1, 2);
            G.rc.setIndicatorLine(G.me, bestEmpty, 0, 0, 255);
        }
        moveScores = Motion.defaultMicro.micro(dir, G.invalidLoc);
        if (G.rc.onTheMap(microDir))
            G.rc.setIndicatorLine(G.me, microDir, 0, 200, 255);
    }

    public static void exploreAttackScores() throws Exception {
        MapLocation loc = G.me;
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[0] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[0] += 100;
                }
            }
            attackScores[0] += 25;
        }
        loc = G.me.translate(-1, 0);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[1] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[1] += 100;
                }
            }
            attackScores[1] += 25;
        }
        loc = G.me.translate(0, -1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[2] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[2] += 100;
                }
            }
            attackScores[2] += 25;
        }
        loc = G.me.translate(0, 1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[3] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[3] += 100;
                }
            }
            attackScores[3] += 25;
        }
        loc = G.me.translate(1, 0);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[4] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[4] += 100;
                }
            }
            attackScores[4] += 25;
        }
        loc = G.me.translate(-1, -1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[5] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[5] += 100;
                }
            }
            attackScores[5] += 25;
        }
        loc = G.me.translate(-1, 1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[6] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[6] += 100;
                }
            }
            attackScores[6] += 25;
        }
        loc = G.me.translate(1, -1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[7] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[7] += 100;
                }
            }
            attackScores[7] += 25;
        }
        loc = G.me.translate(1, 1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[8] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[8] += 100;
                }
            }
            attackScores[8] += 25;
        }
        loc = G.me.translate(-2, 0);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[9] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[9] += 100;
                }
            }
            attackScores[9] += 25;
        }
        loc = G.me.translate(0, -2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[10] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[10] += 100;
                }
            }
            attackScores[10] += 25;
        }
        loc = G.me.translate(0, 2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[11] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[11] += 100;
                }
            }
            attackScores[11] += 25;
        }
        loc = G.me.translate(2, 0);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[12] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[12] += 100;
                }
            }
            attackScores[12] += 25;
        }
        loc = G.me.translate(-2, -1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[13] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[13] += 100;
                }
            }
            attackScores[13] += 25;
        }
        loc = G.me.translate(-2, 1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[14] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[14] += 100;
                }
            }
            attackScores[14] += 25;
        }
        loc = G.me.translate(-1, -2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[15] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[15] += 100;
                }
            }
            attackScores[15] += 25;
        }
        loc = G.me.translate(-1, 2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[16] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[16] += 100;
                }
            }
            attackScores[16] += 25;
        }
        loc = G.me.translate(1, -2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[17] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[17] += 100;
                }
            }
            attackScores[17] += 25;
        }
        loc = G.me.translate(1, 2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[18] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[18] += 100;
                }
            }
            attackScores[18] += 25;
        }
        loc = G.me.translate(2, -1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[19] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[19] += 100;
                }
            }
            attackScores[19] += 25;
        }
        loc = G.me.translate(2, 1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[20] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[20] += 100;
                }
            }
            attackScores[20] += 25;
        }
        loc = G.me.translate(-2, -2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[21] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[21] += 100;
                }
            }
            attackScores[21] += 25;
        }
        loc = G.me.translate(-2, 2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[22] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[22] += 100;
                }
            }
            attackScores[22] += 25;
        }
        loc = G.me.translate(2, -2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[23] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[23] += 100;
                }
            }
            attackScores[23] += 25;
        }
        loc = G.me.translate(2, 2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[24] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[24] += 100;
                }
            }
            attackScores[24] += 25;
        }
    }

    public static void exploreSwingScores() throws Exception {
        MapLocation loc;
        loc = G.me.translate(-1, -2);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[32] += Math.min(5, bot.paintAmount) * 7;
            swingScores[28] += Math.min(5, bot.paintAmount) * 7;
            swingScores[9] += Math.min(5, bot.paintAmount) * 7;
            swingScores[5] += Math.min(5, bot.paintAmount) * 7;
            swingScores[4] += Math.min(5, bot.paintAmount) * 7;
            swingScores[0] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(-1, -3);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[4] += Math.min(5, bot.paintAmount) * 7;
            swingScores[0] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(-2, -2);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[28] += Math.min(5, bot.paintAmount) * 7;
            swingScores[5] += Math.min(5, bot.paintAmount) * 7;
            swingScores[1] += Math.min(5, bot.paintAmount) * 7;
            swingScores[0] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(-2, -3);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[0] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(0, -2);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[32] += Math.min(5, bot.paintAmount) * 7;
            swingScores[28] += Math.min(5, bot.paintAmount) * 7;
            swingScores[12] += Math.min(5, bot.paintAmount) * 7;
            swingScores[9] += Math.min(5, bot.paintAmount) * 7;
            swingScores[8] += Math.min(5, bot.paintAmount) * 7;
            swingScores[4] += Math.min(5, bot.paintAmount) * 7;
            swingScores[2] += Math.min(5, bot.paintAmount) * 7;
            swingScores[0] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(0, -3);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[8] += Math.min(5, bot.paintAmount) * 7;
            swingScores[4] += Math.min(5, bot.paintAmount) * 7;
            swingScores[0] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(-2, -1);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[33] += Math.min(5, bot.paintAmount) * 7;
            swingScores[29] += Math.min(5, bot.paintAmount) * 7;
            swingScores[28] += Math.min(5, bot.paintAmount) * 7;
            swingScores[24] += Math.min(5, bot.paintAmount) * 7;
            swingScores[5] += Math.min(5, bot.paintAmount) * 7;
            swingScores[1] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(-3, -1);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[29] += Math.min(5, bot.paintAmount) * 7;
            swingScores[1] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(-3, -2);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[1] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(-2, 0);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[33] += Math.min(5, bot.paintAmount) * 7;
            swingScores[29] += Math.min(5, bot.paintAmount) * 7;
            swingScores[25] += Math.min(5, bot.paintAmount) * 7;
            swingScores[24] += Math.min(5, bot.paintAmount) * 7;
            swingScores[21] += Math.min(5, bot.paintAmount) * 7;
            swingScores[5] += Math.min(5, bot.paintAmount) * 7;
            swingScores[3] += Math.min(5, bot.paintAmount) * 7;
            swingScores[1] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(-3, 0);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[29] += Math.min(5, bot.paintAmount) * 7;
            swingScores[25] += Math.min(5, bot.paintAmount) * 7;
            swingScores[1] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(0, -1);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[32] += Math.min(5, bot.paintAmount) * 7;
            swingScores[30] += Math.min(5, bot.paintAmount) * 7;
            swingScores[28] += Math.min(5, bot.paintAmount) * 7;
            swingScores[24] += Math.min(5, bot.paintAmount) * 7;
            swingScores[20] += Math.min(5, bot.paintAmount) * 7;
            swingScores[16] += Math.min(5, bot.paintAmount) * 7;
            swingScores[13] += Math.min(5, bot.paintAmount) * 7;
            swingScores[12] += Math.min(5, bot.paintAmount) * 7;
            swingScores[9] += Math.min(5, bot.paintAmount) * 7;
            swingScores[2] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(1, -1);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[34] += Math.min(5, bot.paintAmount) * 7;
            swingScores[32] += Math.min(5, bot.paintAmount) * 7;
            swingScores[30] += Math.min(5, bot.paintAmount) * 7;
            swingScores[20] += Math.min(5, bot.paintAmount) * 7;
            swingScores[16] += Math.min(5, bot.paintAmount) * 7;
            swingScores[12] += Math.min(5, bot.paintAmount) * 7;
            swingScores[6] += Math.min(5, bot.paintAmount) * 7;
            swingScores[2] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(0, 0);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[30] += Math.min(5, bot.paintAmount) * 7;
            swingScores[26] += Math.min(5, bot.paintAmount) * 7;
            swingScores[24] += Math.min(5, bot.paintAmount) * 7;
            swingScores[20] += Math.min(5, bot.paintAmount) * 7;
            swingScores[17] += Math.min(5, bot.paintAmount) * 7;
            swingScores[16] += Math.min(5, bot.paintAmount) * 7;
            swingScores[13] += Math.min(5, bot.paintAmount) * 7;
            swingScores[11] += Math.min(5, bot.paintAmount) * 7;
            swingScores[9] += Math.min(5, bot.paintAmount) * 7;
            swingScores[7] += Math.min(5, bot.paintAmount) * 7;
            swingScores[3] += Math.min(5, bot.paintAmount) * 7;
            swingScores[2] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(1, 0);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[34] += Math.min(5, bot.paintAmount) * 7;
            swingScores[30] += Math.min(5, bot.paintAmount) * 7;
            swingScores[26] += Math.min(5, bot.paintAmount) * 7;
            swingScores[22] += Math.min(5, bot.paintAmount) * 7;
            swingScores[20] += Math.min(5, bot.paintAmount) * 7;
            swingScores[16] += Math.min(5, bot.paintAmount) * 7;
            swingScores[11] += Math.min(5, bot.paintAmount) * 7;
            swingScores[7] += Math.min(5, bot.paintAmount) * 7;
            swingScores[6] += Math.min(5, bot.paintAmount) * 7;
            swingScores[2] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(1, -2);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[32] += Math.min(5, bot.paintAmount) * 7;
            swingScores[12] += Math.min(5, bot.paintAmount) * 7;
            swingScores[8] += Math.min(5, bot.paintAmount) * 7;
            swingScores[6] += Math.min(5, bot.paintAmount) * 7;
            swingScores[4] += Math.min(5, bot.paintAmount) * 7;
            swingScores[2] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(-1, 0);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[33] += Math.min(5, bot.paintAmount) * 7;
            swingScores[24] += Math.min(5, bot.paintAmount) * 7;
            swingScores[21] += Math.min(5, bot.paintAmount) * 7;
            swingScores[20] += Math.min(5, bot.paintAmount) * 7;
            swingScores[17] += Math.min(5, bot.paintAmount) * 7;
            swingScores[13] += Math.min(5, bot.paintAmount) * 7;
            swingScores[9] += Math.min(5, bot.paintAmount) * 7;
            swingScores[7] += Math.min(5, bot.paintAmount) * 7;
            swingScores[5] += Math.min(5, bot.paintAmount) * 7;
            swingScores[3] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(-1, 1);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[35] += Math.min(5, bot.paintAmount) * 7;
            swingScores[33] += Math.min(5, bot.paintAmount) * 7;
            swingScores[31] += Math.min(5, bot.paintAmount) * 7;
            swingScores[21] += Math.min(5, bot.paintAmount) * 7;
            swingScores[17] += Math.min(5, bot.paintAmount) * 7;
            swingScores[13] += Math.min(5, bot.paintAmount) * 7;
            swingScores[7] += Math.min(5, bot.paintAmount) * 7;
            swingScores[3] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(0, 1);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[35] += Math.min(5, bot.paintAmount) * 7;
            swingScores[31] += Math.min(5, bot.paintAmount) * 7;
            swingScores[30] += Math.min(5, bot.paintAmount) * 7;
            swingScores[26] += Math.min(5, bot.paintAmount) * 7;
            swingScores[17] += Math.min(5, bot.paintAmount) * 7;
            swingScores[15] += Math.min(5, bot.paintAmount) * 7;
            swingScores[13] += Math.min(5, bot.paintAmount) * 7;
            swingScores[11] += Math.min(5, bot.paintAmount) * 7;
            swingScores[7] += Math.min(5, bot.paintAmount) * 7;
            swingScores[3] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(-2, 1);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[33] += Math.min(5, bot.paintAmount) * 7;
            swingScores[31] += Math.min(5, bot.paintAmount) * 7;
            swingScores[29] += Math.min(5, bot.paintAmount) * 7;
            swingScores[25] += Math.min(5, bot.paintAmount) * 7;
            swingScores[21] += Math.min(5, bot.paintAmount) * 7;
            swingScores[3] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(1, -3);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[8] += Math.min(5, bot.paintAmount) * 7;
            swingScores[4] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(-1, -1);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[33] += Math.min(5, bot.paintAmount) * 7;
            swingScores[32] += Math.min(5, bot.paintAmount) * 7;
            swingScores[28] += Math.min(5, bot.paintAmount) * 7;
            swingScores[24] += Math.min(5, bot.paintAmount) * 7;
            swingScores[20] += Math.min(5, bot.paintAmount) * 7;
            swingScores[13] += Math.min(5, bot.paintAmount) * 7;
            swingScores[9] += Math.min(5, bot.paintAmount) * 7;
            swingScores[5] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(2, -1);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[34] += Math.min(5, bot.paintAmount) * 7;
            swingScores[16] += Math.min(5, bot.paintAmount) * 7;
            swingScores[14] += Math.min(5, bot.paintAmount) * 7;
            swingScores[12] += Math.min(5, bot.paintAmount) * 7;
            swingScores[10] += Math.min(5, bot.paintAmount) * 7;
            swingScores[6] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(2, 0);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[34] += Math.min(5, bot.paintAmount) * 7;
            swingScores[22] += Math.min(5, bot.paintAmount) * 7;
            swingScores[18] += Math.min(5, bot.paintAmount) * 7;
            swingScores[16] += Math.min(5, bot.paintAmount) * 7;
            swingScores[14] += Math.min(5, bot.paintAmount) * 7;
            swingScores[11] += Math.min(5, bot.paintAmount) * 7;
            swingScores[10] += Math.min(5, bot.paintAmount) * 7;
            swingScores[6] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(2, -2);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[12] += Math.min(5, bot.paintAmount) * 7;
            swingScores[10] += Math.min(5, bot.paintAmount) * 7;
            swingScores[8] += Math.min(5, bot.paintAmount) * 7;
            swingScores[6] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(1, 1);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[35] += Math.min(5, bot.paintAmount) * 7;
            swingScores[34] += Math.min(5, bot.paintAmount) * 7;
            swingScores[30] += Math.min(5, bot.paintAmount) * 7;
            swingScores[26] += Math.min(5, bot.paintAmount) * 7;
            swingScores[22] += Math.min(5, bot.paintAmount) * 7;
            swingScores[15] += Math.min(5, bot.paintAmount) * 7;
            swingScores[11] += Math.min(5, bot.paintAmount) * 7;
            swingScores[7] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(2, -3);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[8] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(3, -1);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[14] += Math.min(5, bot.paintAmount) * 7;
            swingScores[10] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(3, 0);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[18] += Math.min(5, bot.paintAmount) * 7;
            swingScores[14] += Math.min(5, bot.paintAmount) * 7;
            swingScores[10] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(3, -2);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[10] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(2, 1);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[34] += Math.min(5, bot.paintAmount) * 7;
            swingScores[22] += Math.min(5, bot.paintAmount) * 7;
            swingScores[18] += Math.min(5, bot.paintAmount) * 7;
            swingScores[15] += Math.min(5, bot.paintAmount) * 7;
            swingScores[14] += Math.min(5, bot.paintAmount) * 7;
            swingScores[11] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(3, 1);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[18] += Math.min(5, bot.paintAmount) * 7;
            swingScores[14] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(1, 2);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[35] += Math.min(5, bot.paintAmount) * 7;
            swingScores[26] += Math.min(5, bot.paintAmount) * 7;
            swingScores[23] += Math.min(5, bot.paintAmount) * 7;
            swingScores[22] += Math.min(5, bot.paintAmount) * 7;
            swingScores[19] += Math.min(5, bot.paintAmount) * 7;
            swingScores[15] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(2, 2);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[22] += Math.min(5, bot.paintAmount) * 7;
            swingScores[19] += Math.min(5, bot.paintAmount) * 7;
            swingScores[18] += Math.min(5, bot.paintAmount) * 7;
            swingScores[15] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(0, 2);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[35] += Math.min(5, bot.paintAmount) * 7;
            swingScores[31] += Math.min(5, bot.paintAmount) * 7;
            swingScores[27] += Math.min(5, bot.paintAmount) * 7;
            swingScores[26] += Math.min(5, bot.paintAmount) * 7;
            swingScores[23] += Math.min(5, bot.paintAmount) * 7;
            swingScores[19] += Math.min(5, bot.paintAmount) * 7;
            swingScores[17] += Math.min(5, bot.paintAmount) * 7;
            swingScores[15] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(-1, 2);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[35] += Math.min(5, bot.paintAmount) * 7;
            swingScores[31] += Math.min(5, bot.paintAmount) * 7;
            swingScores[27] += Math.min(5, bot.paintAmount) * 7;
            swingScores[23] += Math.min(5, bot.paintAmount) * 7;
            swingScores[21] += Math.min(5, bot.paintAmount) * 7;
            swingScores[17] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(3, 2);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[18] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(1, 3);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[23] += Math.min(5, bot.paintAmount) * 7;
            swingScores[19] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(2, 3);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[19] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(0, 3);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[27] += Math.min(5, bot.paintAmount) * 7;
            swingScores[23] += Math.min(5, bot.paintAmount) * 7;
            swingScores[19] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(-2, 2);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[31] += Math.min(5, bot.paintAmount) * 7;
            swingScores[27] += Math.min(5, bot.paintAmount) * 7;
            swingScores[25] += Math.min(5, bot.paintAmount) * 7;
            swingScores[21] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(-1, 3);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[27] += Math.min(5, bot.paintAmount) * 7;
            swingScores[23] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(-3, 1);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[29] += Math.min(5, bot.paintAmount) * 7;
            swingScores[25] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(-3, 2);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[25] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
        loc = G.me.translate(-2, 3);
        if (G.opponentRobotsString.indexOf(loc.toString()) != -1) {
            RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            swingScores[27] += Math.min(5, bot.paintAmount) * 7; //7 because the cooldown is lower for swing
        }
    }

    public static void buildMoveScores() throws Exception {
        // clean enemy paint for ruin patterns
        // TODO: FIND AND MOP ENEMY PAINT OFF SRP
        // get 2 best locations to build stuff on
        // so if the first one is already there just go to the next one
        moveScores = Motion.defaultMicro.micro(G.me.directionTo(target), target);
        moveScores[G.dirOrd(G.me.directionTo(target))] -= 18;
        moveScores[(G.dirOrd(G.me.directionTo(target)) + 1) % 8] -= 14;
        moveScores[(G.dirOrd(G.me.directionTo(target)) + 7) % 8] -= 14;
    }

    public static void buildAttackScores() throws Exception {
        MapLocation loc = G.me;
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[0] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[0] += 100;
                }
            }
            if (target.distanceSquaredTo(loc) <= 8) {
                attackScores[0] += 50;
            }
            attackScores[0] += 25;
        }
        loc = G.me.translate(-1, 0);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[1] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[1] += 100;
                }
            }
            if (target.distanceSquaredTo(loc) <= 8) {
                attackScores[1] += 50;
            }
            attackScores[1] += 25;
        }
        loc = G.me.translate(0, -1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[2] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[2] += 100;
                }
            }
            if (target.distanceSquaredTo(loc) <= 8) {
                attackScores[2] += 50;
            }
            attackScores[2] += 25;
        }
        loc = G.me.translate(0, 1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[3] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[3] += 100;
                }
            }
            if (target.distanceSquaredTo(loc) <= 8) {
                attackScores[3] += 50;
            }
            attackScores[3] += 25;
        }
        loc = G.me.translate(1, 0);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[4] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[4] += 100;
                }
            }
            if (target.distanceSquaredTo(loc) <= 8) {
                attackScores[4] += 50;
            }
            attackScores[4] += 25;
        }
        loc = G.me.translate(-1, -1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[5] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[5] += 100;
                }
            }
            if (target.distanceSquaredTo(loc) <= 8) {
                attackScores[5] += 50;
            }
            attackScores[5] += 25;
        }
        loc = G.me.translate(-1, 1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[6] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[6] += 100;
                }
            }
            if (target.distanceSquaredTo(loc) <= 8) {
                attackScores[6] += 50;
            }
            attackScores[6] += 25;
        }
        loc = G.me.translate(1, -1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[7] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[7] += 100;
                }
            }
            if (target.distanceSquaredTo(loc) <= 8) {
                attackScores[7] += 50;
            }
            attackScores[7] += 25;
        }
        loc = G.me.translate(1, 1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[8] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[8] += 100;
                }
            }
            if (target.distanceSquaredTo(loc) <= 8) {
                attackScores[8] += 50;
            }
            attackScores[8] += 25;
        }
        loc = G.me.translate(-2, 0);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[9] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[9] += 100;
                }
            }
            if (target.distanceSquaredTo(loc) <= 8) {
                attackScores[9] += 50;
            }
            attackScores[9] += 25;
        }
        loc = G.me.translate(0, -2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[10] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[10] += 100;
                }
            }
            if (target.distanceSquaredTo(loc) <= 8) {
                attackScores[10] += 50;
            }
            attackScores[10] += 25;
        }
        loc = G.me.translate(0, 2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[11] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[11] += 100;
                }
            }
            if (target.distanceSquaredTo(loc) <= 8) {
                attackScores[11] += 50;
            }
            attackScores[11] += 25;
        }
        loc = G.me.translate(2, 0);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[12] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[12] += 100;
                }
            }
            if (target.distanceSquaredTo(loc) <= 8) {
                attackScores[12] += 50;
            }
            attackScores[12] += 25;
        }
        loc = G.me.translate(-2, -1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[13] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[13] += 100;
                }
            }
            if (target.distanceSquaredTo(loc) <= 8) {
                attackScores[13] += 50;
            }
            attackScores[13] += 25;
        }
        loc = G.me.translate(-2, 1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[14] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[14] += 100;
                }
            }
            if (target.distanceSquaredTo(loc) <= 8) {
                attackScores[14] += 50;
            }
            attackScores[14] += 25;
        }
        loc = G.me.translate(-1, -2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[15] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[15] += 100;
                }
            }
            if (target.distanceSquaredTo(loc) <= 8) {
                attackScores[15] += 50;
            }
            attackScores[15] += 25;
        }
        loc = G.me.translate(-1, 2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[16] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[16] += 100;
                }
            }
            if (target.distanceSquaredTo(loc) <= 8) {
                attackScores[16] += 50;
            }
            attackScores[16] += 25;
        }
        loc = G.me.translate(1, -2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[17] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[17] += 100;
                }
            }
            if (target.distanceSquaredTo(loc) <= 8) {
                attackScores[17] += 50;
            }
            attackScores[17] += 25;
        }
        loc = G.me.translate(1, 2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[18] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[18] += 100;
                }
            }
            if (target.distanceSquaredTo(loc) <= 8) {
                attackScores[18] += 50;
            }
            attackScores[18] += 25;
        }
        loc = G.me.translate(2, -1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[19] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[19] += 100;
                }
            }
            if (target.distanceSquaredTo(loc) <= 8) {
                attackScores[19] += 50;
            }
            attackScores[19] += 25;
        }
        loc = G.me.translate(2, 1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[20] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[20] += 100;
                }
            }
            if (target.distanceSquaredTo(loc) <= 8) {
                attackScores[20] += 50;
            }
            attackScores[20] += 25;
        }
        loc = G.me.translate(-2, -2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[21] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[21] += 100;
                }
            }
            if (target.distanceSquaredTo(loc) <= 8) {
                attackScores[21] += 50;
            }
            attackScores[21] += 25;
        }
        loc = G.me.translate(-2, 2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[22] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[22] += 100;
                }
            }
            if (target.distanceSquaredTo(loc) <= 8) {
                attackScores[22] += 50;
            }
            attackScores[22] += 25;
        }
        loc = G.me.translate(2, -2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[23] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[23] += 100;
                }
            }
            if (target.distanceSquaredTo(loc) <= 8) {
                attackScores[23] += 50;
            }
            attackScores[23] += 25;
        }
        loc = G.me.translate(2, 2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[24] += (Math.min(10, bot.paintAmount) + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint())) * 5;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[24] += 100;
                }
            }
            if (target.distanceSquaredTo(loc) <= 8) {
                attackScores[24] += 50;
            }
            attackScores[24] += 25;
        }
    }

    public static void buildSwingScores() throws Exception {
        exploreSwingScores();
    }

    public static void retreatMoveScores() throws Exception {
        Direction bestDir = Motion.retreatDir();
        moveScores = Motion.defaultMicro.micro(bestDir, Motion.retreatLoc);
        moveScores[G.dirOrd(bestDir)] += 50;
        moveScores[G.dirOrd(bestDir.rotateLeft())] += 40;
        moveScores[G.dirOrd(bestDir.rotateRight())] += 40;
    }

    // basically the same as exploreAttackScores but with extra bonus for stealing 5
    // paint
    public static void retreatAttackScores() throws Exception {
        MapLocation loc = G.me;
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[0] += 100;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[0] += 100;
                }
            }
            attackScores[0] += 25;
        }
        loc = G.me.translate(-1, 0);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[1] += 100;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[1] += 100;
                }
            }
            attackScores[1] += 25;
        }
        loc = G.me.translate(0, -1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[2] += 100;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[2] += 100;
                }
            }
            attackScores[2] += 25;
        }
        loc = G.me.translate(0, 1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[3] += 100;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[3] += 100;
                }
            }
            attackScores[3] += 25;
        }
        loc = G.me.translate(1, 0);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[4] += 100;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[4] += 100;
                }
            }
            attackScores[4] += 25;
        }
        loc = G.me.translate(-1, -1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[5] += 100;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[5] += 100;
                }
            }
            attackScores[5] += 25;
        }
        loc = G.me.translate(-1, 1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[6] += 100;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[6] += 100;
                }
            }
            attackScores[6] += 25;
        }
        loc = G.me.translate(1, -1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[7] += 100;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[7] += 100;
                }
            }
            attackScores[7] += 25;
        }
        loc = G.me.translate(1, 1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[8] += 100;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[8] += 100;
                }
            }
            attackScores[8] += 25;
        }
        loc = G.me.translate(-2, 0);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[9] += 100;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[9] += 100;
                }
            }
            attackScores[9] += 25;
        }
        loc = G.me.translate(0, -2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[10] += 100;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[10] += 100;
                }
            }
            attackScores[10] += 25;
        }
        loc = G.me.translate(0, 2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[11] += 100;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[11] += 100;
                }
            }
            attackScores[11] += 25;
        }
        loc = G.me.translate(2, 0);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[12] += 100;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[12] += 100;
                }
            }
            attackScores[12] += 25;
        }
        loc = G.me.translate(-2, -1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[13] += 100;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[13] += 100;
                }
            }
            attackScores[13] += 25;
        }
        loc = G.me.translate(-2, 1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[14] += 100;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[14] += 100;
                }
            }
            attackScores[14] += 25;
        }
        loc = G.me.translate(-1, -2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[15] += 100;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[15] += 100;
                }
            }
            attackScores[15] += 25;
        }
        loc = G.me.translate(-1, 2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[16] += 100;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[16] += 100;
                }
            }
            attackScores[16] += 25;
        }
        loc = G.me.translate(1, -2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[17] += 100;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[17] += 100;
                }
            }
            attackScores[17] += 25;
        }
        loc = G.me.translate(1, 2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[18] += 100;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[18] += 100;
                }
            }
            attackScores[18] += 25;
        }
        loc = G.me.translate(2, -1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[19] += 100;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[19] += 100;
                }
            }
            attackScores[19] += 25;
        }
        loc = G.me.translate(2, 1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[20] += 100;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[20] += 100;
                }
            }
            attackScores[20] += 25;
        }
        loc = G.me.translate(-2, -2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[21] += 100;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[21] += 100;
                }
            }
            attackScores[21] += 25;
        }
        loc = G.me.translate(-2, 2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[22] += 100;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[22] += 100;
                }
            }
            attackScores[22] += 25;
        }
        loc = G.me.translate(2, -2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[23] += 100;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[23] += 100;
                }
            }
            attackScores[23] += 25;
        }
        loc = G.me.translate(2, 2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                attackScores[24] += 100;
                if (bot.paintAmount <= 10 && bot.paintAmount > 0) {
                    // treat freezing bot equivalent to gaining 20 paint
                    attackScores[24] += 100;
                }
            }
            attackScores[24] += 25;
        }
    }

    public static void retreatSwingScores() throws Exception {
        exploreSwingScores();
    }

    public static void tryTransferPaint() throws Exception {
        RobotInfo bestAllyBot = null;
        int lowestAllyPaint = 0;
        for (int i = G.allyRobots.length; --i >= 0;) {
            if (G.allyRobots[i].type.isTowerType()) {
                continue;
            }
            if (G.me.distanceSquaredTo(G.allyRobots[i].location) > 2) {
                continue;
            }
            if (G.allyRobots[i].type == UnitType.MOPPER) {
                continue;
            }
            if (bestAllyBot == null || G.allyRobots[i].paintAmount < lowestAllyPaint) {
                bestAllyBot = G.allyRobots[i];
                lowestAllyPaint = G.allyRobots[i].paintAmount;
            }
        }
        if (bestAllyBot != null) {
            int paint = Math.min(bestAllyBot.getType().paintCapacity - lowestAllyPaint, G.rc.getPaint() - 20);
            if (paint != 0 && G.rc.canTransferPaint(bestAllyBot.location, paint)) {
                G.rc.transferPaint(bestAllyBot.location, paint);
            }
        }
    }
}
