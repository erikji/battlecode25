package SPAARK;

import battlecode.common.*;
import java.util.*;

public class POI {
    protected static RobotController rc;
    protected static StringBuilder indicatorString;

    protected static Random rng;

    public static int[] towers = new int[25];
    public static int[] towerIDs = new int[25];


    //symmetry detection
    protected static long[] nowall = new long[60];
    protected static long[] wall = new long[60];
    protected static long[] ruin = new long[60];
    protected static long[] noruin = new long[60];
    protected static boolean[] symmetry = new boolean[]{false, false, false};
    public static boolean criticalSymmetry = false;
    //0: horz
    //1: vert
    //2: rot

    // stores all tower and ruin data

    // 26th is symmetry
    public static StringBuilder[] robotsThatKnowInformation = new StringBuilder[26];
    public static boolean[] critical = new boolean[25];

    public static void init() {
        for (int i = 0; i < 25; i++) {
            towers[i] = -1;
        }
        for (int i = 0; i < 26; i++) {
            robotsThatKnowInformation[i] = new StringBuilder();
        }
    };

    public static void addTower(int source, int data) {
        for (int i = 0; i < 25; i++) {
            if (((towers[i] ^ data) & 0b111111111111) == 0 || towers[i] == -1) {
                if (towers[i] != data) {
                    towers[i] = data;
                    robotsThatKnowInformation[i] = new StringBuilder("-" + source + "-");
                    if (source == -1 || source == i) {
                        critical[i] = true;
                    }
                    else {
                        critical[i] = false;
                    }
                }
                else {
                    robotsThatKnowInformation[i].append("-" + source + "-");
                }
                break;
            }
        }
    };
    public static void updateInfo() throws Exception {
        MapLocation[] nearbyRuins = rc.senseNearbyRuins(-1);

        for (MapLocation i : nearbyRuins) {
            addTower(-1, intifyTower(2) | intifyLocation(i));
        }
        // hopefully its set
        for (RobotInfo i : Motion.allyRobots) {
            if (Robot.isTower(i.getType())) {
                addTower(-1, intifyTower(0) | intifyLocation(i.getLocation()));
            }
        }
        for (RobotInfo i : Motion.opponentRobots) {
            if (Robot.isTower(i.getType())) {
                addTower(-1, intifyTower(1) | intifyLocation(i.getLocation()));
            }
        }

        // bytecode inefficient symmetry detection
        MapInfo[] infos = rc.senseNearbyMapInfos();
        for (MapInfo info : infos) {
            MapLocation xy = info.getMapLocation();
            if (info.isWall()) wall[xy.y] |= 1L << xy.x;
            else nowall[xy.y] |= 1L << xy.x;
            if (info.hasRuin()) ruin[xy.y] |= 1L << xy.x;
            else noruin[xy.y] |= 1L << xy.x;
        }
        if (symmetry[0]&&!symmetryValid(0)) {
            symmetry[0]=false;
            criticalSymmetry = true;
        }
        if (symmetry[1]&&!symmetryValid(1)) {
            symmetry[1]=false;
            criticalSymmetry = true;
        }
        if (symmetry[2]&&!symmetryValid(2)) {
            symmetry[2]=false;
            criticalSymmetry = true;
        }
    };

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
    public static void sendMessages() throws Exception {
        if (Robot.isTower(rc.getType())) {
            // we just send all info that the robots dont have
            for (RobotInfo r : Motion.allyRobots) {
                if (!Robot.isTower(r.getType())) {
                    while (rc.canSendMessage(r.getLocation(), 0)) {
                        int message = -1;
                        int messages = 0;
                        if (!robotsThatKnowInformation[25].toString().contains("-" + r.getID() + "-")) {
                            message = intifySymmetry();
                            messages += 1;
                            robotsThatKnowInformation[25].append("-" + r.getID() + "-");
                        }
                        for (int i = 0; i < 25; i++) {
                            if (!robotsThatKnowInformation[i].toString().contains("-" + r.getID() + "-")) {
                                message = appendToMessage(message, towers[i]);
                                messages += 1;
                                robotsThatKnowInformation[i].append("-" + r.getID() + "-");
                                if (messages == 2) {
                                    break;
                                }
                            }
                        }
                        if (messages == 0) {
                            break;
                        }
                        rc.sendMessage(r.getLocation(), message);
                    }
                }
            }
        }
        else {
            int message = -1;
            int messages = 0;
            if (criticalSymmetry) {
                message = intifySymmetry();
                messages += 1;
            }
            for (RobotInfo r : Motion.allyRobots) {
                if (Robot.isTower(r.getType())) {
                    if (rc.canSendMessage(r.getLocation(), 0)) {
                        if (messages < 2) {
                            for (int i = 0; i < 25; i++) {
                                if (critical[i] && ((intifyLocation(r.getLocation()) ^ towers[i]) & 0b111111111111) != 0) {
                                    message = appendToMessage(message, towers[i]);
                                    messages += 1;
                                    critical[i] = false;
                                    if (messages == 2) {
                                        break;
                                    }
                                }
                            }
                            if (!robotsThatKnowInformation[25].toString().contains("-" + r.getID() + "-")) {
                                message = appendToMessage(message, intifySymmetry());
                                messages += 1;
                                robotsThatKnowInformation[25].append("-" + r.getID() + "-");
                            }
                            if (messages < 2) {
                                for (int i = 0; i < 25; i++) {
                                    if (!robotsThatKnowInformation[i].toString().contains("-" + r.getID() + "-")) {
                                        message = appendToMessage(message, towers[i]);
                                        messages += 1;
                                        robotsThatKnowInformation[i].append("-" + r.getID() + "-");
                                        if (messages == 2) {
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        if (messages != 0) {
                            if (criticalSymmetry) {
                                criticalSymmetry = false;
                            }
                            rc.sendMessage(r.getLocation(), message);
                            break;
                        }
                    }
                }
            }
        }
    };
    public static void readMessages() throws Exception {
        // what hapepns if message is sent in same round?? oof oof oof
        Message[] messages = rc.readMessages(rc.getRoundNum() - 1);
        for (Message m : messages) {
            addTower(m.getSenderID(), m.getBytes() & 0b1111111111111111);
            if (m.getBytes() >> 16 != 0) {
                addTower(m.getSenderID(), (m.getBytes() >> 16) & 0b1111111111111111);
            }
        }
    };

    protected static MapLocation parseLocation(int n) {
        return new MapLocation((n & 0b111111) - 1, (n >> 6) & 0b111111);
    }
    protected static int intifyLocation(MapLocation loc) {
        return ((loc.y << 6) | loc.x) + 1;
    }
    
    // team 0 for ally
    // team 1 for opp
    // team 2 for neutral
    protected static int intifyTower(int team) {
        return team << 12;
    }
    protected static int intifySymmetry() {
        return ((symmetry[0] ? 1 : 0) + (symmetry[1] ? 1 : 0) * 2 + (symmetry[2] ? 1 : 0) * 4 + 3) << 12;
    }
    
    protected static int appendToMessage(int message, int a) {
        if (message == -1) {
            return a;
        }
        return (message << 16) | a;
    }

    // need to keep track of what info each tower has
    // as well as what info is most important
    // then send messages to tower

    // for tower, recieve message = update stuff
    // also send most important stuff to nearby robots
}