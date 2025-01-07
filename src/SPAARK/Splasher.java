package SPAARK;

import battlecode.common.*;
import java.util.*;

public class Splasher {
    public static RobotController rc;
    public static Random rng;
    public static MapLocation currLoc;
    
    public static void run() throws Exception {
        Motion.currLoc = currLoc = rc.getLocation();

        if (rc.senseMapInfo(currLoc).getMark().equals(PaintType.EMPTY)) {
            System.out.println("marked");
            if (rc.canAttack(currLoc)) {
                System.out.println("splasher adding paint");
                rc.attack(currLoc);
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
