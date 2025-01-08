package SPAARK;

import battlecode.common.*;
import java.util.*;

public class Splasher {
    public static final int EXPLORE = 0;
    public static final int ATTACK = 1;
    public static final int RETREAT = 2;
    public static int mode = EXPLORE;

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
                Motion.spreadRandomly();
                MapInfo me = G.rc.senseMapInfo(Motion.currLoc);
                if (me.getPaint() != PaintType.ALLY_PRIMARY && me.getPaint() != PaintType.ALLY_SECONDARY && G.rc.canAttack(Motion.currLoc)) {
                    G.rc.attack(Motion.currLoc); //also add logic to paint in special resource pattern
                }
            case RETREAT:
                G.indicatorString.append("RETREAT ");
                Robot.retreat();
        }
    }
}
