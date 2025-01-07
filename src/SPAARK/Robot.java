package SPAARK;

import battlecode.common.*;
import java.util.*;

public class Robot {
    public static void run(RobotController rc, Random rng) throws Exception {
        Mopper.rc = Soldier.rc = Splasher.rc = rc;
        Mopper.rng = Soldier.rng = Splasher.rng = rng;
        Mopper.resourcePattern = Soldier.resourcePattern = Splasher.resourcePattern = rc.getResourcePattern();
        Mopper.towerPatterns = Soldier.towerPatterns = Splasher.towerPatterns = new boolean[][][] {
            rc.getTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER),
            rc.getTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER),
            rc.getTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER)
        };
        while (true) {
            StringBuilder indicatorString = new StringBuilder();
            Motion.indicatorString = indicatorString;
            Motion.updateInfo();
            switch (rc.getType()) {
                case MOPPER -> Mopper.run();
                case SOLDIER -> Soldier.run();
                case SPLASHER -> Splasher.run();
                default -> throw new Exception("Challenge Complete! How Did We Get Here?");
            }
            Clock.yield();
        }
    }
}
