package SPAARK;

import battlecode.common.*;
import java.util.*;

public class Soldier {
    public static RobotController rc;
    public static Random rng;
    public static MapLocation currLoc;
    public static boolean[][] resourcePattern;
    public static boolean[][][] towerPatterns;

    static UnitType ruinBuildType = null;

    public static void run() throws Exception {
        if (rc.getRoundNum() < 1000) {
            Motion.spreadRandomly();
        }
        else {
            // Find nearby ruin
            MapLocation target = null;
            if (target == null) {
                Motion.spreadRandomly();
                Motion.updateInfo();

                MapLocation[] ruins = rc.senseNearbyRuins(-1);
                Arrays.sort(ruins, new Comparator<MapLocation>() {
                    public int compare(MapLocation a, MapLocation b) {
                        return a.distanceSquaredTo(rc.getLocation()) - b.distanceSquaredTo(rc.getLocation());
                    };
                });
                for (MapLocation m : rc.senseNearbyRuins(-1)) {
                    // ADD CODE TO CHECK IF NOT SENT YET
                    target = m;
                    break;
                }
            }
            
            if (target != null) {
                if (rc.getLocation().distanceSquaredTo(target) > 25) {
                    Motion.bugnavTowards(target);
                    Motion.updateInfo();
                }
                else {
                    // System.out.println("Target = (" + target.x + ", " + target.y + ")");
            
                    Motion.bugnavAround(target, 1, 25);
                    Motion.updateInfo();

                    if (rng.nextInt(100) > 50) {
                        for (int i = 0; i < 3; i++) {
                            if (rc.canMarkTowerPattern(Tower.paintLevels[i], target)) {
                                // System.out.println(rc.getID() + " marked paint tower pattern");
                                rc.markTowerPattern(Tower.paintLevels[i], target);
                                ruinBuildType = Tower.paintLevels[i];
                                break;
                            }
                        }
                    }
                    else {
                        for (int i = 0; i < 3; i++) {
                            if (rc.canMarkTowerPattern(Tower.moneyLevels[i], target)) {
                                // System.out.println(rc.getID() + " marked money tower pattern");
                                rc.markTowerPattern(Tower.moneyLevels[i], target);
                                // System.out.println(rc.senseMapInfo(rc.getLocation()).getMark() + " AFTERARDS IS THE MARK TYPE");
                                ruinBuildType = Tower.moneyLevels[i];
                                break;
                            }
                        }
                    }

                    if (rc.senseMapInfo(rc.getLocation()).getMark().equals(PaintType.ALLY_PRIMARY)) {
                        boolean type = rc.senseMapInfo(rc.getLocation()).getMark().isSecondary();
                        if (rc.canAttack(rc.getLocation())) {
                            // System.out.println("painting");
                            rc.attack(rc.getLocation(), type);
                        }
                    }


                    if (ruinBuildType != null &&  rc.canCompleteTowerPattern(ruinBuildType, target)) {
                        System.out.println(rc.getID() + " completed tower pattern");
                        rc.canCompleteTowerPattern(ruinBuildType, target);
                        rc.buildRobot(ruinBuildType, target);
                    }
                }
            }
        }
    }
}
