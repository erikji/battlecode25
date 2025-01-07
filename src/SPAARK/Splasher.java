package SPAARK;

import battlecode.common.*;
import java.util.*;

public class Splasher {
    public static RobotController rc;
    public static Random rng;
    public static MapLocation currLoc;
    public static boolean[][] resourcePattern;
    public static boolean[][][] towerPatterns;

    public static void run() throws Exception {

        // Find nearby ruin
        MapLocation target = null;
        if (target == null) {
            for (MapLocation m : rc.senseNearbyRuins(-1)) {
                if (!Tower.towerLocs.toString().contains(m.x + "" + m.y + "|")) {
                    Tower.towerLocs.append(m.x + "" + m.y + "|");
                    target = m;
                    break;
                }
            }
            Motion.spreadRandomly();
            Motion.updateInfo();
        }
         
        if (target != null) {
            if (rc.getLocation().distanceSquaredTo(target) > 25) {
                Motion.bugnavTowards(target);
                Motion.updateInfo();
            }
            else {
                System.out.println("in range of ruin with dist: " + rc.getLocation().distanceSquaredTo(target));
        
                Motion.bugnavAround(target, 1, 25);
                Motion.updateInfo();

                if (!rc.senseMapInfo(rc.getLocation()).getMark().equals(PaintType.EMPTY)) {
                    if (rc.canAttack(rc.getLocation())) {
                        System.out.println("painting");
                        rc.attack(rc.getLocation());
                    }
                }

                if (rng.nextInt(100) > 50) {
                    for (int i = 0; i < 3; i++) {
                        if (rc.canMarkTowerPattern(Tower.paintLevels[i], target)) {
                            System.out.println(rc.getID() + " marked paint tower pattern");
                            rc.markTowerPattern(Tower.paintLevels[i], target);
                            break;
                        }
                    }
                }
                else {
                    for (int i = 0; i < 3; i++) {
                        if (rc.canMarkTowerPattern(Tower.moneyLevels[i], target)) {
                            System.out.println(rc.getID() + " marked money tower pattern");
                            rc.markTowerPattern(Tower.moneyLevels[i], target);
                            break;
                        }
                    }
                }

                if (rc.canCompleteResourcePattern(target)) {
                    System.out.println(rc.getID() + " completed tower pattern");
                    rc.completeResourcePattern(target);
                }
            }
        }
    }
}
