package SPAARK;

import battlecode.common.*;
import java.util.*;

public class Mopper {
    public static RobotController rc;
    public static Random rng;
    public static MapLocation currloc;

    public static void run() throws Exception {
        Motion.currLoc = currloc = rc.getLocation();
    }

    /**Move this out later!!! */
    public static void attackMicro() throws Exception {
        int left = 0, right = 0, up = 0, down = 0;
        RobotInfo r = rc.senseRobotAtLocation(currloc.add(Direction.NORTH));
        if (r != null && r.getTeam().opponent() == rc.getTeam()) up++;
        r = rc.senseRobotAtLocation(currloc.add(Direction.NORTHEAST));
        if (r != null && r.getTeam().opponent() == rc.getTeam()) {
            up++;
            right++;
        }
    }
}
