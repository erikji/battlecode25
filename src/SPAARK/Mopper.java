package SPAARK;

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
    public static int[] swingScores = new int[36]; //swinging
    //[south, west, east, north] for each swingScore

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
        if (!G.rc.isActionReady()) {
            for (int i = 36; --i >= 25;) {
                swingScores[i] = 0;
            }
            for (int i = 25; --i >= 0;) {
                attackScores[i] = 0;
                swingScores[i] = 0;
            }
        }
        if (!G.rc.isMovementReady()) {
            for (int i = 9; --i >= 0;) {
                moveScores[i] = 0;
            }
        }
		boolean swing = false; //whether our attack will be a swing
        int cmax = attackScores[0];
        int cx = 0; //if it's a swing, then also store index of swing direction
        int cy = 0;
        // check every tile within sqrt2 radius
        if (attackScores[1] > cmax) {
            cmax = attackScores[1];
            cx = -1;
            cy = 0;
			swing = false;
        }
        if (attackScores[2] > cmax) {
            cmax = attackScores[2];
            cx = 0;
            cy = -1;
			swing = false;
        }
        if (attackScores[3] > cmax) {
            cmax = attackScores[3];
            cx = 0;
            cy = 1;
			swing = false;
        }
        if (attackScores[4] > cmax) {
            cmax = attackScores[4];
            cx = 1;
            cy = 0;
			swing = false;
        }
        if (attackScores[5] > cmax) {
            cmax = attackScores[5];
            cx = -1;
            cy = -1;
			swing = false;
        }
        if (attackScores[6] > cmax) {
            cmax = attackScores[6];
            cx = -1;
            cy = 1;
			swing = false;
        }
        if (attackScores[7] > cmax) {
            cmax = attackScores[7];
            cx = 1;
            cy = -1;
			swing = false;
        }
        if (attackScores[8] > cmax) {
            cmax = attackScores[8];
            cx = 1;
            cy = 1;
			swing = false;
        }
		if (swingScores[32] > cmax) {
			cmax = swingScores[32];
			cx = 1;
			swing = true;
		}
		if (swingScores[33] > cmax) {
			cmax = swingScores[33];
			cx = 3;
			swing = true;
		}
		if (swingScores[34] > cmax) {
			cmax = swingScores[34];
			cx = 4;
			swing = true;
		}
		if (swingScores[35] > cmax) {
			cmax = swingScores[35];
			cx = 6;
			swing = true;
		}
        // store total score and best attack location for each direction (incl
        // Direction.CENTER)
        int[] allmax = new int[] {
                cmax, cmax, cmax, cmax, cmax, cmax, cmax, cmax, cmax
        };
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
		if (attackScores[13] > allmax[3]) {
			allmax[3] = attackScores[13];
			allx[3] = -2;
			ally[3] = -1;
			allswing[3] = false;
		}
		if (attackScores[9] > allmax[3]) {
			allmax[3] = attackScores[9];
			allx[3] = -2;
			ally[3] = 0;
			allswing[3] = false;
		}
		if (attackScores[14] > allmax[3]) {
			allmax[3] = attackScores[14];
			allx[3] = -2;
			ally[3] = 1;
			allswing[3] = false;
		}
		if (attackScores[19] > allmax[4]) {
			allmax[4] = attackScores[19];
			allx[4] = 2;
			ally[4] = -1;
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
		if (attackScores[9] > allmax[5]) {
			allmax[5] = attackScores[9];
			allx[5] = -2;
			ally[5] = 0;
			allswing[5] = false;
		}
		if (attackScores[14] > allmax[5]) {
			allmax[5] = attackScores[14];
			allx[5] = -2;
			ally[5] = 1;
			allswing[5] = false;
		}
		if (attackScores[22] > allmax[5]) {
			allmax[5] = attackScores[22];
			allx[5] = -2;
			ally[5] = 2;
			allswing[5] = false;
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
		if (attackScores[18] > allmax[6]) {
			allmax[6] = attackScores[18];
			allx[6] = 1;
			ally[6] = 2;
			allswing[6] = false;
		}
		if (attackScores[11] > allmax[7]) {
			allmax[7] = attackScores[11];
			allx[7] = 0;
			ally[7] = 2;
			allswing[7] = false;
		}
		if (attackScores[18] > allmax[7]) {
			allmax[7] = attackScores[18];
			allx[7] = 1;
			ally[7] = 2;
			allswing[7] = false;
		}
		if (attackScores[12] > allmax[7]) {
			allmax[7] = attackScores[12];
			allx[7] = 2;
			ally[7] = 0;
			allswing[7] = false;
		}
		if (attackScores[20] > allmax[7]) {
			allmax[7] = attackScores[20];
			allx[7] = 2;
			ally[7] = 1;
			allswing[7] = false;
		}
		if (attackScores[24] > allmax[7]) {
			allmax[7] = attackScores[24];
			allx[7] = 2;
			ally[7] = 2;
			allswing[7] = false;
		}
		if (swingScores[0] > allmax[0]) {
			allmax[0] = swingScores[0];
			allx[0] = 1;
			allswing[0] = true;
		}
		if (swingScores[1] > allmax[0]) {
			allmax[0] = swingScores[1];
			allx[0] = 3;
			allswing[0] = true;
		}
		if (swingScores[2] > allmax[0]) {
			allmax[0] = swingScores[2];
			allx[0] = 4;
			allswing[0] = true;
		}
		if (swingScores[3] > allmax[0]) {
			allmax[0] = swingScores[3];
			allx[0] = 6;
			allswing[0] = true;
		}
		if (swingScores[4] > allmax[1]) {
			allmax[1] = swingScores[4];
			allx[1] = 1;
			allswing[1] = true;
		}
		if (swingScores[5] > allmax[1]) {
			allmax[1] = swingScores[5];
			allx[1] = 3;
			allswing[1] = true;
		}
		if (swingScores[6] > allmax[1]) {
			allmax[1] = swingScores[6];
			allx[1] = 4;
			allswing[1] = true;
		}
		if (swingScores[7] > allmax[1]) {
			allmax[1] = swingScores[7];
			allx[1] = 6;
			allswing[1] = true;
		}
		if (swingScores[8] > allmax[2]) {
			allmax[2] = swingScores[8];
			allx[2] = 1;
			allswing[2] = true;
		}
		if (swingScores[9] > allmax[2]) {
			allmax[2] = swingScores[9];
			allx[2] = 3;
			allswing[2] = true;
		}
		if (swingScores[10] > allmax[2]) {
			allmax[2] = swingScores[10];
			allx[2] = 4;
			allswing[2] = true;
		}
		if (swingScores[11] > allmax[2]) {
			allmax[2] = swingScores[11];
			allx[2] = 6;
			allswing[2] = true;
		}
		if (swingScores[12] > allmax[3]) {
			allmax[3] = swingScores[12];
			allx[3] = 1;
			allswing[3] = true;
		}
		if (swingScores[13] > allmax[3]) {
			allmax[3] = swingScores[13];
			allx[3] = 3;
			allswing[3] = true;
		}
		if (swingScores[14] > allmax[3]) {
			allmax[3] = swingScores[14];
			allx[3] = 4;
			allswing[3] = true;
		}
		if (swingScores[15] > allmax[3]) {
			allmax[3] = swingScores[15];
			allx[3] = 6;
			allswing[3] = true;
		}
		if (swingScores[16] > allmax[4]) {
			allmax[4] = swingScores[16];
			allx[4] = 1;
			allswing[4] = true;
		}
		if (swingScores[17] > allmax[4]) {
			allmax[4] = swingScores[17];
			allx[4] = 3;
			allswing[4] = true;
		}
		if (swingScores[18] > allmax[4]) {
			allmax[4] = swingScores[18];
			allx[4] = 4;
			allswing[4] = true;
		}
		if (swingScores[19] > allmax[4]) {
			allmax[4] = swingScores[19];
			allx[4] = 6;
			allswing[4] = true;
		}
		if (swingScores[20] > allmax[5]) {
			allmax[5] = swingScores[20];
			allx[5] = 1;
			allswing[5] = true;
		}
		if (swingScores[21] > allmax[5]) {
			allmax[5] = swingScores[21];
			allx[5] = 3;
			allswing[5] = true;
		}
		if (swingScores[22] > allmax[5]) {
			allmax[5] = swingScores[22];
			allx[5] = 4;
			allswing[5] = true;
		}
		if (swingScores[23] > allmax[5]) {
			allmax[5] = swingScores[23];
			allx[5] = 6;
			allswing[5] = true;
		}
		if (swingScores[24] > allmax[6]) {
			allmax[6] = swingScores[24];
			allx[6] = 1;
			allswing[6] = true;
		}
		if (swingScores[25] > allmax[6]) {
			allmax[6] = swingScores[25];
			allx[6] = 3;
			allswing[6] = true;
		}
		if (swingScores[26] > allmax[6]) {
			allmax[6] = swingScores[26];
			allx[6] = 4;
			allswing[6] = true;
		}
		if (swingScores[27] > allmax[6]) {
			allmax[6] = swingScores[27];
			allx[6] = 6;
			allswing[6] = true;
		}
		if (swingScores[28] > allmax[7]) {
			allmax[7] = swingScores[28];
			allx[7] = 1;
			allswing[7] = true;
		}
		if (swingScores[29] > allmax[7]) {
			allmax[7] = swingScores[29];
			allx[7] = 3;
			allswing[7] = true;
		}
		if (swingScores[30] > allmax[7]) {
			allmax[7] = swingScores[30];
			allx[7] = 4;
			allswing[7] = true;
		}
		if (swingScores[31] > allmax[7]) {
			allmax[7] = swingScores[31];
			allx[7] = 6;
			allswing[7] = true;
		}
        // copyspaghetti from Motion.microMove but whatever
        int best = 8;
        int numBest = 1;
        for (int i = 8; --i >= 0;) {
            if (allmax[i] + moveScores[i] > allmax[best] + moveScores[best]) {
                best = i;
                numBest = 1;
            } else if (allmax[i] + moveScores[i] == allmax[best] + moveScores[best] && Random.rand() % ++numBest == 0) {
                best = i;
            }
        }
        // try attack then move then attack again
		if (allswing[best]) {
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
		} else {
			MapLocation attackLoc = G.me.translate(allx[best], ally[best]);
			if (G.rc.canAttack(attackLoc)) {
				G.rc.attack(attackLoc);
			}
			Motion.move(G.ALL_DIRECTIONS[best]);
			if (G.rc.canAttack(attackLoc)) {
				G.rc.attack(attackLoc);
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

    public static void explore() throws Exception {
        G.indicatorString.append("EXPLORE ");
        if (G.rc.isMovementReady()) {
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
            Direction dir = Direction.CENTER;
            if (bestEmpty == null && bestBot == null) {
                G.indicatorString.append("RAND ");
                dir = G.me.directionTo(Motion.exploreRandomlyLoc());
            } else {
                if (bestBot != null)
                    bestEmpty = bestBot;
                dir = Motion.bug2Helper(G.me, bestEmpty, Motion.AROUND, 1, 2);
                G.rc.setIndicatorLine(G.me, bestEmpty, 0, 0, 255);
            }
            moveScores = avoidPaintMicro.micro(dir, G.invalidLoc);
            if (G.rc.onTheMap(microDir))
                G.rc.setIndicatorLine(G.me, microDir, 0, 200, 255);
        }
        if (G.rc.isActionReady()) {
            StringBuilder opponentRobotsString = new StringBuilder();
            for (int i = G.opponentRobots.length; --i >= 0;) {
				if (G.opponentRobots[i].type.isRobotType()) {
					opponentRobotsString.append(G.opponentRobots[i].location);
				}
            }
            for (int i = 25; --i >= 0;) {
                attackScores[i] = 0;
                MapLocation loc = G.me.translate(G.range20X[i], G.range20Y[i]);
                if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
                    if (G.rc.canSenseRobotAtLocation(loc)) {
                        // if it's an opponent, they get -1 paint
                        // if it's an ally, they go from -2 to -1 paint
                        // in both cases we gain 1 paint
                        // can't be a tower because it has to be painted
                        RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                        // maybe also incentize mopping enemies with low paint
                        attackScores[i] += 11 + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint());
                        if (bot.getType() == UnitType.MOPPER) {
                            // double paint loss on moppers
                            attackScores[i]++;
                        }
                        if (bot.paintAmount <= 10) {
                            //treat freezing bot equivalent to gaining 20 paint
                            attackScores[i] += 20;
                        }
                    }
                    attackScores[i] += 5;
                    attackScores[i] *= 5;
                }
            }
            swingScores[0] = 0;
			swingScores[1] = 0;
			swingScores[2] = 0;
			swingScores[3] = 0;
			swingScores[4] = 0;
			swingScores[5] = 0;
			swingScores[6] = 0;
			swingScores[7] = 0;
			swingScores[8] = 0;
			swingScores[9] = 0;
			swingScores[10] = 0;
			swingScores[11] = 0;
			swingScores[12] = 0;
			swingScores[13] = 0;
			swingScores[14] = 0;
			swingScores[15] = 0;
			swingScores[16] = 0;
			swingScores[17] = 0;
			swingScores[18] = 0;
			swingScores[19] = 0;
			swingScores[20] = 0;
			swingScores[21] = 0;
			swingScores[22] = 0;
			swingScores[23] = 0;
			swingScores[24] = 0;
			swingScores[25] = 0;
			swingScores[26] = 0;
			swingScores[27] = 0;
			swingScores[28] = 0;
			swingScores[29] = 0;
			swingScores[30] = 0;
			swingScores[31] = 0;
			swingScores[32] = 0;
			swingScores[33] = 0;
			swingScores[34] = 0;
			swingScores[35] = 0;
			if (opponentRobotsString.indexOf("["+(G.me.x-1)+", "+(G.me.y-2)+"]") != -1) {
				swingScores[32] += 35;
				swingScores[12] += 35;
				swingScores[9] += 35;
				swingScores[5] += 35;
				swingScores[4] += 35;
				swingScores[0] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x-1)+", "+(G.me.y-3)+"]") != -1) {
				swingScores[4] += 35;
				swingScores[0] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x-2)+", "+(G.me.y-2)+"]") != -1) {
				swingScores[12] += 35;
				swingScores[5] += 35;
				swingScores[1] += 35;
				swingScores[0] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x-2)+", "+(G.me.y-3)+"]") != -1) {
				swingScores[0] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x+0)+", "+(G.me.y-2)+"]") != -1) {
				swingScores[32] += 35;
				swingScores[16] += 35;
				swingScores[12] += 35;
				swingScores[9] += 35;
				swingScores[8] += 35;
				swingScores[4] += 35;
				swingScores[2] += 35;
				swingScores[0] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x+0)+", "+(G.me.y-3)+"]") != -1) {
				swingScores[8] += 35;
				swingScores[4] += 35;
				swingScores[0] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x-2)+", "+(G.me.y-1)+"]") != -1) {
				swingScores[33] += 35;
				swingScores[20] += 35;
				swingScores[13] += 35;
				swingScores[12] += 35;
				swingScores[5] += 35;
				swingScores[1] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x-3)+", "+(G.me.y-1)+"]") != -1) {
				swingScores[13] += 35;
				swingScores[1] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x-3)+", "+(G.me.y-2)+"]") != -1) {
				swingScores[1] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x-2)+", "+(G.me.y+0)+"]") != -1) {
				swingScores[33] += 35;
				swingScores[25] += 35;
				swingScores[21] += 35;
				swingScores[20] += 35;
				swingScores[13] += 35;
				swingScores[5] += 35;
				swingScores[3] += 35;
				swingScores[1] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x-3)+", "+(G.me.y+0)+"]") != -1) {
				swingScores[21] += 35;
				swingScores[13] += 35;
				swingScores[1] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x+0)+", "+(G.me.y-1)+"]") != -1) {
				swingScores[32] += 35;
				swingScores[28] += 35;
				swingScores[24] += 35;
				swingScores[20] += 35;
				swingScores[17] += 35;
				swingScores[16] += 35;
				swingScores[14] += 35;
				swingScores[12] += 35;
				swingScores[9] += 35;
				swingScores[2] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x+1)+", "+(G.me.y-1)+"]") != -1) {
				swingScores[34] += 35;
				swingScores[32] += 35;
				swingScores[28] += 35;
				swingScores[24] += 35;
				swingScores[16] += 35;
				swingScores[14] += 35;
				swingScores[6] += 35;
				swingScores[2] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x+0)+", "+(G.me.y+0)+"]") != -1) {
				swingScores[29] += 35;
				swingScores[28] += 35;
				swingScores[24] += 35;
				swingScores[22] += 35;
				swingScores[20] += 35;
				swingScores[17] += 35;
				swingScores[14] += 35;
				swingScores[11] += 35;
				swingScores[9] += 35;
				swingScores[7] += 35;
				swingScores[3] += 35;
				swingScores[2] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x+1)+", "+(G.me.y+0)+"]") != -1) {
				swingScores[34] += 35;
				swingScores[28] += 35;
				swingScores[26] += 35;
				swingScores[24] += 35;
				swingScores[22] += 35;
				swingScores[14] += 35;
				swingScores[11] += 35;
				swingScores[7] += 35;
				swingScores[6] += 35;
				swingScores[2] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x+1)+", "+(G.me.y-2)+"]") != -1) {
				swingScores[32] += 35;
				swingScores[16] += 35;
				swingScores[8] += 35;
				swingScores[6] += 35;
				swingScores[4] += 35;
				swingScores[2] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x-1)+", "+(G.me.y+0)+"]") != -1) {
				swingScores[33] += 35;
				swingScores[29] += 35;
				swingScores[25] += 35;
				swingScores[24] += 35;
				swingScores[20] += 35;
				swingScores[17] += 35;
				swingScores[9] += 35;
				swingScores[7] += 35;
				swingScores[5] += 35;
				swingScores[3] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x-1)+", "+(G.me.y+1)+"]") != -1) {
				swingScores[35] += 35;
				swingScores[33] += 35;
				swingScores[29] += 35;
				swingScores[25] += 35;
				swingScores[17] += 35;
				swingScores[15] += 35;
				swingScores[7] += 35;
				swingScores[3] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x+0)+", "+(G.me.y+1)+"]") != -1) {
				swingScores[35] += 35;
				swingScores[29] += 35;
				swingScores[22] += 35;
				swingScores[19] += 35;
				swingScores[17] += 35;
				swingScores[15] += 35;
				swingScores[14] += 35;
				swingScores[11] += 35;
				swingScores[7] += 35;
				swingScores[3] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x-2)+", "+(G.me.y+1)+"]") != -1) {
				swingScores[33] += 35;
				swingScores[25] += 35;
				swingScores[21] += 35;
				swingScores[15] += 35;
				swingScores[13] += 35;
				swingScores[3] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x+1)+", "+(G.me.y-3)+"]") != -1) {
				swingScores[8] += 35;
				swingScores[4] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x-1)+", "+(G.me.y-1)+"]") != -1) {
				swingScores[33] += 35;
				swingScores[32] += 35;
				swingScores[24] += 35;
				swingScores[20] += 35;
				swingScores[17] += 35;
				swingScores[12] += 35;
				swingScores[9] += 35;
				swingScores[5] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x+2)+", "+(G.me.y-1)+"]") != -1) {
				swingScores[34] += 35;
				swingScores[28] += 35;
				swingScores[18] += 35;
				swingScores[16] += 35;
				swingScores[10] += 35;
				swingScores[6] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x+2)+", "+(G.me.y+0)+"]") != -1) {
				swingScores[34] += 35;
				swingScores[30] += 35;
				swingScores[28] += 35;
				swingScores[26] += 35;
				swingScores[18] += 35;
				swingScores[11] += 35;
				swingScores[10] += 35;
				swingScores[6] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x+2)+", "+(G.me.y-2)+"]") != -1) {
				swingScores[16] += 35;
				swingScores[10] += 35;
				swingScores[8] += 35;
				swingScores[6] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x+1)+", "+(G.me.y+1)+"]") != -1) {
				swingScores[35] += 35;
				swingScores[34] += 35;
				swingScores[26] += 35;
				swingScores[22] += 35;
				swingScores[19] += 35;
				swingScores[14] += 35;
				swingScores[11] += 35;
				swingScores[7] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x+2)+", "+(G.me.y-3)+"]") != -1) {
				swingScores[8] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x+3)+", "+(G.me.y-1)+"]") != -1) {
				swingScores[18] += 35;
				swingScores[10] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x+3)+", "+(G.me.y+0)+"]") != -1) {
				swingScores[30] += 35;
				swingScores[18] += 35;
				swingScores[10] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x+3)+", "+(G.me.y-2)+"]") != -1) {
				swingScores[10] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x+2)+", "+(G.me.y+1)+"]") != -1) {
				swingScores[34] += 35;
				swingScores[30] += 35;
				swingScores[26] += 35;
				swingScores[19] += 35;
				swingScores[18] += 35;
				swingScores[11] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x-3)+", "+(G.me.y+1)+"]") != -1) {
				swingScores[21] += 35;
				swingScores[13] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x-1)+", "+(G.me.y+2)+"]") != -1) {
				swingScores[35] += 35;
				swingScores[29] += 35;
				swingScores[27] += 35;
				swingScores[25] += 35;
				swingScores[23] += 35;
				swingScores[15] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x+0)+", "+(G.me.y+2)+"]") != -1) {
				swingScores[35] += 35;
				swingScores[31] += 35;
				swingScores[29] += 35;
				swingScores[27] += 35;
				swingScores[23] += 35;
				swingScores[22] += 35;
				swingScores[19] += 35;
				swingScores[15] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x-2)+", "+(G.me.y+2)+"]") != -1) {
				swingScores[25] += 35;
				swingScores[23] += 35;
				swingScores[21] += 35;
				swingScores[15] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x+3)+", "+(G.me.y+1)+"]") != -1) {
				swingScores[30] += 35;
				swingScores[18] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x+1)+", "+(G.me.y+2)+"]") != -1) {
				swingScores[35] += 35;
				swingScores[31] += 35;
				swingScores[27] += 35;
				swingScores[26] += 35;
				swingScores[22] += 35;
				swingScores[19] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x+2)+", "+(G.me.y+2)+"]") != -1) {
				swingScores[31] += 35;
				swingScores[30] += 35;
				swingScores[26] += 35;
				swingScores[19] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x-3)+", "+(G.me.y+2)+"]") != -1) {
				swingScores[21] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x-1)+", "+(G.me.y+3)+"]") != -1) {
				swingScores[27] += 35;
				swingScores[23] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x+0)+", "+(G.me.y+3)+"]") != -1) {
				swingScores[31] += 35;
				swingScores[27] += 35;
				swingScores[23] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x-2)+", "+(G.me.y+3)+"]") != -1) {
				swingScores[23] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x+1)+", "+(G.me.y+3)+"]") != -1) {
				swingScores[31] += 35;
				swingScores[27] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x+3)+", "+(G.me.y+2)+"]") != -1) {
				swingScores[30] += 35;
			}
			if (opponentRobotsString.indexOf("["+(G.me.x+2)+", "+(G.me.y+3)+"]") != -1) {
				swingScores[31] += 35;
			}
        }
        G.rc.setIndicatorDot(G.me, 0, 255, 0);
    }

    public static void build() throws Exception {
        G.indicatorString.append("BUILD ");
        // clean enemy paint for ruin patterns
        // TODO: FIND AND MOP ENEMY PAINT OFF SRP
        // get 2 best locations to build stuff on
        // so if the first one is already there just go to the next one
        if (G.rc.isActionReady()) {
            for (int i = 25; --i >= 0;) {
                attackScores[i] = 0;
                MapLocation loc = G.me.translate(G.range20X[i], G.range20Y[i]);
                if (G.rc.onTheMap(loc) && G.rc.senseMapInfo(loc).getPaint().isEnemy()) {
                    if (G.rc.canSenseRobotAtLocation(loc)) {
                        // if it's an opponent, they get -1 paint
                        // if it's an ally, they go from -2 to -1 paint
                        // in both cases we gain 1 paint
                        // can't be a tower because it has to be painted
                        RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                        // maybe also incentize mopping enemies with low paint
                        attackScores[i] += 11 + Math.min(5, UnitType.MOPPER.paintCapacity - G.rc.getPaint());
                        if (bot.getType() == UnitType.MOPPER) {
                            // double paint loss on moppers
                            attackScores[i]++;
                        }
                        if (bot.paintAmount <= 10) {
                            //treat freezing bot equivalent to gaining 20 paint
                            attackScores[i] += 20;
                        }
                    }
                    if (target.distanceSquaredTo(loc) <= 8) {
                        attackScores[i] += 10;
                    }
                    attackScores[i] += 5;
                    attackScores[i] *= 5;
                }
            }
        }
        if (G.rc.isMovementReady()) {
            for (int i = 8; --i >= 0;) {
                if (G.me.directionTo(target) == G.DIRECTIONS[i]) {
                    moveScores = avoidPaintMicro.micro(G.DIRECTIONS[i], target);
                    moveScores[i] -= 19; // yes only 1 paint
                } else if (G.me.directionTo(target).rotateLeft() == G.DIRECTIONS[i]
                        || G.me.directionTo(target).rotateRight() == G.DIRECTIONS[i]) {
                    moveScores[i] -= 15;
                }
            }
        }
        G.rc.setIndicatorDot(G.me, 0, 0, 255);
    }

    public static Micro avoidPaintMicro = new Micro() {
        @Override
        public int[] micro(Direction d, MapLocation dest) throws Exception {
            // REALLY avoid being on enemy paint
            int[] scores = Motion.defaultMicro.micro(d, dest);
            for (int i = 9; --i >= 0;) {
                if (G.rc.canMove(G.ALL_DIRECTIONS[i])) {
                    MapLocation m = G.me.add(G.ALL_DIRECTIONS[i]);
                    PaintType paint = G.rc.senseMapInfo(m).getPaint();
                    if (paint.isEnemy())
                        scores[i] -= 10;
                    if (paint == PaintType.EMPTY)
                        scores[i] -= 5;
                }
            }
            // run away from towers
            MapLocation[] ruins = G.rc.senseNearbyRuins(-1);
            MapLocation a = G.me;
            for (int i = ruins.length; --i >= 0;) {
                if (G.rc.canSenseRobotAtLocation(ruins[i])
                        && G.rc.senseRobotAtLocation(ruins[i]).team == G.opponentTeam) {
                    a = a.add(ruins[i].directionTo(G.me));
                }
            }
            if (a != G.me) {
                Direction towerDir = G.me.directionTo(a);
                for (int i = 8; --i >= 0;) {
                    if (G.ALL_DIRECTIONS[i] == towerDir) {
                        scores[i] -= 10;
                        break;
                    }
                }
            }
            return scores;
        }
    };
}
