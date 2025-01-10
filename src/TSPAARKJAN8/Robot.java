package TSPAARKJAN8;

import battlecode.common.MapLocation;
import battlecode.common.UnitType;

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
        switch (G.rc.getType()) {
            case MOPPER -> Mopper.run();
            case SOLDIER -> Soldier.run();
            case SPLASHER -> Splasher.run();
            default -> throw new Exception("Challenge Complete! How Did We Get Here?");
        }
        G.indicatorString.append("SYM="
                + (POI.symmetry[0] ? "0" : "1") + (POI.symmetry[1] ? "0" : "1") + (POI.symmetry[2] ? "0 " : "1 "));
    }

    public static int retreatTower = -1;
    public static StringBuilder triedRetreatTowers = new StringBuilder();

    public static void retreat() throws Exception {
        // retreats to an ally tower
        // depends on which information needs to be transmitted and if tower has paint
        if (retreatTower != -1) {
            if (POI.parseTowerTeam(POI.towers[retreatTower]) != G.rc.getTeam()) {
                retreatTower = -1;
            }
        }
        if (retreatTower != -1) {
            MapLocation loc = POI.parseLocation(POI.towers[retreatTower]);
            if (G.rc.canSenseRobotAtLocation(loc)) {
                if (G.rc.senseNearbyRobots(loc, 2, G.rc.getTeam()).length > 4) {
                    retreatTower = -1;
                }
            }
        }
        if (retreatTower == -1) {
            int best = -1;
            while (best == -1) {
                int bestDistance = 0;
                boolean bestPaint = false;
                boolean bestCritical = false;
                String tried = triedRetreatTowers.toString();
                for (int i = 49; --i >= 0; ) {
                    if (POI.towers[i] == -1) {
                        break;
                    }
                    if (POI.parseTowerTeam(POI.towers[i]) != G.rc.getTeam()) {
                        continue;
                    }
                    boolean paint = POI.parseTowerType(POI.towers[i]) == UnitType.LEVEL_ONE_PAINT_TOWER;
                    if (!paint) {
                        // This is dumb but borks code for some reason
                        continue;
                    }
                    if (tried.contains("-" + i + "-")) {
                        continue;
                    }
                    int distance = Motion.getChebyshevDistance(G.me, POI.parseLocation(POI.towers[i]));
                    if (best == -1) {
                        best = i;
                        bestDistance = distance;
                        bestCritical = POI.critical[i];
                        bestPaint = paint;
                    } else if (bestCritical && !POI.critical[i]) {
                        best = i;
                        bestDistance = distance;
                        bestCritical = POI.critical[i];
                        bestPaint = paint;
                    } else if (paint && !bestPaint) {
                        best = i;
                        bestDistance = distance;
                        bestCritical = POI.critical[i];
                        bestPaint = paint;
                    } else if (distance < bestDistance) {
                        best = i;
                        bestDistance = distance;
                        bestCritical = POI.critical[i];
                        bestPaint = paint;
                    }
                }
                if (best == -1) {
                    triedRetreatTowers = new StringBuilder();
                    continue;
                }
                retreatTower = best;
                triedRetreatTowers.append(":" + best + ":");
                break;
            }
        }
        if (retreatTower != -1) {
            MapLocation loc = POI.parseLocation(POI.towers[retreatTower]);
            // G.rc.setIndicatorLine(G.me, loc, 255, 0, 255);
            Motion.bugnavTowards(loc);
            if (G.rc.canSenseRobotAtLocation(loc)) {
                int amt = -Math.min(G.rc.getType().paintCapacity - G.rc.getPaint(),
                        G.rc.senseRobotAtLocation(loc).getPaintAmount());
                if (G.rc.canTransferPaint(loc, amt)) {
                    G.rc.transferPaint(loc, amt);
                }
            }
        }
    }
}
