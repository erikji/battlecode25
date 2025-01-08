package SPAARK;

import battlecode.common.*;
import java.util.*;

public class Soldier {
    // modes
    public static MapLocation ruinLocation = null; // BUILD mode
    public static MapLocation towerLocation = null; // ATTACK mode
    public static final int EXPLORE = 0;
    public static final int BUILD = 1;
    public static final int ATTACK = 2;
    public static final int RETREAT = 3;
    public static int mode = EXPLORE;

    public static void run() throws Exception {
        if (G.rc.getPaint() < G.rc.getType().paintCapacity / 3) {
            mode = RETREAT;
        } else {
            mode = EXPLORE;
            MapLocation[] locs = G.rc.senseNearbyRuins(-1);
            for (MapLocation loc : locs) {
                if (G.rc.canSenseRobotAtLocation(loc)) {
                    continue; // tower already there
                }
                ruinLocation = loc;
                mode = BUILD;
                break;
            }
        }
        switch (mode) {
            case EXPLORE:
                G.indicatorString.append("EXPLORE ");
                Motion.spreadRandomly();
                MapInfo me = G.rc.senseMapInfo(G.me);
                if (me.getPaint() != PaintType.ALLY_PRIMARY && me.getPaint() != PaintType.ALLY_SECONDARY
                        && G.rc.canAttack(G.me)) {
                    G.rc.attack(G.me); // also add logic to paint in special resource pattern
                }
                break;
            case BUILD:
                G.indicatorString.append("BUILD ");
                G.rc.setIndicatorLine(G.rc.getLocation(), ruinLocation, 255, 255, 0);
                if (!G.rc.canSenseLocation(ruinLocation) || G.rc.canSenseRobotAtLocation(ruinLocation)) {
                    mode = EXPLORE;
                    ruinLocation = null;
                } else {
                    PaintType mark = G.rc.senseMapInfo(ruinLocation.add(ruinLocation.directionTo(G.me))).getMark();
                    if (!mark.isAlly()) {
                        // if (POI.getNumChipTowers() * 3 > G.rc.getNumberTowers() -
                        // POI.getNumChipTowers()) {
                        // oof chips don't work
                        // if (POI.getNumChipTowers() > G.rc.getNumberTowers() - POI.getNumChipTowers())
                        // {
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
                        if (info.getMark().isAlly() && info.getPaint() != info.getMark()
                                && G.rc.canAttack(info.getMapLocation())) {
                            G.rc.attack(info.getMapLocation(), info.getMark().isSecondary());
                            G.rc.setIndicatorLine(G.me, info.getMapLocation(), 0, 255, 255);
                            break;
                        }
                    }
                    for (UnitType ruinType : G.towerTypes) {
                        if (G.rc.canCompleteTowerPattern(ruinType, ruinLocation) && G.rc.getPaint() > 50) {
                            G.rc.completeTowerPattern(ruinType, ruinLocation);
                            POI.addTower(-1,
                                    POI.intifyTower(G.rc.getTeam(), ruinType) | POI.intifyLocation(ruinLocation));
                            mode = EXPLORE;
                            ruinLocation = null;
                            break;
                        }
                    }
                    if (ruinLocation != null) {
                        Motion.bugnavAround(ruinLocation, 1, 2);
                    }
                }
                break;
            case ATTACK:
                G.indicatorString.append("ATTACK ");
                // do some attack micro idk
                // pretty useless with the range nerf
                break;
            case RETREAT:
                G.indicatorString.append("RETREAT ");
                Robot.retreat();
                break;
        }
    }
}
