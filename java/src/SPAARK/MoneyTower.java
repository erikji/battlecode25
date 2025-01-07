package SPAARK;

import battlecode.common.*;
import java.util.*;

public class MoneyTower {
    protected static RobotController rc;
    protected static Random rng;
    protected static MapLocation loc;
    protected static int level = 0;
    
    public static void run() throws Exception {
        if (level < 2 && rc.canUpgradeTower(loc)) {
            //greedily upgrade tower to level 2 if possible
            //takes only 75 turns to pay for itself!!!
            rc.upgradeTower(loc);
        }
    }
}
