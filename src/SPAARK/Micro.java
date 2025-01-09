package SPAARK;

import battlecode.common.*;

@FunctionalInterface
public interface Micro {
    /**
     * Micro function - moves the robot given pathfind results
     * 
     * @param d    Pathfinding direction
     * @param dest Destination location
     * @return Length-8 array of weights for moving in each direction, mapped the
     *         same as G.DIRECTIONS - for chaining micro functions
     * @throws Exception
     */
    public int[] micro(Direction d, MapLocation dest) throws Exception;
}