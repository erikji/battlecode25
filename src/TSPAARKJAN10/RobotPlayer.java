package TSPAARKJAN10;

import battlecode.common.*;
import java.util.*;

public class RobotPlayer {
    public static void updateInfo() throws Exception {
        //every time we move
        G.me = G.rc.getLocation();
        G.allyRobots = G.rc.senseNearbyRobots(-1, G.team);
        G.opponentRobots = G.rc.senseNearbyRobots(-1, G.opponentTeam);
        G.infos = G.rc.senseNearbyMapInfos();
    }

    public static void updateRound() throws Exception {
        //every round
        updateInfo();
        MapLocation[] visible = G.rc.getAllLocationsWithinRadiusSquared(G.me, 20);
        for (int i = visible.length; --i >= 0; --i) {
            G.lastVisited[visible[i].x][visible[i].y] = G.rc.getRoundNum();
        }
        POI.updateRound();
    }

    public static void run(RobotController rc) throws Exception {
        try {
            G.rc = rc;
            G.rng = new Random(G.rc.getID() + 2025);
            G.mapCenter = new MapLocation(G.rc.getMapWidth() / 2, G.rc.getMapHeight() / 2);
            G.team = G.rc.getTeam();
            G.opponentTeam = G.team.opponent();
            G.indicatorString = new StringBuilder();
            updateInfo();
            switch (G.rc.getType()) {
                case MOPPER, SOLDIER, SPLASHER -> Robot.init();
                default -> Tower.init();
            }
            while (true) {
                try {
                    G.indicatorString = new StringBuilder();
                    updateRound();
                    switch (G.rc.getType()) {
                        case MOPPER, SOLDIER, SPLASHER -> Robot.run();
                        default -> Tower.run();
                    }
                    G.rc.setIndicatorString(G.indicatorString.toString());
                    Clock.yield();
                } catch (GameActionException e) {
                    // System.out.println("Unexpected GameActionException");
                    e.printStackTrace();
                } catch (Exception e) {
                    // System.out.println("Unexpected Exception");
                    e.printStackTrace();
                }
            }
        } catch (GameActionException e) {
            // System.out.println("Unexpected GameActionException");
            e.printStackTrace();
        } catch (Exception e) {
            // System.out.println("Unexpected Exception");
            e.printStackTrace();
        }
    }
}