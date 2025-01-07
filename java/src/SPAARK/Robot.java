package SPAARK;

import battlecode.common.*;
import java.util.*;

public class Robot {
    public static void run(RobotController rc, Random rng) throws Exception {
        Mopper.rc = Soldier.rc = Splasher.rc = rc;
        Mopper.rng = Soldier.rng = Splasher.rng = rng;
        while (true) {
            Motion.updateInfo();
            switch (rc.getType()) {
                case MOPPER:
                    Mopper.run();
                    break;
                case SOLDIER:
                    Soldier.run();
                    break;
                case SPLASHER:
                    Splasher.run();
                    break;
                default:
                    throw new Exception("Challenge Complete! How Did We Get Here?");
            }
            Clock.yield();
        }
    }
}
