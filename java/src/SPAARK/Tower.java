package SPAARK;

import battlecode.common.*;

public class Tower {
    public static void run(RobotController rc) {
        while (true) {
            switch (rc.getType()) {
                case LEVEL_ONE_DEFENSE_TOWER:
                    DefenseTower.run(rc,1);
                    break;
                case LEVEL_TWO_DEFENSE_TOWER:
                    DefenseTower.run(rc,2);
                    break;
                case LEVEL_THREE_DEFENSE_TOWER:
                    DefenseTower.run(rc,3);
                    break;
                case LEVEL_ONE_MONEY_TOWER:
                    MoneyTower.run(rc,1);
                    break;
                case LEVEL_TWO_MONEY_TOWER:
                    MoneyTower.run(rc,2);
                    break;
                case LEVEL_THREE_MONEY_TOWER:
                    MoneyTower.run(rc,3);
                    break;
                case LEVEL_ONE_PAINT_TOWER:
                    PaintTower.run(rc,1);
                    break;
                case LEVEL_TWO_PAINT_TOWER:
                    PaintTower.run(rc,2);
                    break;
                case LEVEL_THREE_PAINT_TOWER:
                    PaintTower.run(rc,3);
                    break;
                default:
                    throw Exception("buh");
        }
    }
}
