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
        Motion.currLoc = rc.getLocation();

        // Find nearby ruin
        MapLocation target = null;
        while (target == null) {
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

        // System.out.println("Found ruin");
        while (Motion.currLoc.distanceSquaredTo(target) > 25) {
            Motion.bugnavTowards(target);
            Motion.updateInfo();
        }

        // System.out.println("in range of ruin");

        Motion.bugnavAround(target, 0, 25);
        Motion.updateInfo();
        if (!rc.senseMapInfo(Motion.currLoc).getMark().equals(PaintType.EMPTY)) {
            if (rc.canAttack(Motion.currLoc)) {
                System.out.println("painting");
                rc.attack(Motion.currLoc);
            }
        }
        for (int i = 0; i < 3; i++) {
            if (rc.canMarkTowerPattern(Tower.paintLevels[i], target)) {
                System.out.println(rc.getID() + " marked paint tower pattern");
                rc.markTowerPattern(Tower.paintLevels[i], target);
                break;
            }
        }

        if (rc.canCompleteResourcePattern(target)) {
            System.out.println(rc.getID() + " completed tower pattern");
            rc.completeResourcePattern(target);
        }
    }
}
