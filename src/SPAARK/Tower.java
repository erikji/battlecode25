package SPAARK;

import battlecode.common.*;
import java.util.*;

public class Tower {
    public static int spawnedSoldiers = 0;
    public static int spawnedSplashers = 0;
    public static int spawnedMoppers = 0;
    public static int spawnedRobots = 0;

    static UnitType moneyLevels[] = {UnitType.LEVEL_THREE_MONEY_TOWER, UnitType.LEVEL_TWO_MONEY_TOWER, UnitType.LEVEL_ONE_MONEY_TOWER};
    static UnitType paintLevels[] = {UnitType.LEVEL_THREE_PAINT_TOWER, UnitType.LEVEL_TWO_PAINT_TOWER, UnitType.LEVEL_ONE_PAINT_TOWER};

    public static void init() throws Exception {
        
    }

    public static void run() throws Exception {
        Motion.updateInfo();
        MapLocation[] spawnLocs = new MapLocation[] {
            Motion.currLoc.add(Direction.NORTH),
            Motion.currLoc.add(Direction.NORTH).add(Direction.NORTH),
            Motion.currLoc.add(Direction.NORTHEAST),
            Motion.currLoc.add(Direction.EAST),
            Motion.currLoc.add(Direction.EAST).add(Direction.EAST),
            Motion.currLoc.add(Direction.SOUTHEAST),
            Motion.currLoc.add(Direction.SOUTH),
            Motion.currLoc.add(Direction.SOUTH).add(Direction.SOUTH),
            Motion.currLoc.add(Direction.SOUTHWEST),
            Motion.currLoc.add(Direction.WEST),
            Motion.currLoc.add(Direction.WEST).add(Direction.WEST),
            Motion.currLoc.add(Direction.NORTHWEST)
        };

        Arrays.sort(spawnLocs, new Comparator<MapLocation>() {
            public int compare(MapLocation a, MapLocation b) {
                return a.distanceSquaredTo(Motion.mapCenter) - b.distanceSquaredTo(Motion.mapCenter);
            };
        });
        // general common code for all towers
        // spawning
        // Note that we r going to have >50% moppers since they r cheaper
        switch (spawnedRobots % 3) {
            case 0:
                for (MapLocation loc : spawnLocs) {
                    if (G.rc.canBuildRobot(UnitType.SOLDIER, loc)) {
                        G.rc.buildRobot(UnitType.SOLDIER, loc);
                        spawnedRobots++;
                        spawnedSoldiers++;
                        break;
                    }
                }
                break;
            case 1:
                for (MapLocation loc : spawnLocs) {
                    if (G.rc.canBuildRobot(UnitType.SPLASHER, loc)) {
                        G.rc.buildRobot(UnitType.SPLASHER, loc);
                        spawnedRobots++;
                        spawnedSplashers++;
                        break;
                    }
                }
                break;
            case 2:
                for (MapLocation loc : spawnLocs) {
                    if (G.rc.canBuildRobot(UnitType.MOPPER, loc)) {
                        G.rc.buildRobot(UnitType.MOPPER, loc);
                        spawnedRobots++;
                        spawnedMoppers++;
                        break;
                    }
                }
                break;
        }
        // more specialized here
        switch (G.rc.getType()) {
            case LEVEL_ONE_DEFENSE_TOWER:
                DefenseTower.level = 0;
                DefenseTower.run();
                break;
            case LEVEL_TWO_DEFENSE_TOWER:
                DefenseTower.level = 1;
                DefenseTower.run();
                break;
            case LEVEL_THREE_DEFENSE_TOWER:
                DefenseTower.level = 2;
                DefenseTower.run();
                break;
            case LEVEL_ONE_MONEY_TOWER:
                MoneyTower.level = 0;
                MoneyTower.run();
                break;
            case LEVEL_TWO_MONEY_TOWER:
                MoneyTower.level = 1;
                MoneyTower.run();
                break;
            case LEVEL_THREE_MONEY_TOWER:
                MoneyTower.level = 2;
                MoneyTower.run();
                break;
            case LEVEL_ONE_PAINT_TOWER:
                PaintTower.level = 0;
                PaintTower.run();
                break;
            case LEVEL_TWO_PAINT_TOWER:
                PaintTower.level = 1;
                PaintTower.run();
                break;
            case LEVEL_THREE_PAINT_TOWER:
                PaintTower.level = 2;
                PaintTower.run();
                break;
            default:
                throw new Exception("Challenge Complete! How Did We Get Here?");
        }
        // attack AFTER run (in case we get an upgrade)
        MapLocation bestEnemyLoc = null;
        int bestEnemyHp = 1000000;
        UnitType bestEnemyType = UnitType.MOPPER;
        // priority: soldier, splasher, mopper
        int numAttackableRobots = 0;
        for (RobotInfo r : Motion.opponentRobots) {
            if (G.rc.canAttack(r.location)) {
                numAttackableRobots++;
                switch (bestEnemyType) {
                    case UnitType.MOPPER:
                        if (r.type == UnitType.SOLDIER || r.type == UnitType.SPLASHER || r.health < bestEnemyHp) {
                            bestEnemyHp = r.health;
                            bestEnemyLoc = r.location;
                            bestEnemyType = r.type;
                        }
                        break;
                    case UnitType.SPLASHER:
                        if (r.type == UnitType.SOLDIER || (r.type == UnitType.SPLASHER && r.health < bestEnemyHp)) {
                            bestEnemyHp = r.health;
                            bestEnemyLoc = r.location;
                            bestEnemyType = r.type;
                        }
                        break;
                    case UnitType.SOLDIER:
                        if (r.type == UnitType.SOLDIER && r.health < bestEnemyHp) {
                            bestEnemyHp = r.health;
                            bestEnemyLoc = r.location;
                        }
                        break;
                }
            }
        }
        if (numAttackableRobots > 2 && G.rc.canAttack(null)) {
            G.rc.attack(null);
        } else if (numAttackableRobots > 0 && G.rc.canAttack(bestEnemyLoc)) {
            G.rc.attack(bestEnemyLoc);
        }
        if (G.rc.canUpgradeTower(Motion.currLoc) && G.rc.getRoundNum() > 100) {
            G.rc.upgradeTower(Motion.currLoc);
        }
    }
}
