package TSPAARKJAN14;

import battlecode.common.*;

public class Splasher {
    public static final int EXPLORE = 0;
    public static final int ATTACK = 1;
    public static final int RETREAT = 2;
    public static int mode = EXPLORE;
    // controls round between visiting ruins
    public static final int VISIT_TIMEOUT = 75;

    public static MapLocation attackTarget = new MapLocation(-1, -1);
    public static int attackTargetTower;
    public static StringBuilder triedAttackTargets = new StringBuilder();

    /**
     * Always:
     * If low on paint, retreat
     * Default to explore mode
     * If found targets to paint in POI storage, switch to attack mode
     * 
     * Explore:
     * Run around randomly painting stuff with some microstrategy
     * TODO: AVOID PAINTING OVER SRP, TOWER PATTERNS, PAINT NEAR OWN PAINT TO AVOID
     * TODO: DETACHED TERRITORY
     * 
     * Attack:
     * Go to targeted POI location and throw paint everywhere
     * Find new target if there's no threat
     */
    public static void run() throws Exception {
        // occasionally clear ruins to not oof forever
        if (G.rc.getPaint() < Robot.getRetreatPaint()) {
            mode = RETREAT;
        } else if (G.rc.getPaint() > G.rc.getType().paintCapacity * 3 / 4 && mode == RETREAT) {
            mode = EXPLORE;
        }
        if (mode != RETREAT) {
            updateAttackTarget();
        }
        else {
            triedAttackTargets = new StringBuilder();
        }
        // int a = Clock.getBytecodeNum();
        // switch (mode) {
        // ADD CASES HERE FOR SWITCHING MODES
        // }
        int b = Clock.getBytecodeNum();
        // G.indicatorString.append((b - a) + " ");
        switch (mode) {
            case EXPLORE -> explore();
            case ATTACK -> attack();
            case RETREAT -> {
                G.indicatorString.append("RETREAT ");
                Robot.retreat();
            }
        }
        G.indicatorString.append((Clock.getBytecodeNum() - b) + " ");
    }

