package SPAARK;

import java.util.Random;

import battlecode.common.*;

public class Micro {
    public static void micro(Direction optimalDir, MapLocation dest) throws GameActionException {
        Motion.move(optimalDir);
        //doesn't perform an action tho
    }
}