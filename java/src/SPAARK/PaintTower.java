package SPAARK;

import battlecode.common.*;
import java.util.*;

public class PaintTower {
    protected static RobotController rc;
    protected static Random rng;
    protected static MapLocation currloc;
    protected static int level = 0;
    
    public static void run() throws Exception {
        currloc = rc.getLocation();
    }
}
