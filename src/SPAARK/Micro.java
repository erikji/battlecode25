package SPAARK;

import battlecode.common.Direction;
import battlecode.common.MapLocation;

@FunctionalInterface
public interface Micro {
    /**
     * Micro function - returns weights of moving each direction, where highest
     * weight is movement direction. Can be chained using the weights.
     * as a general rule, 5 micro score = 1 paint
     * if none of the weights are above 0, then don't move
     * 
     * @param d    Pathfinding direction
     * @param dest Destination location
     * @return Length-9 array of weights for moving in each direction, mapped the
     *         same as G.ALL_DIRECTIONS
     * @throws Exception
     */


    // Soldier.java

    // ratio of paint necessary to exit retreat mode
    public static final double RETREAT_PAINT_RATIO = 0.85;
    // ratio to reduce retreat requirement by if building tower/srp
    public static final double RETREAT_REDUCED_RATIO = 0.5;
    // exploration weight multiplier
    public static final int EXPLORE_OPP_WEIGHT = 5;
    // controls rounds between visiting ruins
    public static final int VISIT_TIMEOUT = 40;
    // stop building towers if enemy paint interferes too much
    public static final int MAX_TOWER_ENEMY_PAINT = 10;
    public static final int MAX_TOWER_BLOCKED_TIME = 30;
    // max build time, max build time if no moppers/splashers to remove paint
    public static final int MAX_TOWER_TIME = 200;
    public static final int MAX_TOWER_TIME_NO_HELP = 80;
    // don't build SRP early-game, prioritize towers
    public static final int MIN_SRP_ROUND = 40;
    public static final int MIN_SRP_TOWERS = 2; // probably shouldn't be higher
    // controls rounds between repairing/expanding SRP
    public static final int SRP_VISIT_TIMEOUT = 20;
    // balance exploring and building SRPs (don't SRP if near target)
    public static final int SRP_EXPAND_TIMEOUT = 20;
    public static final int SRP_EXP_OVERRIDE_DIST = 36;
    // have at most TOWER_CEIL for the first TOWER_CEIL rounds, if map small
    public static final int TOWER_CEIL = 3;
    public static final int TOWER_CEIL_MAP_AREA = 1600;
    public static final int TOWER_CEIL_ROUND = 75;
    // encourages building SRPs if waiting for chips on large maps initially
    public static final int INITIAL_SRP_ALT_MAP_AREA = 1600;
    public static final int INITIAL_SRP_ALT_TOWER_CAP = 6;
    public static final int INITIAL_SRP_ALT_CHIPS = 300;
    // stop building SRP if enemy paint interferes too much
    public static final int MAX_SRP_ENEMY_PAINT = 2;
    public static final int MAX_SRP_BLOCKED_TIME = 10;
    // max build time
    public static final int MAX_SRP_TIME = 50;
    // don't expand SRP if low on paint, since very slow
    public static final int EXPAND_SRP_MIN_PAINT = 75;

    // Mopper.java
    public static final int BUILD_TIMEOUT = 10;
    
    public int[] micro(Direction d, MapLocation dest) throws Exception;
}