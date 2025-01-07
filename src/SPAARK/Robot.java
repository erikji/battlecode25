package SPAARK;

import battlecode.common.*;
import java.util.*;

public class Robot {
    public static boolean[][] resourcePattern;
    public static boolean[][][] towerPatterns;
    public static void init() throws Exception {
        resourcePattern = G.rc.getResourcePattern();
        towerPatterns = new boolean[][][] {
            G.rc.getTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER),
            G.rc.getTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER),
            G.rc.getTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER)
        };
    }
    public static void run() throws Exception {
        while (true) {
            Motion.updateInfo();
            switch (G.rc.getType()) {
                case MOPPER -> Mopper.run();
                case SOLDIER -> Soldier.run();
                case SPLASHER -> Splasher.run();
                default -> throw new Exception("Challenge Complete! How Did We Get Here?");
            }
            G.indicatorString.append("SYM="+(POI.symmetry[0]?"0":"1")+(POI.symmetry[1]?"0":"1")+(POI.symmetry[2]?"0 ":"1 "));
        }
    }
    public static boolean isTower(UnitType t) throws Exception {
        return t != UnitType.MOPPER && t != UnitType.SOLDIER && t != UnitType.SPLASHER;
    }
}
