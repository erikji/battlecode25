package SPAARK;

import battlecode.common.*;
import java.util.*;

public class Soldier {
    //modes
    public static MapLocation ruinLocation = null;
    public static final int EXPLORE = 0;
    public static final int BUILD_RUIN = 1;
    public static final int ATTACK = 2;
    public static int mode = EXPLORE;

    public static void run() throws Exception {
        if (mode == EXPLORE) {
            G.indicatorString.append("EXPLORE ");
            Motion.spreadRandomly();
            MapInfo me = G.rc.senseMapInfo(Motion.currLoc);
            if (me.getPaint() != PaintType.ALLY_PRIMARY && me.getPaint() != PaintType.ALLY_SECONDARY && G.rc.canAttack(Motion.currLoc)) {
                G.rc.attack(Motion.currLoc); //also add logic to paint in special resource pattern
            }
            MapLocation[] locs = G.rc.senseNearbyRuins(-1);
            for (MapLocation loc : locs) {
                if (G.rc.canSenseRobotAtLocation(loc)) {
                    continue;
                }
                ruinLocation = loc;
                mode = BUILD_RUIN;
                break;
            }
        }
        if (mode == BUILD_RUIN) {
            G.indicatorString.append("BUILD_RUIN ");
            G.rc.setIndicatorLine(G.rc.getLocation(), ruinLocation, 255, 0, 0);
            if (!G.rc.canSenseLocation(ruinLocation) || G.rc.canSenseRobotAtLocation(ruinLocation)) {
                mode = EXPLORE;
                ruinLocation = null;
            } else {
                PaintType mark = G.rc.senseMapInfo(ruinLocation.add(ruinLocation.directionTo(Motion.currLoc))).getMark();
                if (!mark.isAlly()) {
                    if (G.rng.nextBoolean()) {
                        if (G.rc.canMarkTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, ruinLocation)) {
                            G.rc.markTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, ruinLocation);
                        }
                    } else {
                        if (G.rc.canMarkTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, ruinLocation)) {
                            G.rc.markTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, ruinLocation);
                        }
                    }
                }
                MapInfo[] infos = G.rc.senseNearbyMapInfos();
                for (MapInfo info : infos) {
                    if (info.getMark() != PaintType.EMPTY && info.getPaint() != info.getMark() && G.rc.canAttack(G.rc.getLocation())) {
                        G.rc.attack(info.getMapLocation(), info.getMark().isSecondary());
                        for (UnitType ruinType : G.towerTypes) {
                            if (G.rc.canCompleteTowerPattern(ruinType, ruinLocation)) {
                                G.rc.completeTowerPattern(ruinType, ruinLocation);
                                POI.addTower(-1, POI.intifyTower(G.rc.getTeam().ordinal()) | POI.intifyLocation(ruinLocation));
                                // probably also transmit some information
                                mode = EXPLORE;
                                ruinLocation = null;
                                break;
                            }
                        }
                        break;
                    }
                }
                if (ruinLocation != null) {
                    Motion.bugnavAround(ruinLocation, 1, 2);
                }
            }
        }
    }
}
