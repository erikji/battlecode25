package aggressive;

import battlecode.common.*;

public class Robot {
    public static boolean[][] resourcePattern;
    public static boolean[][][] towerPatterns;
    public static UnitType[] towers = new UnitType[] {
            UnitType.LEVEL_ONE_DEFENSE_TOWER,
            UnitType.LEVEL_ONE_MONEY_TOWER,
            UnitType.LEVEL_ONE_PAINT_TOWER
    };

    public static void init() throws Exception {
        resourcePattern = G.rc.getResourcePattern();
        towerPatterns = new boolean[][][] {
                G.rc.getTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER),
                G.rc.getTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER),
                G.rc.getTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER)
        };
    }

    public static void run() throws Exception {
        Motion.paintLost += Math.max(Motion.lastPaint - G.rc.getPaint(), 0);
        if (G.rc.getPaint() == 0 && G.allyRobots.length > 0) {
            G.rc.disintegrate();
        }
        switch (G.rc.getType()) {
            case MOPPER -> Mopper.run();
            case SOLDIER -> Soldier.run();
            case SPLASHER -> Splasher.run();
            default -> throw new Exception("Challenge Complete! How Did We Get Here?");
        }
        Motion.lastPaint = G.rc.getPaint();
        G.indicatorString.append("SYM="
                + (POI.symmetry[0] ? "1" : "0") + (POI.symmetry[1] ? "1" : "0") + (POI.symmetry[2] ? "1 " : "0 "));
        POI.drawIndicators();
    }
}
