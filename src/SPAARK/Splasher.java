package SPAARK;

import battlecode.common.*;
import java.util.*;

public class Splasher {
    public static void run() throws Exception {

        if (G.rc.getRoundNum() < 1000) {
            Motion.spreadRandomly();
        }
        else {
            // Find nearby ruin
            MapLocation target = null;
            if (target == null) {
                Motion.spreadRandomly();
                Motion.updateInfo();

                MapLocation[] ruins = G.rc.senseNearbyRuins(-1);
                Arrays.sort(ruins, new Comparator<MapLocation>() {
                    public int compare(MapLocation a, MapLocation b) {
                        return a.distanceSquaredTo(G.rc.getLocation()) - b.distanceSquaredTo(G.rc.getLocation());
                    };
                });
                for (MapLocation m : G.rc.senseNearbyRuins(-1)) {
                    // ADD CODE TO CHECK IF NOT SENT YET
                    target = m;
                    break;
                }
            }
            
            if (target != null) {
                if (G.rc.getLocation().distanceSquaredTo(target) > 25) {
                    Motion.bugnavTowards(target);
                    Motion.updateInfo();
                }
                else {
                    // System.out.println("Target = (" + target.x + ", " + target.y + ")");
            
                    Motion.bugnavAround(target, 1, 25);
                    Motion.updateInfo();

                    if (G.rc.canMarkTowerPattern(Tower.paintLevels[2], target)) {
                        System.out.println(G.rc.getID() + " marked paint tower pattern");
                        G.rc.markTowerPattern(Tower.paintLevels[2], target);
                    }

                    // if (G.rng.nextInt(100) > 50) {
                    //     for (int i = 0; i < 3; i++) {
                    //         if (G.rc.canMarkTowerPattern(Tower.paintLevels[i], target)) {
                    //             System.out.println(G.rc.getID() + " marked paint tower pattern");
                    //             G.rc.markTowerPattern(Tower.paintLevels[i], target);
                    //             break;
                    //         }
                    //     }
                    // }
                    // else {
                    //     for (int i = 0; i < 3; i++) {
                    //         if (G.rc.canMarkTowerPattern(Tower.moneyLevels[i], target)) {
                    //             System.out.println(G.rc.getID() + " marked money tower pattern");
                    //             G.rc.markTowerPattern(Tower.moneyLevels[i], target);
                    //             System.out.println(G.rc.senseMapInfo(G.rc.getLocation()).getMark() + " AFTERARDS IS THE MARK TYPE");

                    //             break;
                    //         }
                    //     }
                    // }

                    if (G.rc.senseMapInfo(G.rc.getLocation()).getMark().equals(PaintType.ALLY_PRIMARY)) {
                        if (G.rc.canAttack(G.rc.getLocation())) {
                            System.out.println("painting");
                            G.rc.attack(G.rc.getLocation());
                        }
                    }

                    if (G.rc.canCompleteResourcePattern(target)) {
                        System.out.println(G.rc.getID() + " completed tower pattern");
                        G.rc.completeResourcePattern(target);
                    }
                }
            }
        }
    }
}
