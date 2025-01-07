package SPAARK;

import battlecode.common.*;
import java.util.*;

public class DefenseTower {
    protected static RobotController rc;
    protected static Random rng;
    protected static MapLocation currloc;
    protected static int level = 0;
    
    public static void run() throws Exception {
        currloc = rc.getLocation();
    }
}
