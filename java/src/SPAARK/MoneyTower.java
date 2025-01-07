package SPAARK;

import battlecode.common.*;
import java.util.*;

public class MoneyTower {
    public static void run(RobotController rc, Random rng) throws Exception {
        if (Tower.level < 2 && rc.canUpgradeTower(rc.getLocation())) {
            //greedily upgrade tower to level 2 if possible
            //takes only 75 turns to pay for itself!!!
            rc.upgradeTower(rc.getLocation());
        }
    }
}
