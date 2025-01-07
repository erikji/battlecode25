package SPAARK;

import battlecode.common.*;
import java.util.*;

public class Soldier {
    public static RobotController rc;
    public static Random rng;
    public static MapLocation currLoc;
    public static boolean[][] resourcePattern;
    public static boolean[][][] towerPatterns;
    
    public static void run() throws Exception {
        for (int i = 0; i < 3; i++) {
            if (rc.canMarkTowerPattern(Tower.moneyLevels[i], Motion.currLoc)) {
                System.out.println(rc.getID() + " marked money tower pattern");
                rc.markTowerPattern(Tower.moneyLevels[i], Motion.currLoc);
                break;
            }
        }
            
        for (int i = 0; i < 3; i++) {
            if (rc.canMarkTowerPattern(Tower.paintLevels[i], Motion.currLoc)) {
                System.out.println(rc.getID() + " marked paint tower pattern");
                rc.markTowerPattern(Tower.paintLevels[i], Motion.currLoc);
                break;
            }
        }
        
        if (rc.canCompleteResourcePattern(Motion.currLoc)) {        
            System.out.println(rc.getID() + " completed tower pattern");
            rc.completeResourcePattern(Motion.currLoc);
        }
        Motion.moveRandomly();
        Clock.yield();
    }
}
