package SPAARK;

import java.util.*;

import battlecode.common.*;

public class Motion {
    protected static RobotController rc;
    protected static StringBuilder indicatorString;

    protected static Random rng;
    protected static final Direction[] DIRECTIONS = {
        Direction.SOUTHWEST,
        Direction.SOUTH,
        Direction.SOUTHEAST,
        Direction.WEST,
        Direction.EAST,
        Direction.NORTHWEST,
        Direction.NORTH,
        Direction.NORTHEAST,
    };
    protected static final Direction[] ALL_DIRECTIONS = {
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
    protected static final String[] DIRABBREV = {
        "C",
        "W",
        "NW",
        "N",
        "NE",
        "E",
        "SE",
        "S",
        "SW",
    };
    protected static final int TOWARDS = 0;
    protected static final int AWAY = 1;
    protected static final int AROUND = 2;
    protected static final int NONE = 0;
    protected static final int CLOCKWISE = 1;
    protected static final int COUNTER_CLOCKWISE = -1;

    protected static final int DEFAULT_RETREAT_HP = 999;

    protected static RobotInfo[] opponentRobots;
    protected static RobotInfo[] friendlyRobots;

    //symmetry detection
    protected static long[] nowall = new long[60];
    protected static long[] wall = new long[60];
    protected static long[] ruin = new long[60];
    protected static long[] noruin = new long[60];
    protected static boolean[] symmetry = new boolean[]{false, false, false};
    //0: horz
    //1: vert
    //2: rot

    protected static MapLocation mapCenter;
    protected static MapLocation currLoc;

    protected static Direction lastDir = Direction.CENTER;
    protected static Direction optimalDir = Direction.CENTER;
    protected static int rotation = NONE;
    protected static int circleDirection = CLOCKWISE;

    protected static Direction lastRandomDir = Direction.CENTER;
    protected static MapLocation lastRandomSpread;

    // common distance stuff
    protected static int getManhattanDistance(MapLocation a, MapLocation b) {
        return Math.abs(a.x-b.x)+Math.abs(a.y-b.y);
    }
    protected static int getChebyshevDistance(MapLocation a, MapLocation b) {
        return Math.max(Math.abs(a.x-b.x), Math.abs(a.y-b.y));
    }

    protected static MapLocation getClosest(MapLocation[] a) throws GameActionException {
        /* Get closest MapLocation to this robot (Euclidean) */
        return getClosest(a, rc.getLocation());
    }
    protected static MapLocation getClosest(MapLocation[] a, MapLocation me) throws GameActionException {
        /* Get closest MapLocation to me (Euclidean) */
        MapLocation closest = a[0];
        int distance = me.distanceSquaredTo(a[0]);
        for (MapLocation loc : a) {
            if (me.distanceSquaredTo(loc) < distance) {
                closest = loc;
                distance = me.distanceSquaredTo(loc);
            }
        }
        return closest;
    }
    protected static MapLocation getClosestPair(MapLocation[] a, MapLocation[] b) throws GameActionException {
        /* Get closest MapLocation to me (Euclidean) */
        MapLocation closest = a[0];
        int distance = b[0].distanceSquaredTo(a[0]);
        for (MapLocation loc : a) {
            for (MapLocation loc2 : b) {
                if (loc2.distanceSquaredTo(loc) < distance) {
                    closest = loc;
                    distance = loc2.distanceSquaredTo(loc);
                }
            }
        }
        return closest;
    }
    protected static RobotInfo getClosestRobot(RobotInfo[] a) throws GameActionException {
        MapLocation me = rc.getLocation();
        RobotInfo closest = null;
        int distance = 0;
        for (RobotInfo loc : a) {
            if (closest == null || me.distanceSquaredTo(loc.getLocation()) < distance) {
                closest = loc;
                distance = me.distanceSquaredTo(loc.getLocation());
            }
        }
        return closest;
    }
    protected static RobotInfo getClosestRobot(RobotInfo[] a, MapLocation b) throws GameActionException {
        RobotInfo closest = null;
        int distance = 0;
        for (RobotInfo loc : a) {
            if (closest == null || b.distanceSquaredTo(loc.getLocation()) < distance) {
                closest = loc;
                distance = b.distanceSquaredTo(loc.getLocation());
            }
        }
        return closest;
    }

    protected static MapLocation getFarthest(MapLocation[] a) throws GameActionException {
        /* Get farthest MapLocation to this robot (Euclidean) */
        return getFarthest(a, rc.getLocation());
    }
    protected static MapLocation getFarthest(MapLocation[] a, MapLocation me) throws GameActionException {
        /* Get farthest MapLocation to me (Euclidean) */
        MapLocation closest = a[0];
        int distance = me.distanceSquaredTo(a[0]);
        for (MapLocation loc : a) {
            if (me.distanceSquaredTo(loc) > distance) {
                closest = loc;
                distance = me.distanceSquaredTo(loc);
            }
        }
        return closest;
    }

    // basic random movement
    protected static void moveRandomly() throws GameActionException {
        if (rc.isMovementReady()) {
            boolean stuck = true;
            for (Direction d : DIRECTIONS) {
                if (rc.canMove(d)) {
                    stuck = false;
                }
            }
            if (stuck) {
                return;
            }
            // move in a random direction but minimize making useless moves back to where you came from
            Direction direction = DIRECTIONS[rng.nextInt(DIRECTIONS.length)];
            if (direction == lastRandomDir.opposite() && rc.canMove(direction.opposite())) {
                direction = direction.opposite();
            }
            if (rc.canMove(direction)) {
                rc.move(direction);
                lastRandomDir = direction;
                updateInfo();
            }
        }
    }
    protected static void spreadRandomly() throws GameActionException {
        boolean stuck = true;
        for (Direction d : DIRECTIONS) {
            if (canMove(d)) {
                stuck = false;
            }
        }
        if (stuck) {
            return;
        }
        if (rc.isMovementReady()) {
            MapLocation me = rc.getLocation();
            MapLocation target = me;
            for (RobotInfo r : friendlyRobots) {
                target = target.add(me.directionTo(r.getLocation()).opposite());
            }
            if (target.equals(me)) {
                // just keep moving in the same direction as before if there's no robots nearby
                if (rc.getRoundNum() % 3 == 0 || lastRandomSpread == null) {
                    moveRandomly(); // occasionally move randomly to avoid getting stuck
                } else if (rng.nextInt(20) == 1) {
                    // don't get stuck in corners
                    lastRandomSpread = me.add(DIRECTIONS[rng.nextInt(DIRECTIONS.length)]);
                    moveRandomly();
                } else {
                    // Direction direction = bug2Helper(me, lastRandomSpread, TOWARDS, 0, 0);
                    Direction direction = me.directionTo(target);
                    if (rc.canMove(direction)) {
                        rc.move(direction);
                        lastRandomSpread = lastRandomSpread.add(direction);
                        lastRandomDir = direction;
                        updateInfo();
                    } else {
                        moveRandomly();
                    }
                }
                lastDir = Direction.CENTER;
                optimalDir = Direction.CENTER;
            } else {
                rc.setIndicatorLine(me, new MapLocation(Math.max(0, Math.min(rc.getMapWidth() - 1, target.x)), Math.max(0, Math.min(rc.getMapHeight() - 1, target.y))), DEFAULT_RETREAT_HP, AWAY, AROUND);
                if (lastDir == me.directionTo(target)) {
                    lastDir = Direction.CENTER;
                }
                Direction direction = bug2Helper(me, target, TOWARDS, 0, 0);
                if (rc.canMove(direction)) {
                    rc.move(direction);
                    lastRandomSpread = target;
                    lastRandomDir = direction;
                    updateInfo();
                }
            }
        }
    }
    
    // bugnav helpers
    protected static StringBuilder visitedList = new StringBuilder();
    protected static int[] simulateMovement(MapLocation me, MapLocation dest) throws GameActionException {
        MapLocation clockwiseLoc = rc.getLocation();
        Direction clockwiseLastDir = lastDir;
        int clockwiseStuck = 0;
        MapLocation counterClockwiseLoc = rc.getLocation();
        Direction counterClockwiseLastDir = lastDir;
        int counterClockwiseStuck = 0;
        search: for (int t = 0; t < 10; t++) {
            if (clockwiseLoc.equals(dest)) {
                break;
            }
            if (counterClockwiseLoc.equals(dest)) {
                break;
            }
            Direction clockwiseDir = clockwiseLoc.directionTo(dest);
            {
                for (int i = 9; --i >= 0;) {
                    MapLocation loc = clockwiseLoc.add(clockwiseDir);
                    if (rc.onTheMap(loc)) {
                        if (!rc.canSenseLocation(loc)) {
                            break search;
                        }
                        if (clockwiseDir != clockwiseLastDir.opposite() && rc.senseMapInfo(loc).isPassable() && rc.senseRobotAtLocation(loc) == null) {
                            clockwiseLastDir = clockwiseDir;
                            break;
                        }
                    }
                    clockwiseDir = clockwiseDir.rotateRight();
                    if (i == 7) {
                        clockwiseStuck = 1;
                        break search;
                    }
                }
            }
            Direction counterClockwiseDir = counterClockwiseLoc.directionTo(dest);
            {
                for (int i = 9; --i >= 0;) {
                    MapLocation loc = counterClockwiseLoc.add(counterClockwiseDir);
                    if (rc.onTheMap(loc)) {
                        if (!rc.canSenseLocation(loc)) {
                            break search;
                        }
                        if (counterClockwiseDir != counterClockwiseLastDir.opposite() && rc.senseMapInfo(loc).isPassable() && rc.senseRobotAtLocation(loc) == null) {
                            counterClockwiseLastDir = counterClockwiseDir;
                            break;
                        }
                    }
                    counterClockwiseDir = counterClockwiseDir.rotateLeft();
                    if (i == 7) {
                        counterClockwiseStuck = 1;
                        break search;
                    }
                }
            }
            clockwiseLoc = clockwiseLoc.add(clockwiseDir);
            counterClockwiseLoc = counterClockwiseLoc.add(counterClockwiseDir);
        }

        int clockwiseDist = clockwiseLoc.distanceSquaredTo(dest);
        int counterClockwiseDist = counterClockwiseLoc.distanceSquaredTo(dest);

        return new int[]{clockwiseDist, clockwiseStuck, counterClockwiseDist, counterClockwiseStuck};
    }
    protected static Direction bug2Helper(MapLocation me, MapLocation dest, int mode, int minRadiusSquared, int maxRadiusSquared) throws GameActionException {
        Direction direction = me.directionTo(dest);
        if (me.equals(dest)) {
            if (mode == AROUND) {
                direction = Direction.EAST;
            }
            else {
                return Direction.CENTER;
            }
        }
        if (mode == AWAY) {
            direction = direction.opposite();
        }
        else if (mode == AROUND) {
            if (me.distanceSquaredTo(dest) < minRadiusSquared) {
                direction = direction.opposite();
            }
            else if (me.distanceSquaredTo(dest) <= maxRadiusSquared) {
                direction = direction.rotateLeft().rotateLeft();
                if (circleDirection == COUNTER_CLOCKWISE) {
                    direction = direction.opposite();
                }
            }
            lastDir = Direction.CENTER;
        }

        boolean stuck = true;
        for (int i = 4; --i >= 0;) {
            if (!visitedList.toString().contains(me + " " + i + " ")) {
                visitedList.append(me + " " + i + " ");
                stuck = false;
                break;
            }
        }
        if (stuck) {
            moveRandomly();
            visitedList = new StringBuilder();
            return Direction.CENTER;
        }

        indicatorString.append("DIR=" + direction + " ");
        if (optimalDir != Direction.CENTER && mode != AROUND) {
            if (rc.canMove(optimalDir) && lastDir != optimalDir.opposite()) {
                optimalDir = Direction.CENTER;
                rotation = NONE;
                visitedList = new StringBuilder();
            }
            else {
                direction = optimalDir;
            }
        }
        indicatorString.append("OPTIMAL=" + optimalDir + " ");

        // indicatorString.append("CIRCLE: " + circleDirection);
        // indicatorString.append("DIR: " + direction);
        // indicatorString.append("OFF: " + rc.onTheMap(me.add(direction)));
        
        if (lastDir != direction.opposite()) {
            if (rc.canMove(direction)) {
                // if (!lastBlocked) {
                //     rotation = NONE;
                // }
                // lastBlocked = false;
                // boolean touchingTheWallBefore = false;
                // for (Direction d : DIRECTIONS) {
                //     MapLocation translatedMapLocation = me.add(d);
                //     if (rc.onTheMap(translatedMapLocation)) {
                //         if (!rc.senseMapInfo(translatedMapLocation).isPassable()) {
                //             touchingTheWallBefore = true;
                //             break;
                //         }
                //     }
                // }
                // if (touchingTheWallBefore) {
                //     rotation = NONE;
                // }
                return direction;
            }
        }
        else if (rc.canMove(direction)) {
            Direction dir;
            if (rotation == CLOCKWISE) {
                dir = direction.rotateRight();
            }
            else {
                dir = direction.rotateLeft();
            }
            if (!rc.onTheMap(me.add(dir))) {
                // boolean touchingTheWallBefore = false;
                // for (Direction d : DIRECTIONS) {
                //     MapLocation translatedMapLocation = me.add(d);
                //     if (rc.onTheMap(translatedMapLocation)) {
                //         if (!rc.senseMapInfo(translatedMapLocation).isPassable()) {
                //             touchingTheWallBefore = true;
                //             break;
                //         }
                //     }
                // }
                // if (touchingTheWallBefore) {
                //     rotation = NONE;
                // }
                rotation *= -1;
                return direction;
            }
        }
        if (!rc.onTheMap(me.add(direction))) {
            if (mode == AROUND) {
                circleDirection *= -1;
                direction = direction.opposite();
                indicatorString.append("FLIPPED");
            }
            else {
                direction = me.directionTo(dest);
            }
            if (rc.canMove(direction)) {
                return direction;
            }
        }

        if (optimalDir == Direction.CENTER) {
            optimalDir = direction;
        }
        
        indicatorString.append("ROTATION=" + rotation + " ");
        if (rotation == NONE) {
            int[] simulated = simulateMovement(me, dest);
    
            int clockwiseDist = simulated[0];
            int counterClockwiseDist = simulated[2];
            boolean clockwiseStuck = simulated[1] == 1;
            boolean counterClockwiseStuck = simulated[3] == 1;
            
            indicatorString.append("DIST=" + clockwiseDist + " " + counterClockwiseDist);
            int tempMode = mode;
            if (mode == AROUND) {
                if (clockwiseDist < minRadiusSquared) {
                    if (counterClockwiseDist < minRadiusSquared) {
                        tempMode = AWAY;
                    }
                    else {
                        tempMode = AWAY;
                    }
                }
                else {
                    if (counterClockwiseDist < minRadiusSquared) {
                        tempMode = AWAY;
                    }
                    else {
                        tempMode = TOWARDS;
                    }
                }
            }
            if (clockwiseStuck) {
                rotation = COUNTER_CLOCKWISE;
            }
            else if (counterClockwiseStuck) {
                rotation = CLOCKWISE;
            }
            else if (tempMode == TOWARDS) {
                if (clockwiseDist < counterClockwiseDist) {
                    rotation = CLOCKWISE;
                }
                else {
                    rotation = COUNTER_CLOCKWISE;
                }
            }
            else if (tempMode == AWAY) {
                if (clockwiseDist < counterClockwiseDist) {
                    rotation = COUNTER_CLOCKWISE;
                }
                else {
                    rotation = CLOCKWISE;
                }
            }
        }

        boolean flip = false;
        for (int i = 8; --i >= 0;) {
            if (rotation == CLOCKWISE) {
                direction = direction.rotateRight();
            }
            else {
                direction = direction.rotateLeft();
            }
            if (!rc.onTheMap(me.add(direction))) {
                flip = true;
            }
            // if (rc.onTheMap(me.add(direction)) && rc.senseMapInfo(me.add(direction)).isPassable() && lastDir != direction.opposite()) {
            //     if (rc.canMove(direction)) {
            //         return direction;
            //     }
            //     return Direction.CENTER;
            // }
            if (rc.canMove(direction) && lastDir != direction.opposite()) {
                if (flip) {
                    rotation *= -1;
                }
                if (rc.canMove(direction)) {
                    return direction;
                }
                return Direction.CENTER;
            }
        }
        if (flip) {
            rotation *= -1;
        }
        if (rc.canMove(lastDir.opposite())) {
            return lastDir.opposite();
        }
        return Direction.CENTER;
    }
    
    // bugnav
    protected static void bugnavTowards(MapLocation dest) throws GameActionException {
        if (rc.isMovementReady()) {
            Direction d = bug2Helper(rc.getLocation(), dest, TOWARDS, 0, 0);
            if (d == Direction.CENTER) {
                d = rc.getLocation().directionTo(dest);
            }
            Micro.micro(d, dest);
        }
    }
    protected static void bugnavAway(MapLocation dest) throws GameActionException {
        if (rc.isMovementReady()) {
            Direction d = bug2Helper(rc.getLocation(), dest, AWAY, 0, 0);
            if (d == Direction.CENTER) {
                d = rc.getLocation().directionTo(dest);
            }
            Micro.micro(d, dest);
        }
    }
    protected static void bugnavAround(MapLocation dest, int minRadiusSquared, int maxRadiusSquared) throws GameActionException {
        if (rc.isMovementReady()) {
            Direction d = bug2Helper(rc.getLocation(), dest, AROUND, minRadiusSquared, maxRadiusSquared);
            if (d == Direction.CENTER) {
                d = rc.getLocation().directionTo(dest);
            }
            Micro.micro(d, dest);
        }
    }

    protected static MapLocation bfsDest;
    protected static long[] bfsMap;
    protected static long[] bfsDist;
    protected static long[] bfsCurr;
    protected static long bitmask;
    // protected static StringBuilder bfsQueue = new StringBuilder();
    protected static final int MAX_PATH_LENGTH = 100;
    protected static void bfsInit() {
        width = rc.getMapWidth();
        height = rc.getMapHeight();
        bfsMap = new long[height + 2];
        bfsCurr = new long[height + 2];
        bfsDist = new long[(height + 2) * MAX_PATH_LENGTH];
        bitmask = (long1 << width) - 1;
    }
    protected static int step = 1;
    protected static int stepOffset;
    protected static int width;
    protected static int height;
    protected static long long1 = 1;
    protected static int recalculationNeeded = MAX_PATH_LENGTH;
    protected static void updateBfsMap() throws GameActionException {
        MapInfo[] map = rc.senseNearbyMapInfos();
        for (MapInfo m : map) {
            if (m.isWall()) {
                int loc = m.getMapLocation().y + 1;
                int subloc = m.getMapLocation().x;
                if (((bfsMap[loc] >> subloc) & 1) == 0) {
                    bfsMap[loc] |= (long1 << subloc);
                    rc.setIndicatorDot(m.getMapLocation(), 255, 255, 255);
                    for (int i = step - 1; i >= 0; i--) {
                        if (((bfsDist[i * (height + 2) + loc] >> subloc) & 1) != 1) {
                            recalculationNeeded = Math.min(i, recalculationNeeded);
                            break;
                        }
                    }
                }
            }
        }
    }
    protected static void bfs() throws GameActionException {

        if (recalculationNeeded != MAX_PATH_LENGTH && recalculationNeeded < step) { 
            step = recalculationNeeded;
            for (int i = 1; i <= height; i++) {
                // bfsDist[i] = 0;
                // bfsCurr[i] = 0;
                bfsCurr[i] = bfsDist[step * (height + 2) + i];
            }
            step += 1;
            indicatorString.append("RECALCULATING;");
        }
        recalculationNeeded = MAX_PATH_LENGTH;
        
        while (step < MAX_PATH_LENGTH && Clock.getBytecodesLeft() > 5000) {
            stepOffset = step * (height + 2);
            switch (height) {
                case 20:
                MotionCodeGen.bfs20();
                break;
                case 21:
                MotionCodeGen.bfs21();
                break;
                case 22:
                MotionCodeGen.bfs22();
                break;
                case 23:
                MotionCodeGen.bfs23();
                break;
                case 24:
                MotionCodeGen.bfs24();
                break;
                case 25:
                MotionCodeGen.bfs25();
                break;
                case 26:
                MotionCodeGen.bfs26();
                break;
                case 27:
                MotionCodeGen.bfs27();
                break;
                case 28:
                MotionCodeGen.bfs28();
                break;
                case 29:
                MotionCodeGen.bfs29();
                break;
                case 30:
                MotionCodeGen.bfs30();
                break;
                case 31:
                MotionCodeGen.bfs31();
                break;
                case 32:
                MotionCodeGen.bfs32();
                break;
                case 33:
                MotionCodeGen.bfs33();
                break;
                case 34:
                MotionCodeGen.bfs34();
                break;
                case 35:
                MotionCodeGen.bfs35();
                break;
                case 36:
                MotionCodeGen.bfs36();
                break;
                case 37:
                MotionCodeGen.bfs37();
                break;
                case 38:
                MotionCodeGen.bfs38();
                break;
                case 39:
                MotionCodeGen.bfs39();
                break;
                case 40:
                MotionCodeGen.bfs40();
                break;
                case 41:
                MotionCodeGen.bfs41();
                break;
                case 42:
                MotionCodeGen.bfs42();
                break;
                case 43:
                MotionCodeGen.bfs43();
                break;
                case 44:
                MotionCodeGen.bfs44();
                break;
                case 45:
                MotionCodeGen.bfs45();
                break;
                case 46:
                MotionCodeGen.bfs46();
                break;
                case 47:
                MotionCodeGen.bfs47();
                break;
                case 48:
                MotionCodeGen.bfs48();
                break;
                case 49:
                MotionCodeGen.bfs49();
                break;
                case 50:
                MotionCodeGen.bfs50();
                break;
                case 51:
                MotionCodeGen.bfs51();
                break;
                case 52:
                MotionCodeGen.bfs52();
                break;
                case 53:
                MotionCodeGen.bfs53();
                break;
                case 54:
                MotionCodeGen.bfs54();
                break;
                case 55:
                MotionCodeGen.bfs55();
                break;
                case 56:
                MotionCodeGen.bfs56();
                break;
                case 57:
                MotionCodeGen.bfs57();
                break;
                case 58:
                MotionCodeGen.bfs58();
                break;
                case 59:
                MotionCodeGen.bfs59();
                break;
                case 60:
                MotionCodeGen.bfs60();
                break;
            }
            // var cod = "";
            // for (var i = 30; i <= 60; i++) {
            //     cod += "public static void bfs" + i + "() {\n";
            //     for (var j = 1; j <= i; j++) {
            //         cod += "Motion.bfsCurr[z] = Motion.bfsCurr[z] | (Motion.bfsCurr[z] >> 1) | (Motion.bfsCurr[z] << 1);\n".replaceAll("z", j);
            //     }
            //     for (var j = 1; j <= i; j++) {
            //         cod += "Motion.bfsDist[Motion.stepOffset + z] = (Motion.bfsCurr[z] | Motion.bfsCurr[y] | Motion.bfsCurr[x]) & (Motion.bitmask ^ Motion.bfsMap[z]);\n".replaceAll("z", j).replaceAll("y", j - 1).replaceAll("x", j + 1);
            //     }
            //     for (var j = 1; j <= i; j++) {
            //         //cod += "Motion.bfsDist[Motion.stepOffset + z] &= Motion.bitmask ^ Motion.bfsMap[z];\n".replaceAll("z", j);
            //     }
            //     for (var j = 1; j <= i; j++) {
            //         cod += "Motion.bfsCurr[z] = Motion.bfsDist[Motion.stepOffset + z];\n".replaceAll("z", j);
            //     }
            //     cod += "}\n";
            // }
            // console.log(cod);
            step += 1;
        }

        // int b = rc.getRoundNum() % width;
        // if (rc.getRoundNum() == 201) {
            // for (int i = 0; i < width; i++) {
            //     b = i;
            //     for (int j = 0; j < height; j++) {
            //         // if (((bfsDist[(rc.getRoundNum() % 100) * (height + 2) + j + 1] >> i) & 1) == 0) {
            //         if (((bfsDist[(rc.getRoundNum() % 100) * (height + 2) + j + 1] >> b) & 1) == 0) {
            //             if (((bfsMap[j + 1] >> b) & 1) == 0) {
            //                 rc.setIndicatorDot(new MapLocation(b, j), 255, 0, 0);
            //             }
            //             else {
            //                 rc.setIndicatorDot(new MapLocation(b, j), 0, 0, 0);
            //             }
            //         }
            //         else {
            //             if (((bfsMap[j + 1] >> b) & 1) == 0) {
            //                 rc.setIndicatorDot(new MapLocation(b, j), 255, 255, 255);
            //             }
            //             else {
            //                 rc.setIndicatorDot(new MapLocation(b, j), 0, 255, 0);
            //             }
            //         }
            //     }
            // }
        // }
        indicatorString.append("STEP=" + step);
    }
    protected static Direction getBfsDirection(MapLocation dest) throws GameActionException {
        MapLocation me = rc.getLocation();

        boolean[] directions = new boolean[9];
        for (int i = 1; i < step; i++) {
            if (((bfsDist[i * (height + 2) + 1 + me.y] >> me.x) & 1) == 1) {
                if (((bfsDist[(i - 1) * (height + 2) + 1 + me.y - 1] >> me.x) & 1) == 1) {
                    directions[7] = true;
                }
                if (((bfsDist[(i - 1) * (height + 2) + 1 + me.y + 1] >> me.x) & 1) == 1) {
                    directions[3] = true;
                }
                if (me.x > 0) {
                    if (((bfsDist[(i - 1) * (height + 2) + 1 + me.y] >> (me.x - 1)) & 1) == 1) {
                        directions[1] = true;
                    }
                    if (((bfsDist[(i - 1) * (height + 2) + 1 + me.y - 1] >> (me.x - 1)) & 1) == 1) {
                        directions[8] = true;
                    }
                    if (((bfsDist[(i - 1) * (height + 2) + 1 + me.y + 1] >> (me.x - 1)) & 1) == 1) {
                        directions[2] = true;
                    }
                }
                if (me.x < width - 1) {
                    if (((bfsDist[(i - 1) * (height + 2) + 1 + me.y] >> (me.x + 1)) & 1) == 1) {
                        directions[5] = true;
                    }
                    if (((bfsDist[(i - 1) * (height + 2) + 1 + me.y - 1] >> (me.x + 1)) & 1) == 1) {
                        directions[6] = true;
                    }
                    if (((bfsDist[(i - 1) * (height + 2) + 1 + me.y + 1] >> (me.x + 1)) & 1) == 1) {
                        directions[4] = true;
                    }
                }
                break;
            }
        }
        Direction optimalDirection = Direction.CENTER;
        int minDist = Integer.MAX_VALUE;
        // int optimalIndex = 0;
        for (int i = 9; --i >= 0;) {
            if (directions[i]) {
                Direction dir = Direction.DIRECTION_ORDER[i];
                if (rc.canMove(dir)) {
                    if (me.add(dir).distanceSquaredTo(dest) < minDist) {
                        optimalDirection = dir;
                        minDist = me.add(dir).distanceSquaredTo(dest);
                    }
                }
            }
        }
        if (optimalDirection != Direction.CENTER) {
            return optimalDirection;
        }
        if (optimalDirection == Direction.CENTER) {
            optimalDirection = bug2Helper(me, dest, TOWARDS, 0, 0);
            indicatorString.append("BUGNAV");

            if (canMove(optimalDirection)) {
                return optimalDirection;
            }
        }
        if (canMove(optimalDirection)) {
            return optimalDirection;
        }
        return Direction.CENTER;
    }

    protected static void bfsnav(MapLocation dest) throws GameActionException {
        indicatorString.append(Clock.getBytecodesLeft() + " ");
        updateBfsTarget(dest);

        if (!rc.getLocation().equals(dest) && rc.isMovementReady()) {
            Direction d = getBfsDirection(dest);
            if (d == Direction.CENTER) {
                d = rc.getLocation().directionTo(dest);
            }
            Micro.micro(d, dest);
        }
        else {
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // Atk.attack();
            // Atk.heal();
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
            // ADD THIS BACK
        }
        bfs();
        indicatorString.append(Clock.getBytecodesLeft() + " ");
    }
    protected static void updateBfsTarget(MapLocation dest) throws GameActionException {
        if (!dest.equals(bfsDest)) {
            bfsDest = dest;
            for (int i = 1; i <= height; i++) {
                bfsDist[i] = 0;
                bfsCurr[i] = 0;
            }
            bfsDist[dest.y + 1] = long1 << (dest.x);
            bfsCurr[dest.y + 1] = long1 << (dest.x);
            step = 1;
        }
    }

    protected static void move(Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            lastDir = dir;
            updateInfo();
        }
    }
    protected static boolean canMove(Direction dir) throws GameActionException {
        return rc.canMove(dir);
    }
    protected static void updateInfo() throws GameActionException {
        opponentRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        friendlyRobots = rc.senseNearbyRobots(-1, rc.getTeam());

        // bytecode inefficient symmetry detection
        MapInfo[] infos = rc.senseNearbyMapInfos();
        for (MapInfo info : infos) {
            MapLocation xy = info.getMapLocation();
            if (info.isWall()) {
                wall[xy.y] |= 1L << xy.x;
                rc.setIndicatorDot(xy, 255, 0, 0);
            }
            else {
                nowall[xy.y] |= 1L << xy.x;
            }
            if (info.hasRuin()) {
                ruin[xy.y] |= 1L << xy.x;
                rc.setIndicatorDot(xy, 0, 0, 255);
            }
            else {
                noruin[xy.y] |= 1L << xy.x;
            }
        }
        if (symmetry[0]&&!symmetryValid(0))symmetry[0]=false;
        if (symmetry[1]&&!symmetryValid(1))symmetry[1]=false;
        if (symmetry[2]&&!symmetryValid(2))symmetry[2]=false;
    }

