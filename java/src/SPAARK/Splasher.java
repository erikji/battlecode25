package SPAARK;

import battlecode.common.*;
import java.util.*;

public class Splasher {
    public static RobotController rc;
    public static Random rng;
    public static MapLocation currloc;
    
    public static void run() throws Exception {
        Motion.currLoc = currloc = rc.getLocation();
    }
}
