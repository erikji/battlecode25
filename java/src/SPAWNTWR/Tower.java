package SPAWNTWR;

import battlecode.common.*;
import java.util.*;

public class Tower {
    public static int spawnedSoldiers = 0;
    public static int spawnedSplashers = 0;
    public static int spawnedMoppers = 0;
    public static int level = 0;

    static UnitType moneyLevels[] = {UnitType.LEVEL_THREE_MONEY_TOWER, UnitType.LEVEL_TWO_MONEY_TOWER, UnitType.LEVEL_ONE_MONEY_TOWER};
    static UnitType paintLevels[] = {UnitType.LEVEL_THREE_PAINT_TOWER, UnitType.LEVEL_TWO_PAINT_TOWER, UnitType.LEVEL_ONE_PAINT_TOWER};

    public static void run(RobotController rc, Random rng) throws Exception {
        Motion.currLoc = rc.getLocation();
        MapLocation[] spawnLocs = new MapLocation[8];
        for (int i = 0; i < 8; i++) {
            spawnLocs[i] = Motion.currLoc.add(Motion.DIRECTIONS[i]);
        }
        Arrays.sort(spawnLocs, new Comparator<MapLocation>() {
            public int compare(MapLocation a, MapLocation b) {
                return a.distanceSquaredTo(Motion.mapCenter) - b.distanceSquaredTo(Motion.mapCenter);
            };
        });
        while (true) {
            UnitType spawnType = UnitType.MOPPER;
            if (spawnedSplashers < spawnedMoppers * 2) {
                spawnType = UnitType.SPLASHER;
            }
            for (int i = 0; i < 8; i++) {
                if (rc.canBuildRobot(spawnType, spawnLocs[i])) {
                    rc.buildRobot(spawnType, spawnLocs[i]);
                    break;
                }
            }
            switch (rc.getType()) {
                case LEVEL_ONE_DEFENSE_TOWER:
                    level = 1;
                    DefenseTower.run(rc, rng);
                    break;
                case LEVEL_TWO_DEFENSE_TOWER:
                    level = 2;
                    DefenseTower.run(rc, rng);
                    break;
                case LEVEL_THREE_DEFENSE_TOWER:
                    level = 3;
                    DefenseTower.run(rc, rng);
                    break;
                case LEVEL_ONE_MONEY_TOWER:
                    level = 1;
                    MoneyTower.run(rc, rng, level);
                    break;
                case LEVEL_TWO_MONEY_TOWER:
                    level = 2;
                    MoneyTower.run(rc, rng, level);
                    break;
                case LEVEL_THREE_MONEY_TOWER:
                    level = 3;
                    MoneyTower.run(rc, rng, level);
                    break;
                case LEVEL_ONE_PAINT_TOWER:
                    level = 1;
                    PaintTower.run(rc, rng);
                    break;
                case LEVEL_TWO_PAINT_TOWER:
                    level = 2;
                    PaintTower.run(rc, rng);
                    break;
                case LEVEL_THREE_PAINT_TOWER:
                    level = 3;
                    PaintTower.run(rc, rng);
                    break;
                default:
                    throw new Exception("Challenge Complete! How Did We Get Here?");
            }
            //attack AFTER run (in case something gets upgraded)
            if (rc.canAttack(null)) rc.attack(null); //splash
            MapLocation bestEnemyLoc = new MapLocation(-1, -1);
            int bestEnemyHp = 1000000;
            UnitType bestEnemyType = UnitType.MOPPER;
            //priority: soldier, splasher, mopper
            for (RobotInfo r : rc.senseNearbyRobots()) {
                if (bestEnemyType == UnitType.MOPPER && (r.type == UnitType.SOLDIER || r.type == UnitType.SPLASHER || r.health < bestEnemyHp)) {
                    bestEnemyHp = r.health;
                    bestEnemyLoc = r.location;
                    bestEnemyType = r.type;
                }
                if (bestEnemyType == UnitType.SPLASHER && (r.type == UnitType.SOLDIER || (r.type == UnitType.SPLASHER && r.health < bestEnemyHp))) {
                    bestEnemyHp = r.health;
                    bestEnemyLoc = r.location;
                    bestEnemyType = r.type;
                }
                if (bestEnemyType == UnitType.SOLDIER && (r.type == UnitType.SOLDIER && r.health < bestEnemyHp)) {
                    bestEnemyHp = r.health;
                    bestEnemyLoc = r.location;
                    bestEnemyType = r.type;
                }
            }
            if (bestEnemyLoc.x >= 0 && rc.canAttack(bestEnemyLoc)) {
                rc.attack(bestEnemyLoc);
            }
            Clock.yield();
        }
    }
}
