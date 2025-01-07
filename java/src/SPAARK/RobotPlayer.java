package SPAARK;

import battlecode.common.*;
import java.util.*;

public class RobotPlayer {
    protected static Random rng;
    public static void run(RobotController rc) {
        try {
            rng = new Random(rc.getID()+2025);
            Motion.rc = rc;
            Motion.rng = rng;
            Motion.mapCenter = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);
            Micro.rc = rc;
            Micro.rng = rng;
            switch (rc.getType()) {
                case MOPPER:
                    Robot.run(rc,rng);
                    break;
                case SOLDIER:
                    Robot.run(rc,rng);
                    break;
                case SPLASHER:
                    Robot.run(rc,rng);
                    break;
                default:
                    Tower.run(rc,rng);
                    break;
            }
        } catch (GameActionException e) {
            System.out.println("GameActionException");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
        }
    }
}