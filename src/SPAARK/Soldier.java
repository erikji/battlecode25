package SPAARK;

import battlecode.common.*;
import java.util.*;

public class Soldier {
    public static MapLocation ruinLocation = null; //BUILD mode
    public static UnitType towerType = null; //ATTACK mode
    public static MapLocation towerLocation = null; //ATTACK mode
    public static final int EXPLORE = 0;
    public static final int BUILD = 1;
    public static final int ATTACK = 2;
    public static final int RETREAT = 3;
    public static int mode = EXPLORE;

    public static void run() throws Exception {
        if (G.rc.getPaint() < G.rc.getType().paintCapacity / 3) {
            mode = RETREAT;
        } else if (G.rc.getPaint() > G.rc.getType().paintCapacity * 3 / 4 && mode == RETREAT) {
            mode = EXPLORE;
        }
        if (mode == EXPLORE) {
            MapLocation[] locs = G.rc.senseNearbyRuins(-1);
            for (int i = locs.length; --i >= 0; ) {
                if (G.rc.canSenseRobotAtLocation(locs[i])) {
                    RobotInfo bot = G.rc.senseRobotAtLocation(locs[i]);
                    if (bot.team == POI.opponentTeam && bot.type.actionRadiusSquared <= G.rc.getType().actionRadiusSquared) {
                        towerLocation = locs[i];
                        towerType = bot.type;
                        mode = ATTACK;
                        break;
                    }
                } else {
                    ruinLocation = locs[i];
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
                for (int i = 50; --i >= 0; ) {
                    if (POI.towers[i] == -1) {
                        break;
                    }
                    if (POI.parseTowerTeam(POI.towers[i]) != G.rc.getTeam()) {
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
                    MapInfo[] infos = G.rc.senseNearbyMapInfos(UnitType.SOLDIER.actionRadiusSquared);
                    for (int i = infos.length; --i >= 0; ) {
                        if (G.me.isWithinDistanceSquared(infos[i].getMapLocation(), UnitType.SOLDIER.actionRadiusSquared) && infos[i].getMapLocation().isWithinDistanceSquared(ruinLocation, 8)) {
                            int dx = infos[i].getMapLocation().x-ruinLocation.x+2;
                            int dy = infos[i].getMapLocation().y-ruinLocation.y+2;
                            if(dx!=2||dy!=2){
                                boolean paint = Robot.towerPatterns[predictTowerType(ruinLocation)][dx][dy];
                                if(G.rc.canAttack(infos[i].getMapLocation()) && (infos[i].getPaint() == PaintType.EMPTY || infos[i].getPaint() == (paint?PaintType.ALLY_PRIMARY:PaintType.ALLY_SECONDARY))) {
                                    G.rc.attack(infos[i].getMapLocation(),paint);
                                    G.rc.setIndicatorLine(G.me, infos[i].getMapLocation(), 0, 255, 255);
                                    break;
                                }
                            }
                        }
                    }
                    int t = predictTowerType(ruinLocation);
                    if (G.rc.canCompleteTowerPattern(Robot.towers[t], ruinLocation) && G.rc.getPaint() > 50) {
                        G.rc.completeTowerPattern(Robot.towers[t], ruinLocation);
                        POI.addTower(-1,
                                POI.intifyTower(G.rc.getTeam(), Robot.towers[t]) | POI.intifyLocation(ruinLocation));
                        mode = EXPLORE;
                        ruinLocation = null;
                        Motion.bugnavTowards(G.mapCenter);
                    } else {
                        Motion.bugnavAround(ruinLocation, 1, 2);
                    }
                }
                break;
            case ATTACK:
                G.indicatorString.append("ATTACK ");
                // attack micro moment
                if (towerLocation.isWithinDistanceSquared(G.me, towerType.actionRadiusSquared)) {
                    if (G.rc.canAttack(towerLocation))
                        G.rc.attack(towerLocation);
                    Motion.bugnavAway(towerLocation, attackMicro);
                } else {
                    if (G.rc.isActionReady()) {
                        Motion.bugnavTowards(towerLocation, attackMicro);
                        if (G.rc.canAttack(towerLocation))
                            G.rc.attack(towerLocation);
                    } else {
                        Motion.bugnavAround(towerLocation, towerType.actionRadiusSquared+1, towerType.actionRadiusSquared+1);
                    }
                }
                break;
            case RETREAT:
                G.indicatorString.append("RETREAT ");
                Robot.retreat();
                break;
        }
    }

    public static Micro attackMicro = new Micro() {
        public void micro(Direction d, MapLocation dest) throws Exception {
            //try to stay out of range if on cd, otherwise try to get in range
            Direction best = d;
            int bestScore = Integer.MIN_VALUE;
            if (G.rc.isActionReady()) {
                for (int i = 8; --i >= 0; ) {
                    if (!G.rc.canMove(G.DIRECTIONS[i])) continue;
                    int score = 0;
                    MapLocation nxt = G.me.add(G.DIRECTIONS[i]);
                    MapInfo info = G.rc.senseMapInfo(nxt);
                    if (info.getPaint().isEnemy()) score -= 10;
                    else if (info.getPaint() == PaintType.EMPTY) score -= 5;
                    if (G.DIRECTIONS[i] == d) {
                        score += 20;
                    } else if (G.DIRECTIONS[i].rotateLeft() == d || G.DIRECTIONS[i].rotateRight() == d) {
                        score += 16;
                    }
                    if (nxt.isWithinDistanceSquared(towerLocation, G.rc.getType().actionRadiusSquared)) {
                        score += 40;
                    }
                    if (score > bestScore) {
                        best = G.DIRECTIONS[i];
                        bestScore = score;
                    }
                }
            } else {
                for (int i = 8; --i >= 0; ) {
                    if (!G.rc.canMove(G.DIRECTIONS[i])) continue;
                    int score = 0;
                    MapLocation nxt = G.me.add(G.DIRECTIONS[i]);
                    MapInfo info = G.rc.senseMapInfo(nxt);
                    if (info.getPaint().isEnemy()) score -= 10;
                    else if (info.getPaint() == PaintType.EMPTY) score -= 5;
                    if (G.DIRECTIONS[i] == d) {
                        score += 20;
                    } else if (G.DIRECTIONS[i].rotateLeft() == d || G.DIRECTIONS[i].rotateRight() == d) {
                        score += 16;
                    }
                    if (!nxt.isWithinDistanceSquared(towerLocation, towerType.actionRadiusSquared)) {
                        score += 40;
                    }
                    if (score > bestScore) {
                        best = G.DIRECTIONS[i];
                        bestScore = score;
                    }
                }
            }
            Motion.move(best);
        }
    };

    public static int predictTowerType(MapLocation xy){
        Random rng = new Random(xy.x*0x67f176e2+xy.y);
        return rng.nextInt(10)>5?2:1;
    }
}