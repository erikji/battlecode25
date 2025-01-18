package SPAARK;

import battlecode.common.*;
import java.util.*;

public class Tower {
    public static int spawnedSoldiers = 0;
    public static int spawnedSplashers = 0;
    public static int spawnedMoppers = 0;
    public static int spawnedRobots = 0;

    public static double doubleSpawnedSoldiers = 0;
    public static double doubleSpawnedSplashers = 0;
    public static double doubleSpawnedMoppers = 0;

    public static int cnt = 0;

    public static MapLocation[] spawnLocs;
    public static int level;

    public static void init() throws Exception {
        spawnLocs = new MapLocation[] {
                G.me.add(Direction.NORTH),
                G.me.add(Direction.NORTH).add(Direction.NORTH),
                G.me.add(Direction.NORTHEAST),
                G.me.add(Direction.EAST),
                G.me.add(Direction.EAST).add(Direction.EAST),
                G.me.add(Direction.SOUTHEAST),
                G.me.add(Direction.SOUTH),
                G.me.add(Direction.SOUTH).add(Direction.SOUTH),
                G.me.add(Direction.SOUTHWEST),
                G.me.add(Direction.WEST),
                G.me.add(Direction.WEST).add(Direction.WEST),
                G.me.add(Direction.NORTHWEST)
        };
        Arrays.sort(spawnLocs,
                (MapLocation a, MapLocation b) -> a.distanceSquaredTo(G.mapCenter) - b.distanceSquaredTo(G.mapCenter));
    }

    public static void spawnBot(UnitType t) throws Exception {
        switch (t) {
            case UnitType.MOPPER:
                for (MapLocation loc : spawnLocs) {
                    if (G.rc.canBuildRobot(UnitType.MOPPER, loc)) {
                        G.rc.buildRobot(UnitType.MOPPER, loc);
                        spawnedRobots++;
                        // spawnedMoppers++;
                        break;
                    }
                }
                break;
            case UnitType.SPLASHER:
                for (MapLocation loc : spawnLocs) {
                    if (G.rc.canBuildRobot(UnitType.SPLASHER, loc)) {
                        G.rc.buildRobot(UnitType.SPLASHER, loc);
                        spawnedRobots++;
                        // spawnedSplashers++;
                        break;
                    }
                }
                break;
            case UnitType.SOLDIER:
                for (MapLocation loc : spawnLocs) {
                    if (G.rc.canBuildRobot(UnitType.SOLDIER, loc)) {
                        G.rc.buildRobot(UnitType.SOLDIER, loc);
                        spawnedRobots++;
                        // spawnedSoldiers++;
                        break;
                    }
                }
                break;
            default:
                throw new Exception("what are you spawning?? a tower???");
        }
    }