    public static void explore() throws Exception {
        G.indicatorString.append("EXPLORE ");
        MapLocation bestLoc = null;
        int bestScore = 0;
        // painting heuristic
        // remove opponent paint, paint under enemy bots, paint under allied bots
        StringBuilder allyRobotsList = new StringBuilder();
        for (int i = G.allyRobots.length; --i >= 0;) {
            allyRobotsList.append(G.allyRobots[i].getLocation().toString());
        }
        StringBuilder opponentRobotsList = new StringBuilder();
        for (int i = G.opponentRobots.length; --i >= 0;) {
            opponentRobotsList.append(G.opponentRobots[i].getLocation().toString());
        }
        int r = Random.rand() % 13;
        for (int j = 13; --j >= 0;) {
            int i = (j + r) % 13;
            MapLocation loc = G.me.translate(G.range20X[i], G.range20Y[i]);
            if (G.rc.canAttack(loc)) {
                int score = 0;
                for (int dir = 9; --dir >= 0;) {
                    // only care about sqrt(2) distance because bytecode restrictions
                    MapLocation nxt = loc.add(G.ALL_DIRECTIONS[dir]);
                    if (G.rc.canSenseLocation(nxt)) {
                        MapInfo info = G.rc.senseMapInfo(nxt);
                        //bonus points for hit opponent tower
                        if (info.hasRuin() && opponentRobotsList.indexOf(nxt.toString()) != -1) score += 2;
                        if (info.isPassable()) {
                            PaintType paint = info.getPaint();
                            int paintScore = 0;
                            if (paint == PaintType.EMPTY) {
                                paintScore = 1;
                            } else if (paint.isEnemy()) {
                                paintScore = 2; // bonus points for deleting opponent paint
                            }
                            if (!paint.isAlly() && nxt == G.me) {
                                score += paintScore; // bonus points for painting self
                            }
                            if (allyRobotsList.indexOf(nxt.toString()) != -1) {
                                score += paintScore; // bonus points for painting allies
                            }
                            if (opponentRobotsList.indexOf(nxt.toString()) != -1) {
                                score += paintScore; // bonus points for painting opponents
                            }
                            score += paintScore;
                        }
                    }
                }
                if (score > bestScore) {
                    bestLoc = loc;
                    bestScore = score;
                }
                // very easy fix
                if (Clock.getBytecodesLeft() < 2500) {
                    break;
                }
            }
        }
        if (bestScore > 8 && bestLoc != null) {
            G.rc.attack(bestLoc, Random.rand() % 2 == 0);
        }
        // find towers to go to from POI
        Motion.exploreRandomly();
        // bestLoc = null;
        // int bestDistanceSquared = 10000;
        // for (int i = 144; --i >= 0;) {
        //     if (POI.towers[i] == -1) {
        //         break;
        //     }
        //     if (POI.parseTowerTeam(POI.towers[i]) == G.opponentTeam) {
        //         MapLocation pos = POI.parseLocation(POI.towers[i]);
        //         if (G.me.isWithinDistanceSquared(pos, bestDistanceSquared) && !G.me.isWithinDistanceSquared(pos, 20)
        //                 && G.getLastVisited(pos) + VISIT_TIMEOUT < G.round) {
        //             bestDistanceSquared = G.me.distanceSquaredTo(pos);
        //             bestLoc = pos;
        //         }
        //     } else if (POI.parseTowerTeam(POI.towers[i]) == Team.NEUTRAL) {
        //         MapLocation pos = POI.parseLocation(POI.towers[i]);
        //         // prioritize opponent towers more than ruins
        //         // so it has to be REALLY close
        //         if (G.me.isWithinDistanceSquared(pos, bestDistanceSquared / 5) && !G.me.isWithinDistanceSquared(pos, 20)
        //                 && G.getLastVisited(pos) + VISIT_TIMEOUT < G.round) {
        //             bestDistanceSquared = G.me.distanceSquaredTo(pos) * 5; // lol
        //             bestLoc = pos;
        //         }
        //     }
        //     if (Clock.getBytecodesLeft() < 1500) {
        //         break;
        //     }
        // }
        // if (bestLoc == null) {
        //     Motion.exploreRandomly();
        // } else {
        //     Motion.bugnavTowards(bestLoc);
        //   // // G.rc.setIndicatorLine(G.me, bestLoc, 255, 255, 0);
        // }
      // // G.rc.setIndicatorDot(G.me, 0, 255, 0);
    }