    protected static boolean symmetryValid(int sym) throws GameActionException {
        //completely untested...
        int w=rc.getMapWidth();
        int h=rc.getMapHeight();
        switch (sym) {
            case 0: //horz
                for (int i = 0; i < h/2; i++) {
                    if ((nowall[i] ^ nowall[h-i]) != 0) return false;
                    if ((wall[i] ^ wall[h-i]) != 0) return false;
                    if ((noruin[i] ^ noruin[h-i]) != 0) return false;
                    if ((ruin[i] ^ ruin[h-i]) != 0) return false;
                }
                return true;
            case 1: //vert
                for (int i = 0; i < h; i++) {
                    if ((Long.reverse(nowall[i]) << (64 - w)) != nowall[i]) return false;
                    if ((Long.reverse(wall[i]) << (64 - w)) != wall[i]) return false;
                    if ((Long.reverse(noruin[i]) << (64 - w)) != noruin[i]) return false;
                    if ((Long.reverse(ruin[i]) << (64 - w)) != ruin[i]) return false;
                }
                return true;
            case 2: //rot
                for (int i = 0; i < h/2; i++) {
                    if (((Long.reverse(nowall[i]) << (64 - w)) ^ nowall[h-i]) != 0) return false;
                    if (((Long.reverse(wall[i]) << (64 - w)) ^ wall[h-i]) != 0) return false;
                    if (((Long.reverse(noruin[i]) << (64 - w)) ^ noruin[h-i]) != 0) return false;
                    if (((Long.reverse(ruin[i]) << (64 - w)) ^ ruin[h-i]) != 0) return false;
                }
                return true;
        }
        System.out.println("invalid symmetry argument");
        return false;
    }
}