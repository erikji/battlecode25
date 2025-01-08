package SPAARK;

import battlecode.common.*;

public class Motion {
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
    public static final String[] DIRABBREV = {
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
    public static final int TOWARDS = 0;
    public static final int AWAY = 1;
    public static final int AROUND = 2;
    public static final int NONE = 0;
    public static final int CLOCKWISE = 1;
    public static final int COUNTER_CLOCKWISE = -1;

    public static final int DEFAULT_RETREAT_HP = 999;

    public static RobotInfo[] opponentRobots;
    public static RobotInfo[] allyRobots;

    public static MapLocation mapCenter;

    public static Direction lastDir = Direction.CENTER;
    public static Direction optimalDir = Direction.CENTER;
    public static int rotation = NONE;
    public static int circleDirection = CLOCKWISE;

    public static Direction lastRandomDir = Direction.CENTER;
    public static MapLocation lastRandomSpread;

    // common distance stuff
    public static int getManhattanDistance(MapLocation a, MapLocation b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    public static int getChebyshevDistance(MapLocation a, MapLocation b) {
        return Math.max(Math.abs(a.x - b.x), Math.abs(a.y - b.y));
    }

    // basic random movement
    public static void moveRandomly() throws GameActionException {
        if (G.rc.isMovementReady()) {
            boolean stuck = true;
            for (int i = DIRECTIONS.length; --i >= 0;) {
                if (G.rc.canMove(DIRECTIONS[i])) {
                    stuck = false;
                }
            }
            if (stuck) {
                return;
            }
            // move in a random direction but minimize making useless moves back to where
            // you came from
            Direction direction = DIRECTIONS[G.rng.nextInt(DIRECTIONS.length)];
            if (direction == lastRandomDir.opposite() && G.rc.canMove(direction.opposite())) {
                direction = direction.opposite();
            }
            if (G.rc.canMove(direction)) {
                G.rc.move(direction);
                lastRandomDir = direction;
                updateInfo();
            }
        }
    }

    public static void spreadRandomly() throws GameActionException {
        boolean stuck = true;
        for (int i = DIRECTIONS.length; --i >= 0;) {
            if (canMove(DIRECTIONS[i])) {
                stuck = false;
            }
        }
        if (stuck) {
            return;
        }
        if (G.rc.isMovementReady()) {
            MapLocation me = G.rc.getLocation();
            MapLocation target = me;
            for (int i = allyRobots.length; --i >= 0;) {
                MapLocation loc = allyRobots[i].getLocation();
                if (!G.rc.senseMapInfo(loc).hasRuin())
                    // ignore towers
                    target = target.add(me.directionTo(loc).opposite());
            }
            if (target.equals(me)) {
                // just keep moving in the same direction as before if there's no robots nearby
                if (G.rc.getRoundNum() % 3 == 0 || lastRandomSpread == null) {
                    moveRandomly(); // occasionally move randomly to avoid getting stuck
                } else if (G.rng.nextInt(20) == 1) {
                    // don't get stuck in corners
                    lastRandomSpread = me.add(DIRECTIONS[G.rng.nextInt(DIRECTIONS.length)]);
                    moveRandomly();
                } else {
                    // Direction direction = bug2Helper(me, lastRandomSpread, TOWARDS, 0, 0);
                    Direction direction = me.directionTo(target);
                    if (G.rc.canMove(direction)) {
                        G.rc.move(direction);
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
                G.rc.setIndicatorLine(me,
                        new MapLocation(Math.max(0, Math.min(G.rc.getMapWidth() - 1, target.x)),
                                Math.max(0, Math.min(G.rc.getMapHeight() - 1, target.y))),
                        DEFAULT_RETREAT_HP, AWAY, AROUND);
                if (lastDir == me.directionTo(target)) {
                    lastDir = Direction.CENTER;
                }
                Direction direction = bug2Helper(me, target, TOWARDS, 0, 0);
                if (G.rc.canMove(direction)) {
                    G.rc.move(direction);
                    lastRandomSpread = target;
                    lastRandomDir = direction;
                    updateInfo();
                }
            }
        }
    }

    // bugnav helpers
    public static StringBuilder visitedList = new StringBuilder();

    public static int[] simulateMovement(MapLocation me, MapLocation dest) throws GameActionException {
        MapLocation clockwiseLoc = G.rc.getLocation();
        Direction clockwiseLastDir = lastDir;
        int clockwiseStuck = 0;
        MapLocation counterClockwiseLoc = G.rc.getLocation();
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
                    if (G.rc.onTheMap(loc)) {
                        if (!G.rc.canSenseLocation(loc)) {
                            break search;
                        }
                        if (clockwiseDir != clockwiseLastDir.opposite() && G.rc.senseMapInfo(loc).isPassable()
                                && G.rc.senseRobotAtLocation(loc) == null) {
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
                    if (G.rc.onTheMap(loc)) {
                        if (!G.rc.canSenseLocation(loc)) {
                            break search;
                        }
                        if (counterClockwiseDir != counterClockwiseLastDir.opposite()
                                && G.rc.senseMapInfo(loc).isPassable() && G.rc.senseRobotAtLocation(loc) == null) {
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

        return new int[] { clockwiseDist, clockwiseStuck, counterClockwiseDist, counterClockwiseStuck };
    }

    public static Direction bug2Helper(MapLocation me, MapLocation dest, int mode, int minRadiusSquared,
            int maxRadiusSquared) throws GameActionException {
        Direction direction = me.directionTo(dest);
        if (me.equals(dest)) {
            if (mode == AROUND) {
                direction = Direction.EAST;
            } else {
                return Direction.CENTER;
            }
        }
        if (mode == AWAY) {
            direction = direction.opposite();
        } else if (mode == AROUND) {
            if (me.distanceSquaredTo(dest) < minRadiusSquared) {
                direction = direction.opposite();
            } else if (me.distanceSquaredTo(dest) <= maxRadiusSquared) {
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

        G.indicatorString.append("DIR=" + direction + " ");
        if (optimalDir != Direction.CENTER && mode != AROUND) {
            if (G.rc.canMove(optimalDir) && lastDir != optimalDir.opposite()) {
                optimalDir = Direction.CENTER;
                rotation = NONE;
                visitedList = new StringBuilder();
            } else {
                direction = optimalDir;
            }
        }
        G.indicatorString.append("OPTIMAL=" + optimalDir + " ");

        // G.indicatorString.append("CIRCLE: " + circleDirection);
        // G.indicatorString.append("DIR: " + direction);
        // G.indicatorString.append("OFF: " + G.rc.onTheMap(me.add(direction)));

        if (lastDir != direction.opposite()) {
            if (G.rc.canMove(direction)) {
                // if (!lastBlocked) {
                // rotation = NONE;
                // }
                // lastBlocked = false;
                // boolean touchingTheWallBefore = false;
                // for (int i = DIRECTIONS.length; --i>=0;) {
                // MapLocation translatedMapLocation = me.add(d);
                // if (G.rc.onTheMap(translatedMapLocation)) {
                // if (!G.rc.senseMapInfo(translatedMapLocation).isPassable()) {
                // touchingTheWallBefore = true;
                // break;
                // }
                // }
                // }
                // if (touchingTheWallBefore) {
                // rotation = NONE;
                // }
                return direction;
            }
        } else if (G.rc.canMove(direction)) {
            Direction dir;
            if (rotation == CLOCKWISE) {
                dir = direction.rotateRight();
            } else {
                dir = direction.rotateLeft();
            }
            if (!G.rc.onTheMap(me.add(dir))) {
                // boolean touchingTheWallBefore = false;
                // for (int i = DIRECTIONS.length; --i>=0;) {
                // MapLocation translatedMapLocation = me.add(d);
                // if (G.rc.onTheMap(translatedMapLocation)) {
                // if (!G.rc.senseMapInfo(translatedMapLocation).isPassable()) {
                // touchingTheWallBefore = true;
                // break;
                // }
                // }
                // }
                // if (touchingTheWallBefore) {
                // rotation = NONE;
                // }
                rotation *= -1;
                return direction;
            }
        }
        if (!G.rc.onTheMap(me.add(direction))) {
            if (mode == AROUND) {
                circleDirection *= -1;
                direction = direction.opposite();
                G.indicatorString.append("FLIPPED");
            } else {
                direction = me.directionTo(dest);
            }
            if (G.rc.canMove(direction)) {
                return direction;
            }
        }

        if (optimalDir == Direction.CENTER) {
            optimalDir = direction;
        }

        G.indicatorString.append("ROTATION=" + rotation + " ");
        if (rotation == NONE) {
            int[] simulated = simulateMovement(me, dest);

            int clockwiseDist = simulated[0];
            int counterClockwiseDist = simulated[2];
            boolean clockwiseStuck = simulated[1] == 1;
            boolean counterClockwiseStuck = simulated[3] == 1;

            G.indicatorString.append("DIST=" + clockwiseDist + " " + counterClockwiseDist);
            int tempMode = mode;
            if (mode == AROUND) {
                if (clockwiseDist < minRadiusSquared) {
                    if (counterClockwiseDist < minRadiusSquared) {
                        tempMode = AWAY;
                    } else {
                        tempMode = AWAY;
                    }
                } else {
                    if (counterClockwiseDist < minRadiusSquared) {
                        tempMode = AWAY;
                    } else {
                        tempMode = TOWARDS;
                    }
                }
            }
            if (clockwiseStuck) {
                rotation = COUNTER_CLOCKWISE;
            } else if (counterClockwiseStuck) {
                rotation = CLOCKWISE;
            } else if (tempMode == TOWARDS) {
                if (clockwiseDist < counterClockwiseDist) {
                    rotation = CLOCKWISE;
                } else {
                    rotation = COUNTER_CLOCKWISE;
                }
            } else if (tempMode == AWAY) {
                if (clockwiseDist < counterClockwiseDist) {
                    rotation = COUNTER_CLOCKWISE;
                } else {
                    rotation = CLOCKWISE;
                }
            }
        }

        boolean flip = false;
        for (int i = 8; --i >= 0;) {
            if (rotation == CLOCKWISE) {
                direction = direction.rotateRight();
            } else {
                direction = direction.rotateLeft();
            }
            if (!G.rc.onTheMap(me.add(direction))) {
                flip = true;
            }
            // if (G.rc.onTheMap(me.add(direction)) &&
            // G.rc.senseMapInfo(me.add(direction)).isPassable() && lastDir !=
            // direction.opposite()) {
            // if (G.rc.canMove(direction)) {
            // return direction;
            // }
            // return Direction.CENTER;
            // }
            if (G.rc.canMove(direction) && lastDir != direction.opposite()) {
                if (flip) {
                    rotation *= -1;
                }
                if (G.rc.canMove(direction)) {
                    return direction;
                }
                return Direction.CENTER;
            }
        }
        if (flip) {
            rotation *= -1;
        }
        if (G.rc.canMove(lastDir.opposite())) {
            return lastDir.opposite();
        }
        return Direction.CENTER;
    }

    // bugnav
    public static void bugnavTowards(MapLocation dest) throws GameActionException {
        if (G.rc.isMovementReady()) {
            Direction d = bug2Helper(G.rc.getLocation(), dest, TOWARDS, 0, 0);
            if (d == Direction.CENTER) {
                d = G.rc.getLocation().directionTo(dest);
            }
            Micro.micro(d, dest);
        }
    }

    public static void bugnavAway(MapLocation dest) throws GameActionException {
        if (G.rc.isMovementReady()) {
            Direction d = bug2Helper(G.rc.getLocation(), dest, AWAY, 0, 0);
            if (d == Direction.CENTER) {
                d = G.rc.getLocation().directionTo(dest);
            }
            Micro.micro(d, dest);
        }
    }

    public static void bugnavAround(MapLocation dest, int minRadiusSquared, int maxRadiusSquared)
            throws GameActionException {
        if (G.rc.isMovementReady()) {
            Direction d = bug2Helper(G.rc.getLocation(), dest, AROUND, minRadiusSquared, maxRadiusSquared);
            if (d == Direction.CENTER) {
                d = G.rc.getLocation().directionTo(dest);
            }
            Micro.micro(d, dest);
        }
    }

    public static MapLocation bfsDest;
    public static long[] bfsMap;
    public static long[] bfsDist;
    public static long[] bfsCurr;
    public static long bitmask;
    // public static StringBuilder bfsQueue = new StringBuilder();
    public static final int MAX_PATH_LENGTH = 100;

    public static void bfsInit() {
        width = G.rc.getMapWidth();
        height = G.rc.getMapHeight();
        bfsMap = new long[height + 2];
        bfsCurr = new long[height + 2];
        bfsDist = new long[(height + 2) * MAX_PATH_LENGTH];
        bitmask = (long1 << width) - 1;
    }

    public static int step = 1;
    public static int stepOffset;
    public static int width;
    public static int height;
    public static long long1 = 1;
    public static int recalculationNeeded = MAX_PATH_LENGTH;

    public static void updateBfsMap() throws GameActionException {
        MapInfo[] map = G.rc.senseNearbyMapInfos();
        for (int i = map.length; --i >= 0;) {
            MapInfo m = map[i];
            if (m.isWall()) {
                int loc = m.getMapLocation().y + 1;
                int subloc = m.getMapLocation().x;
                if (((bfsMap[loc] >> subloc) & 1) == 0) {
                    bfsMap[loc] |= (long1 << subloc);
                    G.rc.setIndicatorDot(m.getMapLocation(), 255, 255, 255);
                    for (int j = step - 1; j >= 0; j--) {
                        if (((bfsDist[j * (height + 2) + loc] >> subloc) & 1) != 1) {
                            recalculationNeeded = Math.min(j, recalculationNeeded);
                            break;
                        }
                    }
                }
            }
        }
    }

    public static void bfs() throws GameActionException {

        if (recalculationNeeded != MAX_PATH_LENGTH && recalculationNeeded < step) {
            step = recalculationNeeded;
            for (int i = 1; i <= height; i++) {
                // bfsDist[i] = 0;
                // bfsCurr[i] = 0;
                bfsCurr[i] = bfsDist[step * (height + 2) + i];
            }
            step += 1;
            G.indicatorString.append("RECALCULATING;");
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
            // cod += "public static void bfs" + i + "() {\n";
            // for (var j = 1; j <= i; j++) {
            // cod += "Motion.bfsCurr[z] = Motion.bfsCurr[z] | (Motion.bfsCurr[z] >> 1) |
            // (Motion.bfsCurr[z] << 1);\n".replaceAll("z", j);
            // }
            // for (var j = 1; j <= i; j++) {
            // cod += "Motion.bfsDist[Motion.stepOffset + z] = (Motion.bfsCurr[z] |
            // Motion.bfsCurr[y] | Motion.bfsCurr[x]) & (Motion.bitmask ^
            // Motion.bfsMap[z]);\n".replaceAll("z", j).replaceAll("y", j -
            // 1).replaceAll("x", j + 1);
            // }
            // for (var j = 1; j <= i; j++) {
            // //cod += "Motion.bfsDist[Motion.stepOffset + z] &= Motion.bitmask ^
            // Motion.bfsMap[z];\n".replaceAll("z", j);
            // }
            // for (var j = 1; j <= i; j++) {
            // cod += "Motion.bfsCurr[z] = Motion.bfsDist[Motion.stepOffset +
            // z];\n".replaceAll("z", j);
            // }
            // cod += "}\n";
            // }
            // console.log(cod);
            step += 1;
        }

        // int b = G.rc.getRoundNum() % width;
        // if (G.rc.getRoundNum() == 201) {
        // for (int i = 0; i < width; i++) {
        // b = i;
        // for (int j = 0; j < height; j++) {
        // // if (((bfsDist[(G.rc.getRoundNum() % 100) * (height + 2) + j + 1] >> i) &
        // 1) == 0) {
        // if (((bfsDist[(G.rc.getRoundNum() % 100) * (height + 2) + j + 1] >> b) & 1)
        // == 0) {
        // if (((bfsMap[j + 1] >> b) & 1) == 0) {
        // G.rc.setIndicatorDot(new MapLocation(b, j), 255, 0, 0);
        // }
        // else {
        // G.rc.setIndicatorDot(new MapLocation(b, j), 0, 0, 0);
        // }
        // }
        // else {
        // if (((bfsMap[j + 1] >> b) & 1) == 0) {
        // G.rc.setIndicatorDot(new MapLocation(b, j), 255, 255, 255);
        // }
        // else {
        // G.rc.setIndicatorDot(new MapLocation(b, j), 0, 255, 0);
        // }
        // }
        // }
        // }
        // }
        G.indicatorString.append("STEP=" + step);
    }

    public static Direction getBfsDirection(MapLocation dest) throws GameActionException {
        MapLocation me = G.rc.getLocation();

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
                if (G.rc.canMove(dir)) {
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
            G.indicatorString.append("BUGNAV");

            if (canMove(optimalDirection)) {
                return optimalDirection;
            }
        }
        if (canMove(optimalDirection)) {
            return optimalDirection;
        }
        return Direction.CENTER;
    }

    public static void bfsnav(MapLocation dest) throws GameActionException {
        G.indicatorString.append("BFSN: " + Clock.getBytecodesLeft() + " ");
        updateBfsTarget(dest);

        if (!G.rc.getLocation().equals(dest) && G.rc.isMovementReady()) {
            Direction d = getBfsDirection(dest);
            if (d == Direction.CENTER) {
                d = G.rc.getLocation().directionTo(dest);
            }
            Micro.micro(d, dest);
        } else {
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
        G.indicatorString.append(Clock.getBytecodesLeft() + " ");
    }

    public static void updateBfsTarget(MapLocation dest) throws GameActionException {
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

    public static void move(Direction dir) throws GameActionException {
        if (G.rc.canMove(dir)) {
            G.rc.move(dir);
            lastDir = dir;
            updateInfo();
        }
    }

    public static boolean canMove(Direction dir) throws GameActionException {
        return G.rc.canMove(dir);
    }

    public static void updateInfo() throws GameActionException {
        opponentRobots = G.rc.senseNearbyRobots(-1, G.rc.getTeam().opponent());
        allyRobots = G.rc.senseNearbyRobots(-1, G.rc.getTeam());
        // MapInfo[] infos = G.rc.senseNearbyMapInfos();
        // for (MapInfo info : infos) {
        // G.infos[info.getMapLocation().x][info.getMapLocation().y] = info;
        // }
        // oh wait it doesn't save bytecode
    }
}