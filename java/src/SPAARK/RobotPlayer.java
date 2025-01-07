package SPAARK;

import battlecode.common.*;
import java.util.*;

public class RobotPlayer {
    public static Random rng;
    public static void run(RobotController rc) {
        rng = new Random(rc.getID()+2025);
        Motion.rc = rc;
        Motion.rng = rng;
        Micro.rc = rc;
        Micro.rng = rng;
        switch (rc.getType()) {
            case LEVEL_ONE_DEFENSE_TOWER:
                new DefenseTower(rc,1);
                break;
            case LEVEL_TWO_DEFENSE_TOWER:
                new DefenseTower(rc,2);
                break;
            case LEVEL_THREE_DEFENSE_TOWER:
                new DefenseTower(rc,3);
                break;
            case LEVEL_ONE_MONEY_TOWER:
                new MoneyTower(rc,1);
                break;
            case LEVEL_TWO_MONEY_TOWER:
                new MoneyTower(rc,2);
                break;
            case LEVEL_THREE_MONEY_TOWER:
                new MoneyTower(rc,3);
                break;
            case LEVEL_ONE_PAINT_TOWER:
                new PaintTower(rc,1);
                break;
            case LEVEL_TWO_PAINT_TOWER:
                new PaintTower(rc,2);
                break;
            case LEVEL_THREE_PAINT_TOWER:
                new PaintTower(rc,3);
                break;
            case MOPPER:
                new Mopper(rc);
                break;
            case SOLDIER:
                new Soldier(rc);
                break;
            case SPLASHER:
                new Splasher(rc);
                break;
        }
    }
}