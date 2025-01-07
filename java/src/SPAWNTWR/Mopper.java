package SPAWNTWR;

import battlecode.common.*;
import java.util.*;

public class Mopper {

    public static void run(RobotController rc, Random rng) throws Exception {
        MapLocation target = null;
        for (MapLocation m : rc.senseNearbyRuins(-1)) {
            if (rc.senseMapInfo(m).getPaint().isAlly()) {
                target = m;
            }
        }
        if (target == null) {
            Motion.moveRandomly();
        }
        else {
            if (rc.getRoundNum() < rng.nextInt(1000) * 2 / 3) {
                for (int i = 0; i < 3; i++) {
                    if (rc.canMarkTowerPattern(Tower.moneyLevels[i], rc.getLocation())) {
                        System.out.println(rc.getID() + " marked tower pattern at ()" + rc.getLocation().x + ", " + rc.getLocation().y + ")");
                        rc.markTowerPattern(Tower.moneyLevels[i], rc.getLocation());
                        break;
                    }
                }
            }
            else {
                for (int i = 0; i < 3; i++) {
                    if (rc.canMarkTowerPattern(Tower.paintLevels[i], rc.getLocation())) {
                        rc.markTowerPattern(Tower.paintLevels[i], rc.getLocation());
                        break;
                    }
                }
            }
            if (rc.canCompleteResourcePattern(rc.getLocation())) {
                rc.completeResourcePattern(rc.getLocation());
                System.out.println(rc.getID() + " completed resource pattern at (" + rc.getLocation().x + ", " + rc.getLocation().y + ")");
                Motion.bugnavAway(target);
            }
            if (rc.canUpgradeTower(rc.getLocation())) {
                rc.upgradeTower(rc.getLocation());
                System.out.println(rc.getID() + " upgraded resource pattern at (" + rc.getLocation().x + ", " + rc.getLocation().y + ")");
                Motion.bugnavAway(target);
            } 
            Motion.bugnavTowards(target);
        }
        Clock.yield();
    }
}
