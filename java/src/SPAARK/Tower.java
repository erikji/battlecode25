package SPAARK;

import battlecode.common.*;
import java.util.*;

public class Tower {
    public static void run(RobotController rc, Random rng) throws Exception {
        DefenseTower.rc = MoneyTower.rc = PaintTower.rc = rc;
        DefenseTower.rng = MoneyTower.rng = PaintTower.rng = rng;
        while (true) {
            switch (rc.getType()) {
                case LEVEL_ONE_DEFENSE_TOWER:
                    DefenseTower.level = 0;
                case LEVEL_TWO_DEFENSE_TOWER:
                    DefenseTower.level = 1;
                case LEVEL_THREE_DEFENSE_TOWER:
                    DefenseTower.level = 2;
                    DefenseTower.run();
                    break;
                case LEVEL_ONE_MONEY_TOWER:
                    MoneyTower.level = 0;
                case LEVEL_TWO_MONEY_TOWER:
                    MoneyTower.level = 1;
                case LEVEL_THREE_MONEY_TOWER:
                    MoneyTower.level = 2;
                    MoneyTower.run();
                    break;
                case LEVEL_ONE_PAINT_TOWER:
                    PaintTower.level = 0;
                case LEVEL_TWO_PAINT_TOWER:
                    PaintTower.level = 1;
                case LEVEL_THREE_PAINT_TOWER:
                    PaintTower.level = 2;
                    PaintTower.run();
                    break;
                default:
                    throw new Exception("Challenge Complete! How Did We Get Here?");
            }
        }
    }
}
