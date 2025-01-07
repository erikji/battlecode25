package SPAARK;

import battlecode.common.*;
import java.util.*;

public class Robot {
    public static void run(RobotController rc, Random rng) throws Exception {
        while (true) {
            switch (rc.getType()) {
                case MOPPER:
                    Mopper.run(rc, rng);
                    break;
                case SOLDIER:
                    Soldier.run(rc, rng);
                    break;
                case SPLASHER:
                    Splasher.run(rc, rng);
                    break;
                default:
                    throw new Exception("Challenge Complete! How Did We Get Here?");
            }
        }
    }
}
