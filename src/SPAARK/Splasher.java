package SPAARK;

import battlecode.common.*;
import java.util.*;

public class Splasher {
    public static final int EXPLORE = 0;
    public static final int ATTACK = 1;
    public static final int RETREAT = 2;
    public static int mode = EXPLORE;

    //every tile in attack range
    public static MapLocation[] attackRange = new MapLocation[] {
        new MapLocation(0, 0),
        new MapLocation(1, 0),
        new MapLocation(0, 1),
        new MapLocation(-1, 0),
        new MapLocation(0, -1),
        new MapLocation(1, 1),
        new MapLocation(1, -1),
        new MapLocation(-1, 1),
        new MapLocation(-1, -1),
        new MapLocation(2, 0),
        new MapLocation(0, 2),
        new MapLocation(-2, 0),
        new MapLocation(0, -2),
        new MapLocation(2, 1),
        new MapLocation(2, -1),
        new MapLocation(-2, 1),
        new MapLocation(-2, -1),
        new MapLocation(1, 2),
        new MapLocation(-1, 2),
        new MapLocation(1, -2),
        new MapLocation(-1, -2),
        new MapLocation(2, 2),
        new MapLocation(2, -2),
        new MapLocation(-2, 2),
        new MapLocation(-2, -2)
    };

    public static void run() throws Exception {
        if (G.rc.getPaint() < G.rc.getType().paintCapacity / 3) {
            mode = RETREAT;
        }
        else {
            mode = EXPLORE;
        }
        switch (mode) {
            case EXPLORE:
                G.indicatorString.append("EXPLORE ");
                MapLocation bestLoc = null;
                int bestScore = 0;
                //painting heuristic
                for (MapLocation d : attackRange) {
                    MapLocation loc = new MapLocation(Motion.currLoc.x+d.x, Motion.currLoc.y+d.y);
                    if (G.rc.canAttack(loc)) {
                        int score = 0;
                        for (Direction dir : G.ALL_DIRECTIONS) {
                            //only care about sqrt(2) distance because bytecode somehow
                            MapLocation nxt = loc.add(dir);
                            if (G.rc.canSenseLocation(nxt)) {
                                PaintType paint = G.rc.senseMapInfo(nxt).getPaint();
                                boolean willPaintEmpty = paint == PaintType.EMPTY;
                                boolean willPaintOpponent = (paint == PaintType.ENEMY_PRIMARY || paint == PaintType.ENEMY_SECONDARY) && loc.distanceSquaredTo(nxt) <= 2;
                                if (willPaintEmpty) score++;
                                if (willPaintOpponent) score += 2; //bonus points for deleting opponent paint
                                if ((willPaintEmpty || willPaintOpponent) && nxt == Motion.currLoc) {
                                    //bonus points for painting self
                                    score++;
                                }
                            }
                        }
                        if (score > bestScore) {
                            bestLoc = loc;
                            bestScore = score;
                        }
                        if (Clock.getBytecodesLeft()<2000) break;
                    }
                }
                if (bestScore > 4 && bestLoc != null) {
                    G.rc.attack(bestLoc, G.rng.nextBoolean());
                }
                Motion.spreadRandomly();
            case RETREAT:
                G.indicatorString.append("RETREAT ");
                Robot.retreat();
        }
    }
}
