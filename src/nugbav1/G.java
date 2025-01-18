package nugbav1;

import battlecode.common.*;
import java.util.*;

//store global stuff that you dont want to pass around
//and doesnt fit anywhere else
public class G {
    public static RobotController rc;
    public static Random rng;
    public static MapLocation mapCenter;
    public static Team team;
    public static Team opponentTeam;

    public static UnitType[] towerTypes = new UnitType[] {
            UnitType.LEVEL_ONE_DEFENSE_TOWER,
            UnitType.LEVEL_ONE_PAINT_TOWER,
            UnitType.LEVEL_ONE_MONEY_TOWER
    };

    public static UnitType[] allTowerTypes = new UnitType[] {
            UnitType.LEVEL_ONE_DEFENSE_TOWER,
            UnitType.LEVEL_ONE_PAINT_TOWER,
            UnitType.LEVEL_ONE_MONEY_TOWER,
            UnitType.LEVEL_TWO_DEFENSE_TOWER,
            UnitType.LEVEL_TWO_PAINT_TOWER,
            UnitType.LEVEL_TWO_MONEY_TOWER,
            UnitType.LEVEL_THREE_DEFENSE_TOWER,
            UnitType.LEVEL_THREE_PAINT_TOWER,
            UnitType.LEVEL_THREE_MONEY_TOWER
    };

    public static final Direction[] DIRECTIONS = {
            Direction.SOUTHWEST,
            Direction.SOUTH,
            Direction.SOUTHEAST,
            Direction.WEST,
            Direction.EAST,
            Direction.NORTHWEST,
            Direction.NORTH,
            Direction.NORTHEAST,
    };

    public static final Direction[] ALL_DIRECTIONS = {
            Direction.SOUTHWEST,
            Direction.SOUTH,
            Direction.SOUTHEAST,
            Direction.WEST,
            Direction.EAST,
            Direction.NORTHWEST,
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.CENTER,
    };
    public static final MapLocation invalidLoc = new MapLocation(-1, -1);

    // stuff that changes
    public static StringBuilder indicatorString;
    public static MapLocation me;
    public static RobotInfo[] allyRobots;
    public static RobotInfo[] opponentRobots;
    public static MapInfo[] infos;
}