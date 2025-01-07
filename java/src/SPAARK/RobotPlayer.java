package SPAARK;

import battlecode.common.*;
import java.util.*;

public class RobotPlayer {
    protected static Random rng;

    public static void run(RobotController rc) throws Exception {
        try {
            rng = new Random(rc.getID() + 2025);
            Motion.rc = rc;
            Motion.rng = rng;
            Micro.rc = rc;
            Micro.rng = rng;
            switch (rc.getType()) {
                case MOPPER:
                case SOLDIER:
                case SPLASHER:
                    Robot.run(rc, rng);
                    break;
                default:
                    Tower.run(rc, rng);
                    break;
            }
        } catch (GameActionException e) {
            System.out.println("Unexpected GameActionException");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Unexpected Exception");
            e.printStackTrace();
        }
    }
}