    public static void run() throws Exception {
        // general common code for all towers
        // spawning
        if (spawnedRobots == 0) {
            spawnBot(UnitType.SOLDIER);
        } else if (spawnedRobots == 1) {
            // spawnBot(UnitType.MOPPER);
            spawnBot(UnitType.SPLASHER);
        }
        // } else if (spawnedRobots == 2) {
        // spawnBot(UnitType.SOLDIER);
        // }
        else {
            UnitType trying = UnitType.SPLASHER;
            // int mod = 7;
            // int area = G.rc.getMapHeight() * G.rc.getMapWidth();

            double soldierWeight = 2;
            double splasherWeight = 2;
            double mopperWeight = 2;

            // if (G.rc.getNumberTowers() < 25) {
            // for (int i = POI.numberOfTowers; --i >= 0;) {
            // if (POI.towerTeams[i] == Team.NEUTRAL) {
            // soldierWeight += 1;
            // break;
            // }
            // }
            // }
            if (G.rc.getNumberTowers() == 25) {
                soldierWeight -= 1;
            }
            double sum = soldierWeight + splasherWeight + mopperWeight;
            soldierWeight /= sum;
            splasherWeight /= sum;
            mopperWeight /= sum;

            G.indicatorString = new StringBuilder();
            G.indicatorString.append(doubleSpawnedSoldiers + " " + spawnedSoldiers + " " + doubleSpawnedSplashers + " "
                    + spawnedSplashers + " " + doubleSpawnedMoppers + " " + spawnedMoppers + " ");

            double soldier = doubleSpawnedSoldiers + soldierWeight - spawnedSoldiers;
            double splasher = doubleSpawnedSplashers + splasherWeight - spawnedSplashers;
            double mopper = doubleSpawnedMoppers + mopperWeight - spawnedMoppers;

            // if (soldier >= splasher && soldier >= mopper) {
            // trying = UnitType.SOLDIER;
            // }
            // else if (splasher >= mopper) {
            // trying = UnitType.SPLASHER;
            // }
            // else {
            // trying = UnitType.MOPPER;
            // }

            // IMPORTANT: this prioritizes mopper > splasher > soldier at the start
            if (mopper >= splasher && mopper >= soldier) {
                trying = UnitType.MOPPER;
            } else if (splasher >= soldier) {
                trying = UnitType.SPLASHER;
            } else {
                trying = UnitType.SOLDIER;
            }

            if (G.rc.getNumberTowers() == 25 || G.rc.getMoney() - trying.moneyCost >= 1000 || G.rc.getPaint() == 1000) {
                switch (trying) {
                    case UnitType.MOPPER:
                        for (MapLocation loc : spawnLocs) {
                            if (G.rc.canBuildRobot(UnitType.MOPPER, loc)) {
                                G.rc.buildRobot(UnitType.MOPPER, loc);
                                spawnedRobots++;
                                spawnedMoppers++;
                                doubleSpawnedSoldiers += soldierWeight;
                                doubleSpawnedSplashers += splasherWeight;
                                doubleSpawnedMoppers += mopperWeight;
                                break;
                            }
                        }
                        break;
                    case UnitType.SPLASHER:
                        for (MapLocation loc : spawnLocs) {
                            if (G.rc.canBuildRobot(UnitType.SPLASHER, loc)) {
                                G.rc.buildRobot(UnitType.SPLASHER, loc);
                                spawnedRobots++;
                                spawnedSplashers++;
                                doubleSpawnedSoldiers += soldierWeight;
                                doubleSpawnedSplashers += splasherWeight;
                                doubleSpawnedMoppers += mopperWeight;
                                break;
                            }
                        }
                        break;
                    case UnitType.SOLDIER:
                        for (MapLocation loc : spawnLocs) {
                            if (G.rc.canBuildRobot(UnitType.SOLDIER, loc)) {
                                G.rc.buildRobot(UnitType.SOLDIER, loc);
                                spawnedRobots++;
                                spawnedSoldiers++;
                                doubleSpawnedSoldiers += soldierWeight;
                                doubleSpawnedSplashers += splasherWeight;
                                doubleSpawnedMoppers += mopperWeight;
                                break;
                            }
                        }
                        break;
                    default:
                        throw new Exception("what are you spawning?? a tower???");
                }
            }
        }
        // more specialized here
        switch (G.rc.getType()) {
            case LEVEL_ONE_DEFENSE_TOWER -> {
                level = 0;
                DefenseTower.run();
            }
            case LEVEL_TWO_DEFENSE_TOWER -> {
                level = 1;
                DefenseTower.run();
            }
            case LEVEL_THREE_DEFENSE_TOWER -> {
                level = 2;
                DefenseTower.run();
            }
            case LEVEL_ONE_MONEY_TOWER -> {
                level = 0;
                MoneyTower.run();
            }
            case LEVEL_TWO_MONEY_TOWER -> {
                level = 1;
                MoneyTower.run();
            }
            case LEVEL_THREE_MONEY_TOWER -> {
                level = 2;
                MoneyTower.run();
            }
            case LEVEL_ONE_PAINT_TOWER -> {
                level = 0;
                PaintTower.run();
            }
            case LEVEL_TWO_PAINT_TOWER -> {
                level = 1;
                PaintTower.run();
            }
            case LEVEL_THREE_PAINT_TOWER -> {
                level = 2;
                PaintTower.run();
            }
            default -> throw new Exception("Challenge Complete! How Did We Get Here?");
        }
        while (G.rc.canUpgradeTower(G.me) && G.rc.getMoney() - (level == 0 ? 2500 : 5000) >= 5000) {
            attack();
            G.rc.upgradeTower(G.me);
        }
        // attack after upgrading
        attack();
    }

    public static void attack() throws Exception {
        // prioritize bots with low hp, unless they have less hp then our attack power
        if (G.rc.canAttack(null)) {
            G.rc.attack(null);
        }
        // check cooldown on single target attack
        if (G.rc.canAttack(G.me)) {
            MapLocation bestEnemyLoc = null;
            int bestEnemyHp = 1000000;
            int attackStrength = G.rc.getType().attackStrength;
            for (int i = G.opponentRobots.length; --i >= 0;) {
                RobotInfo r = G.opponentRobots[i];
                // check if it's still alive
                if (G.rc.canSenseRobotAtLocation(r.location)
                        && G.me.isWithinDistanceSquared(r.location, G.rc.getType().actionRadiusSquared)) {
                    // just do lowest hp it's basically the same as having priorities and it's more
                    // gold efficient to kill moppers anyways
                    if (bestEnemyHp > attackStrength && r.health < bestEnemyHp) {
                        bestEnemyHp = r.health;
                        bestEnemyLoc = r.location;
                    } else if (r.health > bestEnemyHp && r.health <= attackStrength) {
                        bestEnemyHp = r.health;
                        bestEnemyLoc = r.location;
                    }
                }
            }
            if (bestEnemyLoc != null) {
                G.rc.attack(bestEnemyLoc);
            }
        }
    }
}
