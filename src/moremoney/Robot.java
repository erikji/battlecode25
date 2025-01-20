package moremoney;

import battlecode.common.*;

public class Robot {
    public static boolean[][] resourcePattern;
    public static boolean[][][] towerPatterns;
    public static UnitType[] towers = new UnitType[] {
            UnitType.LEVEL_ONE_DEFENSE_TOWER,
            UnitType.LEVEL_ONE_MONEY_TOWER,
            UnitType.LEVEL_ONE_PAINT_TOWER
    };

    public static final double RETREAT_PAINT_RATIO = 0.75; // OPTNET_PARAM

    public static void init() throws Exception {
        resourcePattern = G.rc.getResourcePattern();
        towerPatterns = new boolean[][][] {
                G.rc.getTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER),
                G.rc.getTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER),
                G.rc.getTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER)
        };
        Motion.paintNeededToStopRetreating = (int) (G.rc.getType().paintCapacity * RETREAT_PAINT_RATIO);
    }
    
    public static void run() throws Exception {
        Motion.paintLost += Math.max(Motion.lastPaint - G.rc.getPaint(), 0);
        if (G.rc.getPaint() == 0) {
            for (int i = G.allyRobots.length; --i >= 0;) {
                if (G.allyRobots[i].location.distanceSquaredTo(G.me) <= 8 && G.allyRobots[i].getType().isRobotType()) {
                    G.rc.disintegrate();
                }
            }
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
