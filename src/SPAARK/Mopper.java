package SPAARK;

import battlecode.common.*;

public class Mopper {
    public static final int EXPLORE = 0;
    public static final int BUILD = 1;
    public static final int RETREAT = 2;
    public static int mode = EXPLORE;

    public static final int BUILD_TIMEOUT = 10;

    public static MapLocation ruinLocation = null; // BUILD mode

    public static int lastBuild = -BUILD_TIMEOUT;

    /**
     * Always:
     * If low on paint, retreat
     * Default to explore mode
     * 
     * Explore:
     * Run around randomly deleting enemy paint if it sees it
     * If near a ruin, go to BUILD mode
     * If mop swing works, do it
     * 
     * Build:
     * Help soldiers mop enemy paint around ruins
     */
    public static void run() throws Exception {
        if (G.rc.getPaint() < Robot.getRetreatPaint()) {
            mode = RETREAT;
        } else if (G.rc.getPaint() > G.rc.getType().paintCapacity * 3 / 4 && mode == RETREAT) {
            mode = EXPLORE;
        }
        int a = Clock.getBytecodeNum();
        switch (mode) {
            case EXPLORE -> exploreCheckMode();
            case BUILD -> buildCheckMode();
        }
        int b = Clock.getBytecodeNum();
        G.indicatorString.append((b - a) + " ");
        // grab directions for micro
        switch (mode) {
            case EXPLORE -> explore();
            case BUILD -> build();
            case RETREAT -> {
                G.indicatorString.append("RETREAT ");
                Robot.retreat();
            }
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
                ruinLocation = locs[i];
                mode = BUILD;
                break;
            }
        }
    }

    public static void buildCheckMode() throws Exception {
        G.indicatorString.append("CHK_B ");
        if (!G.rc.canSenseLocation(ruinLocation) || G.rc.canSenseRobotAtLocation(ruinLocation)
                || G.rc.getNumberTowers() == 25) {
            mode = EXPLORE;
            ruinLocation = null;
        }
    }

    public static void explore() throws Exception {
        G.indicatorString.append("EXPLORE ");
        int[] mopScores = new int[25];
        //consider all possible mop locations
        //TODO: add swingScores
        int start = Clock.getBytecodeNum();
        MapLocation bestBot = null;
        MapLocation bestEmpty = null;
        double bestPaint = -1;
        int bestDist = -1;
        MapLocation microDir = G.me;
        for (int i = G.nearbyMapInfos.length; --i >= 0;) {
            MapInfo info = G.nearbyMapInfos[i];
            MapLocation loc = info.getMapLocation();
            PaintType p = info.getPaint();
            if (p.isEnemy()) {
                microDir = microDir.add(G.me.directionTo(loc));
                if (G.rc.canSenseRobotAtLocation(loc)) {
                    RobotInfo r = G.rc.senseRobotAtLocation(loc);
                    if (r.team == G.opponentTeam) {
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
        int[] moveScores = new int[8];
        Direction dir = Direction.CENTER;
        if (bestEmpty == null && bestBot == null) {
            if (G.me.distanceSquaredTo(microDir) >= 2) {
                dir = Motion.bug2Helper(G.me, microDir, Motion.TOWARDS, 0, 0);
            } else {
                G.indicatorString.append("RAND ");
                dir = Motion.exploreRandomlyLoc();
            }
        } else {
            if (bestBot != null)
                bestEmpty = bestBot;
            if (G.rc.canAttack(bestEmpty))
                G.rc.attack(bestEmpty);
            dir = Motion.bug2Helper(G.me, bestEmpty, Motion.AROUND, 1, 2);
            G.rc.setIndicatorLine(G.me, bestEmpty, 0, 0, 255);
        }
        if (G.rc.onTheMap(microDir))
            G.rc.setIndicatorLine(G.me, microDir, 0, 200, 255);
        G.rc.setIndicatorDot(G.me, 0, 255, 0);
        for (int i = 8; --i >= 0;) {
            if (!G.rc.canMove(G.DIRECTIONS[i])) {
                moveScores[i] = -1000000000;
            }
            if (G.DIRECTIONS[i] == dir) {
                moveScores[i] += 2;
            } else if (G.DIRECTIONS[i] == dir.rotateLeft() || G.DIRECTIONS[i] == dir.rotateRight()) {
                moveScores[i] += 1;
            }
        }
        if (bestBot != null)
            bestEmpty = bestBot;
        // for (int i = 25; --i >= 0;) {
            // MapLocation loc = G.me.translate(G.range20X[i], G.range20Y[i]);
            // if (G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            //     if (G.rc.canSenseRobotAtLocation(loc)) {
            //         //if it's an opponent, they get -1 paint
            //         //if it's an ally, they go from -2 to -1 paint
            //         //in both cases we gain 1 paint
            //         //can't be a tower because it has to be painted
            //         RobotInfo bot = G.rc.senseRobotAtLocation(loc);
            //         mopScores[i] += (1 - bot.paintAmount / (double) bot.type.paintCapacity) * 10;
            //         if (bot.getType() == UnitType.MOPPER) {
            //             //double paint loss on moppers
            //             mopScores[i] *= 2;
            //         }
            //     }
            //     mopScores[i] += 5;
            // }
        // }
        MapLocation loc;
        if (G.rc.senseMapInfo(G.me).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(G.me)) {
                //if it's an opponent, they get -1 paint
                //if it's an ally, they go from -2 to -1 paint
                //in both cases we gain 1 paint
                //can't be a tower because it has to be painted
                RobotInfo bot = G.rc.senseRobotAtLocation(G.me);
                mopScores[0] += (1 - bot.paintAmount / (double) bot.type.paintCapacity) * 10;
                if (bot.getType() == UnitType.MOPPER) {
                    //double paint loss on moppers
                    mopScores[0] *= 2;
                }
            }
            mopScores[0] += 5;
        }
		loc = G.me.translate(-1, 0);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                //if it's an opponent, they get -1 paint
                //if it's an ally, they go from -2 to -1 paint
                //in both cases we gain 1 paint
                //can't be a tower because it has to be painted
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                mopScores[1] += (1 - bot.paintAmount / (double) bot.type.paintCapacity) * 10;
                if (bot.getType() == UnitType.MOPPER) {
                    //double paint loss on moppers
                    mopScores[1] *= 2;
                }
            }
            mopScores[1] += 5;
        }
		loc = G.me.translate(0, -1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                //if it's an opponent, they get -1 paint
                //if it's an ally, they go from -2 to -1 paint
                //in both cases we gain 1 paint
                //can't be a tower because it has to be painted
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                mopScores[2] += (1 - bot.paintAmount / (double) bot.type.paintCapacity) * 10;
                if (bot.getType() == UnitType.MOPPER) {
                    //double paint loss on moppers
                    mopScores[2] *= 2;
                }
            }
            mopScores[2] += 5;
        }
		loc = G.me.translate(0, 1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                //if it's an opponent, they get -1 paint
                //if it's an ally, they go from -2 to -1 paint
                //in both cases we gain 1 paint
                //can't be a tower because it has to be painted
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                mopScores[3] += (1 - bot.paintAmount / (double) bot.type.paintCapacity) * 10;
                if (bot.getType() == UnitType.MOPPER) {
                    //double paint loss on moppers
                    mopScores[3] *= 2;
                }
            }
            mopScores[3] += 5;
        }
		loc = G.me.translate(1, 0);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                //if it's an opponent, they get -1 paint
                //if it's an ally, they go from -2 to -1 paint
                //in both cases we gain 1 paint
                //can't be a tower because it has to be painted
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                mopScores[4] += (1 - bot.paintAmount / (double) bot.type.paintCapacity) * 10;
                if (bot.getType() == UnitType.MOPPER) {
                    //double paint loss on moppers
                    mopScores[4] *= 2;
                }
            }
            mopScores[4] += 5;
        }
		loc = G.me.translate(-1, -1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                //if it's an opponent, they get -1 paint
                //if it's an ally, they go from -2 to -1 paint
                //in both cases we gain 1 paint
                //can't be a tower because it has to be painted
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                mopScores[5] += (1 - bot.paintAmount / (double) bot.type.paintCapacity) * 10;
                if (bot.getType() == UnitType.MOPPER) {
                    //double paint loss on moppers
                    mopScores[5] *= 2;
                }
            }
            mopScores[5] += 5;
        }
		loc = G.me.translate(-1, 1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                //if it's an opponent, they get -1 paint
                //if it's an ally, they go from -2 to -1 paint
                //in both cases we gain 1 paint
                //can't be a tower because it has to be painted
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                mopScores[6] += (1 - bot.paintAmount / (double) bot.type.paintCapacity) * 10;
                if (bot.getType() == UnitType.MOPPER) {
                    //double paint loss on moppers
                    mopScores[6] *= 2;
                }
            }
            mopScores[6] += 5;
        }
		loc = G.me.translate(1, -1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                //if it's an opponent, they get -1 paint
                //if it's an ally, they go from -2 to -1 paint
                //in both cases we gain 1 paint
                //can't be a tower because it has to be painted
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                mopScores[7] += (1 - bot.paintAmount / (double) bot.type.paintCapacity) * 10;
                if (bot.getType() == UnitType.MOPPER) {
                    //double paint loss on moppers
                    mopScores[7] *= 2;
                }
            }
            mopScores[7] += 5;
        }
		loc = G.me.translate(1, 1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                //if it's an opponent, they get -1 paint
                //if it's an ally, they go from -2 to -1 paint
                //in both cases we gain 1 paint
                //can't be a tower because it has to be painted
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                mopScores[8] += (1 - bot.paintAmount / (double) bot.type.paintCapacity) * 10;
                if (bot.getType() == UnitType.MOPPER) {
                    //double paint loss on moppers
                    mopScores[8] *= 2;
                }
            }
            mopScores[8] += 5;
        }
		loc = G.me.translate(-2, 0);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                //if it's an opponent, they get -1 paint
                //if it's an ally, they go from -2 to -1 paint
                //in both cases we gain 1 paint
                //can't be a tower because it has to be painted
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                mopScores[9] += (1 - bot.paintAmount / (double) bot.type.paintCapacity) * 10;
                if (bot.getType() == UnitType.MOPPER) {
                    //double paint loss on moppers
                    mopScores[9] *= 2;
                }
            }
            mopScores[9] += 5;
        }
		loc = G.me.translate(0, -2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                //if it's an opponent, they get -1 paint
                //if it's an ally, they go from -2 to -1 paint
                //in both cases we gain 1 paint
                //can't be a tower because it has to be painted
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                mopScores[10] += (1 - bot.paintAmount / (double) bot.type.paintCapacity) * 10;
                if (bot.getType() == UnitType.MOPPER) {
                    //double paint loss on moppers
                    mopScores[10] *= 2;
                }
            }
            mopScores[10] += 5;
        }
		loc = G.me.translate(0, 2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                //if it's an opponent, they get -1 paint
                //if it's an ally, they go from -2 to -1 paint
                //in both cases we gain 1 paint
                //can't be a tower because it has to be painted
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                mopScores[11] += (1 - bot.paintAmount / (double) bot.type.paintCapacity) * 10;
                if (bot.getType() == UnitType.MOPPER) {
                    //double paint loss on moppers
                    mopScores[11] *= 2;
                }
            }
            mopScores[11] += 5;
        }
		loc = G.me.translate(2, 0);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                //if it's an opponent, they get -1 paint
                //if it's an ally, they go from -2 to -1 paint
                //in both cases we gain 1 paint
                //can't be a tower because it has to be painted
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                mopScores[12] += (1 - bot.paintAmount / (double) bot.type.paintCapacity) * 10;
                if (bot.getType() == UnitType.MOPPER) {
                    //double paint loss on moppers
                    mopScores[12] *= 2;
                }
            }
            mopScores[12] += 5;
        }
		loc = G.me.translate(-2, -1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                //if it's an opponent, they get -1 paint
                //if it's an ally, they go from -2 to -1 paint
                //in both cases we gain 1 paint
                //can't be a tower because it has to be painted
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                mopScores[13] += (1 - bot.paintAmount / (double) bot.type.paintCapacity) * 10;
                if (bot.getType() == UnitType.MOPPER) {
                    //double paint loss on moppers
                    mopScores[13] *= 2;
                }
            }
            mopScores[13] += 5;
        }
		loc = G.me.translate(-2, 1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                //if it's an opponent, they get -1 paint
                //if it's an ally, they go from -2 to -1 paint
                //in both cases we gain 1 paint
                //can't be a tower because it has to be painted
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                mopScores[14] += (1 - bot.paintAmount / (double) bot.type.paintCapacity) * 10;
                if (bot.getType() == UnitType.MOPPER) {
                    //double paint loss on moppers
                    mopScores[14] *= 2;
                }
            }
            mopScores[14] += 5;
        }
		loc = G.me.translate(-1, -2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                //if it's an opponent, they get -1 paint
                //if it's an ally, they go from -2 to -1 paint
                //in both cases we gain 1 paint
                //can't be a tower because it has to be painted
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                mopScores[15] += (1 - bot.paintAmount / (double) bot.type.paintCapacity) * 10;
                if (bot.getType() == UnitType.MOPPER) {
                    //double paint loss on moppers
                    mopScores[15] *= 2;
                }
            }
            mopScores[15] += 5;
        }
		loc = G.me.translate(-1, 2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                //if it's an opponent, they get -1 paint
                //if it's an ally, they go from -2 to -1 paint
                //in both cases we gain 1 paint
                //can't be a tower because it has to be painted
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                mopScores[16] += (1 - bot.paintAmount / (double) bot.type.paintCapacity) * 10;
                if (bot.getType() == UnitType.MOPPER) {
                    //double paint loss on moppers
                    mopScores[16] *= 2;
                }
            }
            mopScores[16] += 5;
        }
		loc = G.me.translate(1, -2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                //if it's an opponent, they get -1 paint
                //if it's an ally, they go from -2 to -1 paint
                //in both cases we gain 1 paint
                //can't be a tower because it has to be painted
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                mopScores[17] += (1 - bot.paintAmount / (double) bot.type.paintCapacity) * 10;
                if (bot.getType() == UnitType.MOPPER) {
                    //double paint loss on moppers
                    mopScores[17] *= 2;
                }
            }
            mopScores[17] += 5;
        }
		loc = G.me.translate(1, 2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                //if it's an opponent, they get -1 paint
                //if it's an ally, they go from -2 to -1 paint
                //in both cases we gain 1 paint
                //can't be a tower because it has to be painted
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                mopScores[18] += (1 - bot.paintAmount / (double) bot.type.paintCapacity) * 10;
                if (bot.getType() == UnitType.MOPPER) {
                    //double paint loss on moppers
                    mopScores[18] *= 2;
                }
            }
            mopScores[18] += 5;
        }
		loc = G.me.translate(2, -1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                //if it's an opponent, they get -1 paint
                //if it's an ally, they go from -2 to -1 paint
                //in both cases we gain 1 paint
                //can't be a tower because it has to be painted
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                mopScores[19] += (1 - bot.paintAmount / (double) bot.type.paintCapacity) * 10;
                if (bot.getType() == UnitType.MOPPER) {
                    //double paint loss on moppers
                    mopScores[19] *= 2;
                }
            }
            mopScores[19] += 5;
        }
		loc = G.me.translate(2, 1);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                //if it's an opponent, they get -1 paint
                //if it's an ally, they go from -2 to -1 paint
                //in both cases we gain 1 paint
                //can't be a tower because it has to be painted
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                mopScores[20] += (1 - bot.paintAmount / (double) bot.type.paintCapacity) * 10;
                if (bot.getType() == UnitType.MOPPER) {
                    //double paint loss on moppers
                    mopScores[20] *= 2;
                }
            }
            mopScores[20] += 5;
        }
		loc = G.me.translate(-2, -2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                //if it's an opponent, they get -1 paint
                //if it's an ally, they go from -2 to -1 paint
                //in both cases we gain 1 paint
                //can't be a tower because it has to be painted
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                mopScores[21] += (1 - bot.paintAmount / (double) bot.type.paintCapacity) * 10;
                if (bot.getType() == UnitType.MOPPER) {
                    //double paint loss on moppers
                    mopScores[21] *= 2;
                }
            }
            mopScores[21] += 5;
        }
		loc = G.me.translate(-2, 2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                //if it's an opponent, they get -1 paint
                //if it's an ally, they go from -2 to -1 paint
                //in both cases we gain 1 paint
                //can't be a tower because it has to be painted
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                mopScores[22] += (1 - bot.paintAmount / (double) bot.type.paintCapacity) * 10;
                if (bot.getType() == UnitType.MOPPER) {
                    //double paint loss on moppers
                    mopScores[22] *= 2;
                }
            }
            mopScores[22] += 5;
        }
		loc = G.me.translate(2, -2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                //if it's an opponent, they get -1 paint
                //if it's an ally, they go from -2 to -1 paint
                //in both cases we gain 1 paint
                //can't be a tower because it has to be painted
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                mopScores[23] += (1 - bot.paintAmount / (double) bot.type.paintCapacity) * 10;
                if (bot.getType() == UnitType.MOPPER) {
                    //double paint loss on moppers
                    mopScores[23] *= 2;
                }
            }
            mopScores[23] += 5;
        }
		loc = G.me.translate(2, 2);
        if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
            if (G.rc.canSenseRobotAtLocation(loc)) {
                //if it's an opponent, they get -1 paint
                //if it's an ally, they go from -2 to -1 paint
                //in both cases we gain 1 paint
                //can't be a tower because it has to be painted
                RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                mopScores[24] += (1 - bot.paintAmount / (double) bot.type.paintCapacity) * 10;
                if (bot.getType() == UnitType.MOPPER) {
                    //double paint loss on moppers
                    mopScores[24] *= 2;
                }
            }
            mopScores[24] += 5;
        }

        //max of the 9 squares at the center
        int cmax = mopScores[0];
        int cx = 0;
        int cy = 0;
		if (mopScores[1] > cmax) {
			cmax = mopScores[1];
			cx = -1;
			cy = 0;
		}
		if (mopScores[2] > cmax) {
			cmax = mopScores[2];
			cx = 0;
			cy = -1;
		}
		if (mopScores[3] > cmax) {
			cmax = mopScores[3];
			cx = 0;
			cy = 1;
		}
		if (mopScores[4] > cmax) {
			cmax = mopScores[4];
			cx = 1;
			cy = 0;
		}
		if (mopScores[5] > cmax) {
			cmax = mopScores[5];
			cx = -1;
			cy = -1;
		}
		if (mopScores[6] > cmax) {
			cmax = mopScores[6];
			cx = -1;
			cy = 1;
		}
		if (mopScores[7] > cmax) {
			cmax = mopScores[7];
			cx = 1;
			cy = -1;
		}
		if (mopScores[8] > cmax) {
			cmax = mopScores[8];
			cx = 1;
			cy = 1;
		}
        int[] allmax = new int[]{
            cmax+moveScores[0],cmax+moveScores[1],cmax+moveScores[2],cmax+moveScores[3],cmax+moveScores[4],cmax+moveScores[5],cmax+moveScores[6],cmax+moveScores[7],cmax
        };
        int[] allx = new int[]{
            cx,cx,cx,cx,cx,cx,cx,cx,cx
        };
        int[] ally = new int[]{
            cy,cy,cy,cy,cy,cy,cy,cy,cy
        };
        if (mopScores[21] > allmax[0]) {
			allmax[0] = mopScores[21];
			allx[0] = -2;
			ally[0] = -2;
		}
		if (mopScores[13] > allmax[0]) {
			allmax[0] = mopScores[13];
			allx[0] = -2;
			ally[0] = -1;
		}
		if (mopScores[9] > allmax[0]) {
			allmax[0] = mopScores[9];
			allx[0] = -2;
			ally[0] = 0;
		}
		if (mopScores[15] > allmax[0]) {
			allmax[0] = mopScores[15];
			allx[0] = -1;
			ally[0] = -2;
		}
		if (mopScores[10] > allmax[0]) {
			allmax[0] = mopScores[10];
			allx[0] = 0;
			ally[0] = -2;
		}
		if (mopScores[15] > allmax[1]) {
			allmax[1] = mopScores[15];
			allx[1] = -1;
			ally[1] = -2;
		}
		if (mopScores[10] > allmax[1]) {
			allmax[1] = mopScores[10];
			allx[1] = 0;
			ally[1] = -2;
		}
		if (mopScores[17] > allmax[1]) {
			allmax[1] = mopScores[17];
			allx[1] = 1;
			ally[1] = -2;
		}
		if (mopScores[10] > allmax[2]) {
			allmax[2] = mopScores[10];
			allx[2] = 0;
			ally[2] = -2;
		}
		if (mopScores[17] > allmax[2]) {
			allmax[2] = mopScores[17];
			allx[2] = 1;
			ally[2] = -2;
		}
		if (mopScores[23] > allmax[2]) {
			allmax[2] = mopScores[23];
			allx[2] = 2;
			ally[2] = -2;
		}
		if (mopScores[19] > allmax[2]) {
			allmax[2] = mopScores[19];
			allx[2] = 2;
			ally[2] = -1;
		}
		if (mopScores[12] > allmax[2]) {
			allmax[2] = mopScores[12];
			allx[2] = 2;
			ally[2] = 0;
		}
		if (mopScores[13] > allmax[3]) {
			allmax[3] = mopScores[13];
			allx[3] = -2;
			ally[3] = -1;
		}
		if (mopScores[9] > allmax[3]) {
			allmax[3] = mopScores[9];
			allx[3] = -2;
			ally[3] = 0;
		}
		if (mopScores[14] > allmax[3]) {
			allmax[3] = mopScores[14];
			allx[3] = -2;
			ally[3] = 1;
		}
		if (mopScores[19] > allmax[4]) {
			allmax[4] = mopScores[19];
			allx[4] = 2;
			ally[4] = -1;
		}
		if (mopScores[12] > allmax[4]) {
			allmax[4] = mopScores[12];
			allx[4] = 2;
			ally[4] = 0;
		}
		if (mopScores[20] > allmax[4]) {
			allmax[4] = mopScores[20];
			allx[4] = 2;
			ally[4] = 1;
		}
		if (mopScores[9] > allmax[5]) {
			allmax[5] = mopScores[9];
			allx[5] = -2;
			ally[5] = 0;
		}
		if (mopScores[14] > allmax[5]) {
			allmax[5] = mopScores[14];
			allx[5] = -2;
			ally[5] = 1;
		}
		if (mopScores[22] > allmax[5]) {
			allmax[5] = mopScores[22];
			allx[5] = -2;
			ally[5] = 2;
		}
		if (mopScores[16] > allmax[5]) {
			allmax[5] = mopScores[16];
			allx[5] = -1;
			ally[5] = 2;
		}
		if (mopScores[11] > allmax[5]) {
			allmax[5] = mopScores[11];
			allx[5] = 0;
			ally[5] = 2;
		}
		if (mopScores[16] > allmax[6]) {
			allmax[6] = mopScores[16];
			allx[6] = -1;
			ally[6] = 2;
		}
		if (mopScores[11] > allmax[6]) {
			allmax[6] = mopScores[11];
			allx[6] = 0;
			ally[6] = 2;
		}
		if (mopScores[18] > allmax[6]) {
			allmax[6] = mopScores[18];
			allx[6] = 1;
			ally[6] = 2;
		}
		if (mopScores[11] > allmax[7]) {
			allmax[7] = mopScores[11];
			allx[7] = 0;
			ally[7] = 2;
		}
		if (mopScores[18] > allmax[7]) {
			allmax[7] = mopScores[18];
			allx[7] = 1;
			ally[7] = 2;
		}
		if (mopScores[12] > allmax[7]) {
			allmax[7] = mopScores[12];
			allx[7] = 2;
			ally[7] = 0;
		}
		if (mopScores[20] > allmax[7]) {
			allmax[7] = mopScores[20];
			allx[7] = 2;
			ally[7] = 1;
		}
		if (mopScores[24] > allmax[7]) {
			allmax[7] = mopScores[24];
			allx[7] = 2;
			ally[7] = 2;
		}
        int best = 8;
        for (int i = 8; --i >= 0;) {
            if (allmax[i]>allmax[best]) {
                best = i;
            }
        }
        MapLocation attackLoc = G.me.translate(allx[best], ally[best]);
        if (G.rc.canAttack(attackLoc)) {
            G.rc.attack(attackLoc);
        }
        Motion.move(G.ALL_DIRECTIONS[best]);
        if (G.rc.canAttack(attackLoc)) {
            G.rc.attack(attackLoc);
        }
    }

    public static void build() throws Exception {
        G.indicatorString.append("BUILD ");
        // clean enemy paint for ruin patterns
        // TODO: FIND AND MOP ENEMY PAINT OFF SRP
        // TODO: FIND AND MOP ENEMY PAINT OFF SRP
        // TODO: FIND AND MOP ENEMY PAINT OFF SRP
        // TODO: FIND AND MOP ENEMY PAINT OFF SRP
        // get 2 best locations to build stuff on
        // so if the first one is already there just go to the next one
        MapLocation bestLoc = null;
        MapLocation bestLoc2 = null;
        int bestDistanceSquared = 10000;
        int bestDistanceSquared2 = 10001;
        for (int i = G.nearbyMapInfos.length; --i >= 0;) {
            if (G.nearbyMapInfos[i].getPaint().isEnemy() && G.nearbyMapInfos[i].getMapLocation().distanceSquaredTo(ruinLocation) <= 8) {
                int distanceSquared = G.nearbyMapInfos[i].getMapLocation().distanceSquaredTo(G.me);
                if (distanceSquared < bestDistanceSquared) {
                    bestDistanceSquared2 = bestDistanceSquared;
                    bestLoc2 = bestLoc;
                    bestDistanceSquared = distanceSquared;
                    bestLoc = G.nearbyMapInfos[i].getMapLocation();
                } else if (distanceSquared < bestDistanceSquared2) {
                    bestDistanceSquared2 = distanceSquared;
                    bestLoc2 = G.nearbyMapInfos[i].getMapLocation();
                }
            }
        }
        if (bestLoc != null) {
            if (G.rc.canAttack(bestLoc)) {
                G.rc.attack(bestLoc);
                if (bestLoc2 != null) {
                    Motion.bugnavTowards(bestLoc2, avoidPaintMicro);
                    G.rc.setIndicatorLine(G.me, bestLoc2, 0, 255, 200);
                } else if (G.me.distanceSquaredTo(ruinLocation) <= 4) {
                    mode = EXPLORE;
                    lastBuild = G.round;
                    Motion.exploreRandomly(avoidPaintMicro);
                    G.rc.setIndicatorLine(G.rc.getLocation(), ruinLocation, 0, 0, 0);
                    ruinLocation = null;
                } else {
                    Motion.bugnavTowards(ruinLocation, avoidPaintMicro);
                }
            } else {
                Motion.bugnavTowards(bestLoc, avoidPaintMicro);
            }
            G.rc.setIndicatorLine(G.me, bestLoc, 0, 255, 255);
        } else if (G.me.distanceSquaredTo(ruinLocation) <= 4) {
            mode = EXPLORE;
            lastBuild = G.round;
            Motion.exploreRandomly(avoidPaintMicro);
            G.rc.setIndicatorLine(G.rc.getLocation(), ruinLocation, 0, 0, 0);
            ruinLocation = null;
        } else {
            Motion.bugnavTowards(ruinLocation, avoidPaintMicro);
        }
        G.rc.setIndicatorDot(G.me, 0, 0, 255);
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
            if (r != null && r.team == G.opponentTeam)
                up++;
        }
        if (G.rc.onTheMap(G.me.add(Direction.NORTHEAST))) {
            r = G.rc.senseRobotAtLocation(G.me.add(Direction.NORTHEAST));
            if (r != null && r.team == G.opponentTeam) {
                up++;
                right++;
            }
        }
        if (G.rc.onTheMap(G.me.add(Direction.EAST))) {
            r = G.rc.senseRobotAtLocation(G.me.add(Direction.EAST));
            if (r != null && r.team == G.opponentTeam)
                right++;
        }
        if (G.rc.onTheMap(G.me.add(Direction.SOUTHEAST))) {
            r = G.rc.senseRobotAtLocation(G.me.add(Direction.SOUTHEAST));
            if (r != null && r.team == G.opponentTeam) {
                down++;
                right++;
            }
        }
        if (G.rc.onTheMap(G.me.add(Direction.SOUTH))) {
            r = G.rc.senseRobotAtLocation(G.me.add(Direction.SOUTH));
            if (r != null && r.team == G.opponentTeam)
                down++;
        }
        if (G.rc.onTheMap(G.me.add(Direction.SOUTHWEST))) {
            r = G.rc.senseRobotAtLocation(G.me.add(Direction.SOUTHWEST));
            if (r != null && r.team == G.opponentTeam) {
                down++;
                left++;
            }
        }
        if (G.rc.onTheMap(G.me.add(Direction.WEST))) {
            r = G.rc.senseRobotAtLocation(G.me.add(Direction.WEST));
            if (r != null && r.team == G.opponentTeam)
                left++;
        }
        if (G.rc.onTheMap(G.me.add(Direction.NORTHWEST))) {
            r = G.rc.senseRobotAtLocation(G.me.add(Direction.NORTHWEST));
            if (r != null && r.team == G.opponentTeam) {
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

    public static Micro avoidPaintMicro = new Micro() {
        @Override
        public int[] micro(Direction d, MapLocation dest) throws Exception {
            // REALLY avoid being on enemy paint
            int[] scores = Motion.defaultMicro.micro(d, dest);
            for (int i = 8; --i >= 0;) {
                if (G.rc.canMove(G.DIRECTIONS[i])) {
                    MapLocation m = G.me.add(G.DIRECTIONS[i]);
                    PaintType paint = G.rc.senseMapInfo(m).getPaint();
                    if (paint.isEnemy()) scores[i] -= 10;
                    if (paint == PaintType.EMPTY) scores[i] -= 5;
                }
            }
            //run away from towers
            MapLocation[] ruins = G.rc.senseNearbyRuins(-1);
            MapLocation a = G.me;
            for (int i = ruins.length; --i >= 0;) {
                if (G.rc.canSenseRobotAtLocation(ruins[i]) && G.rc.senseRobotAtLocation(ruins[i]).team == G.opponentTeam) {
                    a = a.add(ruins[i].directionTo(G.me));
                }
            }
            Direction ruinDir = G.me.directionTo(a);
            for (int i = 8; --i >= 0;) {
                if (G.DIRECTIONS[i] == ruinDir) {
                    scores[i] += 10;
                    break;
                }
            }
            return scores;
        }
    };
}
