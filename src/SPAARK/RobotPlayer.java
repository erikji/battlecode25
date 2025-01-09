package SPAARK;

import battlecode.common.*;
import java.util.*;

public class RobotPlayer {
    public static void updateInfo() throws Exception {
        G.me = G.rc.getLocation();
        G.allyRobots = G.rc.senseNearbyRobots(-1, G.rc.getTeam());
        G.opponentRobots = G.rc.senseNearbyRobots(-1, POI.opponentTeam);
        G.infos = G.rc.senseNearbyMapInfos();
        POI.updateInfo();
    }

    public static void run(RobotController rc) throws Exception {
        try {
            G.rc = rc;
            G.rng = new Random(G.rc.getID() + 2025);
            G.mapCenter = new MapLocation(G.rc.getMapWidth() / 2, G.rc.getMapHeight() / 2);
            G.opponentTeam = G.rc.getTeam().opponent();
            G.indicatorString = new StringBuilder();
            updateInfo();
            switch (G.rc.getType()) {
                case MOPPER:
                case SOLDIER:
                case SPLASHER:
                    Robot.init();
                    break;
                default:
                    Tower.init();
                    break;
            }
            while (true) {
                try {
                    G.indicatorString = new StringBuilder();
                    POI.readMessages();
                    updateInfo();
                    switch (G.rc.getType()) {
                        case MOPPER:
                        case SOLDIER:
                        case SPLASHER:
                            Robot.run();
                            break;
                        default:
                            Tower.run();
                            break;
                    }
                    G.rc.setIndicatorString(G.indicatorString.toString());
                    Clock.yield();
                } catch (GameActionException e) {
                    System.out.println("Unexpected GameActionException");
                    e.printStackTrace();
                } catch (Exception e) {
                    System.out.println("Unexpected Exception");
                    e.printStackTrace();
                }
            }
        } catch (GameActionException e) {
            System.out.println("Unexpected GameActionException");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Unexpected Exception");
            e.printStackTrace();
        }
    }
}