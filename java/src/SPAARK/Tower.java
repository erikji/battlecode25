package SPAARK;

import battlecode.common.*;
import java.util.*;

public class Tower {
    public static void run(RobotController rc, Random rng) throws Exception {
        while (true) {
            switch (rc.getType()) {
                case LEVEL_ONE_DEFENSE_TOWER:
                    DefenseTower.run(rc, rng, 1);
                    break;
                case LEVEL_TWO_DEFENSE_TOWER:
                    DefenseTower.run(rc, rng, 2);
                    break;
                case LEVEL_THREE_DEFENSE_TOWER:
                    DefenseTower.run(rc, rng, 3);
                    break;
                case LEVEL_ONE_MONEY_TOWER:
                    MoneyTower.run(rc, rng, 1);
                    break;
                case LEVEL_TWO_MONEY_TOWER:
                    MoneyTower.run(rc, rng, 2);
                    break;
                case LEVEL_THREE_MONEY_TOWER:
                    MoneyTower.run(rc, rng, 3);
                    break;
                case LEVEL_ONE_PAINT_TOWER:
                    PaintTower.run(rc, rng, 1);
                    break;
                case LEVEL_TWO_PAINT_TOWER:
                    PaintTower.run(rc, rng, 2);
                    break;
                case LEVEL_THREE_PAINT_TOWER:
                    PaintTower.run(rc, rng, 3);
                    break;
                default:
                    throw new Exception("Challenge Complete! How Did We Get Here?");
            }
            //attack AFTER run (in case something gets upgraded)
            rc.attack(null); //splash
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