    public static void attack() throws Exception {
        MapLocation bestLoc = null;
        int bestScore = 0;
        G.indicatorString.append("ATTACK ");
        // painting heuristic
        StringBuilder allyRobotsList = new StringBuilder();
        for (int i = G.allyRobots.length; --i >= 0;) {
            allyRobotsList.append(G.allyRobots[i].getLocation().toString());
        }
        StringBuilder opponentRobotsList = new StringBuilder();
        for (int i = G.opponentRobots.length; --i >= 0;) {
            opponentRobotsList.append(G.opponentRobots[i].getLocation().toString());
        }
        int r = Random.rand() % 13;
        for (int j = 13; --j >= 0;) {
            int i = (j + r) % 13;
            MapLocation loc = G.me.translate(G.range20X[i], G.range20Y[i]);
            if (G.rc.canAttack(loc)) {
                int score = 0;
                // int opponentRobotsPaintedScore = 0;
                for (int dir = 9; --dir >= 0;) {
                    // TODO: negative weight for painting SRP
                    // only care about sqrt(2) distance because bytecode restrictions
                    MapLocation nxt = loc.add(G.ALL_DIRECTIONS[dir]);
                    if (G.rc.canSenseLocation(nxt)) {
                        MapInfo info = G.rc.senseMapInfo(nxt);
                        //bonus points for hit opponent tower
                        if (info.hasRuin() && opponentRobotsList.indexOf(nxt.toString()) != -1) score += 2;
                        if (info.isPassable()) {
                            PaintType paint = info.getPaint();
                            int paintScore = 0;
                            if (paint == PaintType.EMPTY)
                                paintScore = 1;
                            if (paint.isEnemy()) {
                                paintScore = 2; // bonus points for deleting opponent paint
                                if (attackTarget.x != -1
                                        && Motion.getChebyshevDistance(nxt, attackTarget) <= 2) {
                                    score += 4; // bonus points for painting target
                                }
                            }
                            if (!paint.isAlly() && nxt == G.me) {
                                score += paintScore; // bonus points for painting self
                            }
                            if (allyRobotsList.indexOf(nxt.toString()) != -1) {
                                score += paintScore; // bonus points for painting self
                            }
                            if (opponentRobotsList.indexOf(nxt.toString()) != -1) {
                                score += paintScore; // bonus points for painting self
                                // if (!paint.isAlly()) {
                                //     opponentRobotsPaintedScore++;
                                // }
                            }
                            score += paintScore;
                        }
                    }
                }
                // score += opponentRobotsPaintedScore * opponentRobotsPaintedScore;
                if (score > bestScore) {
                    bestLoc = loc;
                    bestScore = score;
                }
                if (Clock.getBytecodesLeft() < 2500) {
                    break;
                }
            }
        }
        if (bestScore > 8 && bestLoc != null) {
            G.rc.attack(bestLoc, Random.rand() % 2 == 0);
        }
        Motion.bugnavTowards(attackTarget);
      // // G.rc.setIndicatorLine(G.me, attackTarget, 255, 255, 0);
      // // G.rc.setIndicatorDot(G.me, 255, 0, 0);
    }

    /**
     * Searches for towers/ruins in POI to attack, and enters ATTACK mode if found
     * target
     */
    public static void updateAttackTarget() throws Exception {
        if (attackTargetTower != -1) {
            if (POI.towerTeams[attackTargetTower] != G.opponentTeam) {
                attackTarget = G.invalidLoc;
            } else {
                MapLocation loc = POI.towerLocs[attackTargetTower];
                search: if (G.me.distanceSquaredTo(loc) <= 4) {
                    for (int y = -2; y <= 2; y++) {
                        for (int x = -2; x <= 2; x++) {
                            if (G.rc.senseMapInfo(loc.translate(x, y)).getPaint().isEnemy()) {
                                break search;
                            }
                        }
                    }
                    attackTarget = G.invalidLoc;
                }
            }
            // TODO: make it change targets if it finds ruin with 24 empty/ally paint
            // TODO: STOP CIRCLING AROUND OUR OWN TOWERS ALREADY
        }
        if (attackTarget.x == -1) {
            int best = -1;
            int bestWeight = 0;
            for (int i = POI.numberOfTowers; --i >= 0;) {
                if (POI.towerTeams[i] == G.team) {
                    continue;
                }
                if (POI.towerTeams[i] == Team.NEUTRAL && G.rc.getNumberTowers() == 25) {
                    continue;
                }
                MapLocation loc = POI.towerLocs[i];
                int distance = Motion.getChebyshevDistance(G.me, loc);
                int weight = -distance;
                if (triedAttackTargets.indexOf("" + (char) i) != -1) {
                // if (!(G.round <= VISIT_TIMEOUT || G.getLastVisited(loc.x, loc.y) + VISIT_TIMEOUT < G.round)) {
                    weight -= 1000;
                    if (POI.towerTeams[i] == Team.NEUTRAL) {
                        continue;
                    }
                }
                if (POI.towerTeams[i] == G.opponentTeam) {
                    weight += 100;
                }
                if (best == -1 || weight > bestWeight) {
                    best = i;
                    bestWeight = weight;
                }
            }
            if (best == -1) {
                mode = EXPLORE;
                return;
            }
            attackTargetTower = best;
            attackTarget = POI.towerLocs[best];
            triedAttackTargets.append((char) best);
            // G.setLastVisited(attackTarget, G.round);
            mode = ATTACK;
        }
    }
}
