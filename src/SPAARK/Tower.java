package SPAARK;

import battlecode.common.*;
import java.util.*;

public class Tower {
    public static int spawnedSoldiers = 0;
    public static int spawnedSplashers = 0;
    public static int spawnedMoppers = 0;
    public static int spawnedRobots = 0;

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
                        spawnedMoppers++;
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
            spawnBot(UnitType.SPLASHER);
        }
        else {
            UnitType trying = UnitType.SPLASHER;
            // int mod = 7;
            // int area = G.rc.getMapHeight() * G.rc.getMapWidth();
            
            switch ((spawnedRobots - 2) % 7) {
                // make sure to subtract 2
                case 0:
                    trying = UnitType.MOPPER;
                    break;
                case 1:
                    trying = UnitType.SPLASHER;
                    break;
                case 2:
                    trying = UnitType.SOLDIER;
                    break;
                case 3:
                    trying = UnitType.SPLASHER;
                    break;
                case 4:
                    trying = UnitType.SOLDIER;
                    break;
                case 5:
                    trying = UnitType.SPLASHER;
                    break;
                case 6:
                    trying = UnitType.MOPPER;
                    break;
            }

            if (G.rc.getNumberTowers() == 25 || G.rc.getMoney() - trying.moneyCost >= 1000 || G.rc.getPaint() == 1000) {
                spawnBot(trying);
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
        while (G.rc.canUpgradeTower(G.me) && G.rc.getMoney() - (level==0?2500:5000) >= 5000) {
            G.rc.upgradeTower(G.me);
        }
        // attack after upgrading
        //prioritize bots with low hp, unless they have less hp then our attack power
        G.rc.attack(null);
        MapLocation bestEnemyLoc = null;
        int bestEnemyHp = 1000000;
        int attackStrength = G.rc.getType().attackStrength;
        UnitType bestEnemyType = UnitType.MOPPER;
        // priority: soldier, splasher, mopper
        for (int i = G.opponentRobots.length; --i >= 0;) {
            RobotInfo r = G.opponentRobots[i];
            if (G.rc.canSenseRobotAtLocation(r.location)) {
                switch (bestEnemyType) {
                    case UnitType.MOPPER:
                        if (bestEnemyHp > attackStrength) {
                            if (r.type == UnitType.SOLDIER || r.type == UnitType.SPLASHER || r.health < bestEnemyHp) {
                                bestEnemyHp = r.health;
                                bestEnemyLoc = r.location;
                                bestEnemyType = r.type;
                            }
                        } else {
                            if (r.type == UnitType.SOLDIER || r.type == UnitType.SPLASHER || (r.health > bestEnemyHp && r.health <= attackStrength)) {
                                bestEnemyHp = r.health;
                                bestEnemyLoc = r.location;
                                bestEnemyType = r.type;
                            }
                        }
                        break;
                    case UnitType.SPLASHER:
                        if (bestEnemyHp > attackStrength) {
                            if (r.type == UnitType.SOLDIER || (r.type == UnitType.SPLASHER && r.health < bestEnemyHp)) {
                                bestEnemyHp = r.health;
                                bestEnemyLoc = r.location;
                                bestEnemyType = r.type;
                            }
                        } else {
                            if (r.type == UnitType.SOLDIER || (r.type == UnitType.SPLASHER && r.health > bestEnemyHp && r.health <= attackStrength)) {
                                bestEnemyHp = r.health;
                                bestEnemyLoc = r.location;
                                bestEnemyType = r.type;
                            }
                        }
                        break;
                    case UnitType.SOLDIER:
                        if (bestEnemyHp > attackStrength) {
                            if (r.type == UnitType.SOLDIER && r.health < bestEnemyHp) {
                                bestEnemyHp = r.health;
                                bestEnemyLoc = r.location;
                            }
                        } else {
                            if (r.type == UnitType.SOLDIER && r.health > bestEnemyHp && r.health <= attackStrength) {
                                bestEnemyHp = r.health;
                                bestEnemyLoc = r.location;
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        if (G.rc.getID() < 5) {
            System.out.println(++cnt);
        }
        if (G.rc.canAttack(bestEnemyLoc)) {
            G.rc.attack(bestEnemyLoc);
        }
    }
}
