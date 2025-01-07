package SPAARK;

import battlecode.common.*;
import java.util.*;

public class MoneyTower {
    public static RobotController rc;
    public static Random rng;
    public static MapLocation loc;
    public static int level = 0;
    
    public static void run() throws Exception {
        if (level < 2 && rc.canUpgradeTower(rc.getLocation())) {
            //greedily upgrade tower to level 2 if possible
            //takes only 75 turns to pay for itself!!!
            rc.upgradeTower(loc);
        }
    }
}
