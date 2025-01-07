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
        }
    }
}
