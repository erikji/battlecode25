package SPAARK;

import battlecode.common.*;

import java.util.*;

public class Splasher {
    public static final int EXPLORE = 0;
    public static final int ATTACK = 1;
    public static final int RETREAT = 2;
    public static int mode = EXPLORE;

    // every tile in attack range
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
        } else if (G.rc.getPaint() > G.rc.getType().paintCapacity - 40) {
            mode = EXPLORE;
        }
        switch (mode) {
            case EXPLORE:
                G.indicatorString.append("EXPLORE ");
                MapLocation bestLoc = null;
                int bestScore = 0;
                // painting heuristic
                for (int i = attackRange.length; --i >= 0;) {
                    MapLocation loc = new MapLocation(G.me.x + attackRange[i].x, G.me.y + attackRange[i].y);
                    if (G.rc.canAttack(loc)) {
                        int score = 0;
                        for (int dir = 9; --dir >= 0;) {
                            // only care about sqrt(2) distance because bytecode restrictions
                            MapLocation nxt = loc.add(Motion.ALL_DIRECTIONS[dir]);
                            if (G.rc.canSenseLocation(nxt)) {
                                PaintType paint = G.rc.senseMapInfo(nxt).getPaint();
                                if (paint == PaintType.EMPTY)
                                    score++;
                                if (paint.isEnemy())
                                    score += 2; // bonus points for deleting opponent paint
                                if (!paint.isAlly() && nxt == G.me) {
                                    // bonus points for painting self
                                    score++;
                                }
                            }
                        }
                        if (score > bestScore) {
                            bestLoc = loc;
                            bestScore = score;
                        }
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
