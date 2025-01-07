package SPAARK;

import battlecode.common.*;
import java.util.*;

public class Soldier {
    protected static RobotController rc;
    protected static Random rng;
    protected static MapLocation currloc;
    
    public static void run() throws Exception {
        Motion.currLoc = currloc = rc.getLocation();
    }
}
