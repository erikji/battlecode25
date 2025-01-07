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
                case MOPPER:
                    Mopper.run();
                    break;
                case SOLDIER:
                    Soldier.run();
                    break;
                case SPLASHER:
                    Splasher.run();
                    break;
                default:
                    throw new Exception("Challenge Complete! How Did We Get Here?");
            }
            indicatorString.append("SYM="+(Motion.symmetry[0]?"0":"1")+(Motion.symmetry[1]?"0":"1")+(Motion.symmetry[2]?"0 ":"1 "));
            rc.setIndicatorString(indicatorString.toString());
            Clock.yield();
        }
    }
}
