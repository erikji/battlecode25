package SPAARK;

import battlecode.common.*;
import java.util.*;

public class Soldier {
    public static MapLocation ruinLocation = null; //BUILD mode
    public static MapLocation towerLocation = null; //ATTACK mode
    public static final int EXPLORE = 0;
    public static final int BUILD = 1;
    public static final int ATTACK = 2;
    public static final int RETREAT = 3;
    public static int mode = EXPLORE;

    public static void run() throws Exception {
        if (G.rc.getPaint() < G.rc.getType().paintCapacity / 3) {
            mode = RETREAT;
        } else if (G.rc.getPaint() > G.rc.getType().paintCapacity - 40) {
            mode = EXPLORE;
        }
        if (mode == EXPLORE) {
            MapLocation[] locs = G.rc.senseNearbyRuins(-1);
            for (MapLocation loc : locs) {
                if (G.rc.canSenseRobotAtLocation(loc)) {
                    RobotInfo bot = G.rc.senseRobotAtLocation(loc);
                    if (bot.team == POI.opponentTeam && bot.type.actionRadiusSquared <= G.rc.getType().actionRadiusSquared) {
                        towerLocation = loc;
                        mode = ATTACK;
                        break;
                    }
                } else {
                    ruinLocation = loc;
                    mode = BUILD;
                    break;
                }
            }
        }
        switch (mode) {
            case EXPLORE:
                G.indicatorString.append("EXPLORE ");
                MapLocation bestLoc = null;
                int bestDistanceSquared = 10000;
                for (int i = 0; i < 50; i++) {
                    if (POI.towers[i] == -1) {
                        break;
                    }
                    if (POI.parseTowerTeam(POI.towers[i]) == Team.NEUTRAL) {
                        if (G.me.isWithinDistanceSquared(POI.parseLocation(POI.towers[i]), bestDistanceSquared)) {
                            bestDistanceSquared = G.me.distanceSquaredTo(POI.parseLocation(POI.towers[i]));
                            bestLoc = POI.parseLocation(POI.towers[i]);
                        }
                    }
                }
                if (bestLoc == null) {
                    Motion.spreadRandomly();
                } else {
                    Motion.bugnavTowards(bestLoc);
                    G.rc.setIndicatorLine(G.me, bestLoc, 255, 255, 0);
                }
                MapInfo me = G.rc.senseMapInfo(G.me);
                if (me.getPaint() == PaintType.EMPTY && G.rc.canAttack(G.me)) {
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
                    MapInfo[] infos = G.rc.senseNearbyMapInfos();
                    for (MapInfo info : infos) {
                        int dx = info.getMapLocation().x - ruinLocation.x + 2;
                        int dy = info.getMapLocation().y - ruinLocation.y + 2;
                        if(0 <= dx && dx <= 4 && 0 <= dy && dy <= 4 && !(dx == 2 && dy == 2)){
                            int towerType = predictTowerType(ruinLocation);
                            boolean paint = Robot.towerPatterns[towerType][dx][dy];
                            if((info.getPaint() == PaintType.EMPTY || info.getPaint() == (paint?PaintType.ALLY_PRIMARY:PaintType.ALLY_SECONDARY)) && G.rc.canAttack(info.getMapLocation())) {
                                G.rc.attack(info.getMapLocation(),paint);
                                G.rc.setIndicatorLine(G.me, info.getMapLocation(), 0, 255, 255);
                                break;
                            }
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
                // attack micro moment
                RobotInfo enemy = G.rc.senseRobotAtLocation(towerLocation); 
                if (towerLocation.isWithinDistanceSquared(G.me, enemy.type.actionRadiusSquared)) {
                    G.rc.attack(towerLocation);
                    Motion.bugnavAway(towerLocation);
                } else {
                    Motion.bugnavTowards(towerLocation);
                    G.rc.attack(towerLocation);
                }
                break;
            case RETREAT:
                G.indicatorString.append("RETREAT ");
                Robot.retreat();
                break;
        }
    }

    public static int predictTowerType(MapLocation m){
        return (m.x^m.y)%6/3+1;
    }
}
