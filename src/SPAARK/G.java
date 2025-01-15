package SPAARK;

import battlecode.common.*;

//store global stuff that you dont want to pass around
//and doesnt fit anywhere else
public class G {
    public static RobotController rc;
    public static MapLocation mapCenter;
    public static int mapArea;
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

    //all dx/dy within 20 distanceSquared, sorted in ascending distanceSquared
    //Don't change the order it will break mopper code!
    //for everything <=0 radiusSquared use [0, 1)
    //for everything <=1 radiusSquared use [0, 5)
    //for everything <=2 radiusSquared use [0, 9)
    //for everything <=3 radiusSquared use [0, 9)
    //for everything <=4 radiusSquared use [0, 13)
    //for everything <=5 radiusSquared use [0, 21)
    //for everything <=6 radiusSquared use [0, 21)
    //for everything <=7 radiusSquared use [0, 21)
    //for everything <=8 radiusSquared use [0, 25)
    //for everything <=9 radiusSquared use [0, 29)
    //for everything <=10 radiusSquared use [0, 37)
    //for everything <=11 radiusSquared use [0, 37)
    //for everything <=12 radiusSquared use [0, 37)
    //for everything <=13 radiusSquared use [0, 45)
    //for everything <=14 radiusSquared use [0, 45)
    //for everything <=15 radiusSquared use [0, 45)
    //for everything <=16 radiusSquared use [0, 49)
    //for everything <=17 radiusSquared use [0, 57)
    //for everything <=18 radiusSquared use [0, 61)
    //for everything <=19 radiusSquared use [0, 61)
    //for everything <=20 radiusSquared use [0, 69)
    public static final int[] range20X = {
        0, -1, 0, 0, 1, -1, -1, 1, 1, -2, 0, 0, 2, -2, -2, -1, -1, 1, 1, 2, 2, -2, -2, 2, 2, -3, 0, 0, 3, -3, -3, -1, -1, 1, 1, 3, 3, -3, -3, -2, -2, 2, 2, 3, 3, -4, 0, 0, 4, -4, -4, -1, -1, 1, 1, 4, 4, -3, -3, 3, 3, -4, -4, -2, -2, 2, 2, 4, 4
    };
    public static final int[] range20Y = {
        0, 0, -1, 1, 0, -1, 1, -1, 1, 0, -2, 2, 0, -1, 1, -2, 2, -2, 2, -1, 1, -2, 2, -2, 2, 0, -3, 3, 0, -1, 1, -3, 3, -3, 3, -1, 1, -2, 2, -3, 3, -3, 3, -2, 2, 0, -4, 4, 0, -1, 1, -4, 4, -4, 4, -1, 1, -3, 3, -3, 3, -2, 2, -4, 4, -4, 4, -2, 2
    };

    // stuff that changes
    public static StringBuilder indicatorString;
    public static MapLocation me;
    public static RobotInfo[] allyRobots;
    public static RobotInfo[] opponentRobots;
    public static MapInfo[] nearbyMapInfos;
    public static int round;
    // divide all coordinates by 2, now 30x30
    // 1/4th the size of 60x60, don't need the resolution
    public static int[][] lastVisited = new int[30][30];

    public static void setLastVisited(int x, int y, int n) {
        lastVisited[y / 2][x / 2] = n + 2000;
    }

    public static void setLastVisited(MapLocation loc, int n) {
        lastVisited[loc.y / 2][loc.x / 2] = n + 2000;
    }

    public static int getLastVisited(int x, int y) {
        return lastVisited[y / 2][x / 2] - 2000;
    }

    public static int getLastVisited(MapLocation loc) {
        return lastVisited[loc.y / 2][loc.x / 2] - 2000;
    }
}