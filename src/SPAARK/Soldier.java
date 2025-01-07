package SPAARK;

import battlecode.common.*;
import java.util.*;

public class Soldier {
    public static RobotController rc;
    public static Random rng;
    public static MapLocation currLoc;
    
    public static void run() throws Exception {
       for (int i = 0; i < 3; i++) {
                if (rc.canMarkTowerPattern(Tower.moneyLevels[i], Motion.currLoc)) {
                    rc.markTowerPattern(Tower.moneyLevels[i], Motion.currLoc);
                    break;
                }
            }
        
       for (int i = 0; i < 3; i++) {
            if (rc.canMarkTowerPattern(Tower.paintLevels[i], Motion.currLoc)) {
                rc.markTowerPattern(Tower.paintLevels[i], Motion.currLoc);
                break;
            }
        }
        
        if (rc.canCompleteResourcePattern(Motion.currLoc)) {
            rc.completeResourcePattern(Motion.currLoc);
        }
        Motion.spreadRandomly();
        Clock.yield();
    }
}
