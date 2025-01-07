package SPAARK;

import battlecode.common.*;
import java.util.*;

public class RobotPlayer {
    public static void run(RobotController rc) throws Exception {
        try {
            Random rng = new Random(rc.getID() + 2025);
            Motion.rc = rc;
            Motion.rng = rng;
            Motion.mapCenter = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);
            Micro.rc = rc;
            Micro.rng = rng;
            POI.opponentTeam = rc.getTeam().opponent();
            Soldier.rc = rc;
            Soldier.rng = rng;
            Motion.currLoc = rc.getLocation();
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