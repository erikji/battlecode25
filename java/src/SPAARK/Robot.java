package SPAARK;

import battlecode.common.*;

public class Robot {
    public static void run(RobotController rc, Random rng) {
        while (true) {
            switch (rc.getType()) {
                case MOPPER:
                    Mopper.run(rc);
                    break;
                case SOLDIER:
                    Soldier.run(rc);
                    break;
                case SPLASHER:
                    Splasher.run(rc);
                    break;
                default:
                    throw Exception("buh");
            }
        }
    }
}
