package SPAARK;

import battlecode.common.*;
import java.util.*;

import java.util.Arrays;
import java.util.Comparator;

public class Tower {
    public static int spawnedSoldiers = 0;
    public static int spawnedSplashers = 0;
    public static int spawnedMoppers = 0;
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
        }
    }
}
