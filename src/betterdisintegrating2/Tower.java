package betterdisintegrating2;

import battlecode.common.*;
import java.util.*;

public class Tower {
    public static int spawnedSoldiers = 0;
    public static int spawnedSplashers = 0;
    public static int spawnedMoppers = 0;
    public static int spawnedRobots = 0;

    public static MapLocation[] spawnLocs;
    public static int level;

    public static void init() throws Exception {
        spawnLocs = new MapLocation[] {
                G.me.add(Direction.NORTH),
                G.me.add(Direction.NORTH).add(Direction.NORTH),
                G.me.add(Direction.NORTHEAST),
                G.me.add(Direction.EAST),
                G.me.add(Direction.EAST).add(Direction.EAST),
                G.me.add(Direction.SOUTHEAST),
                G.me.add(Direction.SOUTH),
                G.me.add(Direction.SOUTH).add(Direction.SOUTH),
                G.me.add(Direction.SOUTHWEST),
                G.me.add(Direction.WEST),
                G.me.add(Direction.WEST).add(Direction.WEST),
                G.me.add(Direction.NORTHWEST)
        };
        Arrays.sort(spawnLocs,
                (MapLocation a, MapLocation b) -> a.distanceSquaredTo(G.mapCenter) - b.distanceSquaredTo(G.mapCenter));
    }

    public static void spawnBot(UnitType t) throws Exception {
        switch (t) {
            case UnitType.MOPPER:
                for (MapLocation loc : spawnLocs) {
                    if (G.rc.canBuildRobot(UnitType.MOPPER, loc)) {
                        G.rc.buildRobot(UnitType.MOPPER, loc);
                        spawnedRobots++;
                        spawnedMoppers++;
                        break;
                    }
                }
                break;
            case UnitType.SPLASHER:
                for (MapLocation loc : spawnLocs) {
                    if (G.rc.canBuildRobot(UnitType.SPLASHER, loc)) {
                        G.rc.buildRobot(UnitType.SPLASHER, loc);
                        spawnedRobots++;
                        spawnedSplashers++;
                        break;
                    }
                }
                break;
            case UnitType.SOLDIER:
                for (MapLocation loc : spawnLocs) {
                    if (G.rc.canBuildRobot(UnitType.SOLDIER, loc)) {
                        G.rc.buildRobot(UnitType.SOLDIER, loc);
                        spawnedRobots++;
                        spawnedSoldiers++;
                        break;
                    }
                }
                break;
            default:
                throw new Exception("what are you spawning?? a tower???");
        }
    }

    public static void run() throws Exception {
        // general common code for all towers
        // spawning
        if (spawnedRobots == 0) {
            spawnBot(UnitType.SOLDIER);
        } else if (spawnedRobots == 1) {
            spawnBot(UnitType.SPLASHER);
        } else if (G.rc.getNumberTowers() > 2 || G.round > 50) {
            //don't suffocate money until we built a tower
            int mod = 7;
            int area = G.rc.getMapHeight() * G.rc.getMapWidth();
            
            switch ((spawnedRobots - 2) % 7) {
                // make sure to subtract 2
                case 0:
                    spawnBot(UnitType.MOPPER);
                    break;
                case 1:
                    spawnBot(UnitType.SPLASHER);
                    break;
                case 2:
                    spawnBot(UnitType.SOLDIER);
                    break;
                case 3:
                    // spawnBot(UnitType.SOLDIER);
                    spawnBot(UnitType.SOLDIER);
                    break;
                case 4:
                    spawnBot(UnitType.SPLASHER);
                    break;
                case 5:
                    spawnBot(UnitType.SPLASHER);
                    break;
                case 6:
                    spawnBot(UnitType.MOPPER);
                    break;
            }
        }
        // more specialized here
        switch (G.rc.getType()) {
            case LEVEL_ONE_DEFENSE_TOWER -> {
                level = 0;
                DefenseTower.run();
            }
            case LEVEL_TWO_DEFENSE_TOWER -> {
                level = 1;
                DefenseTower.run();
            }
            case LEVEL_THREE_DEFENSE_TOWER -> {
                level = 2;
                DefenseTower.run();
            }
            case LEVEL_ONE_MONEY_TOWER -> {
                level = 0;
                MoneyTower.run();
            }
            case LEVEL_TWO_MONEY_TOWER -> {
                level = 1;
                MoneyTower.run();
            }
            case LEVEL_THREE_MONEY_TOWER -> {
                level = 2;
                MoneyTower.run();
            }
            case LEVEL_ONE_PAINT_TOWER -> {
                level = 0;
                PaintTower.run();
            }
            case LEVEL_TWO_PAINT_TOWER -> {
                level = 1;
                PaintTower.run();
            }
            case LEVEL_THREE_PAINT_TOWER -> {
                level = 2;
                PaintTower.run();
            }
            default -> throw new Exception("Challenge Complete! How Did We Get Here?");
        }
        // TODO: make required chips based on tower level
        // i really hope all money towers dont go boom
        destroy: if (G.rc.getNumberTowers() == 25 && ((G.rc.getType().getBaseType() == UnitType.LEVEL_ONE_PAINT_TOWER && G.rc.getChips() > 25000) || G.rc.getType().getBaseType() == UnitType.LEVEL_ONE_MONEY_TOWER && G.rc.getChips() > 100000)) {
            int best = -1;
            int bestDistance = 0;
            for (int i = 144; --i >= 0;) {
                if (POI.towers[i] == -1) {
                    break;
                }
                if (POI.parseTowerTeam(POI.towers[i]) != G.opponentTeam) {
                    continue;
                }
                int distance = Motion.getChebyshevDistance(G.me, POI.parseLocation(POI.towers[i]));
                if (best == -1 || distance < bestDistance) {
                    best = i;
                    bestDistance = distance;
                }
            }
            MapLocation closestOpponentTower = new MapLocation(-1, -1);
            if (best == -1) {
                if (POI.symmetry[0]) {
                    closestOpponentTower = new MapLocation(G.rc.getMapWidth() - 1 - G.me.x, G.me.y);
                }
                if (POI.symmetry[1]) {
                    closestOpponentTower = new MapLocation(G.me.x, G.rc.getMapHeight() - 1 - G.me.y);
                }
                if (POI.symmetry[2]) {
                    closestOpponentTower = new MapLocation(G.rc.getMapWidth() - 1 - G.me.x, G.rc.getMapHeight() - 1 - G.me.y);
                }
                // break destroy;
            }
            else {
                closestOpponentTower = POI.parseLocation(POI.towers[best]);
            }
            boolean foundFurther = false;
            for (int i = 144; --i >= 0;) {
                if (POI.towers[i] == -1) {
                    break;
                }
                if (POI.parseTowerTeam(POI.towers[i]) != G.team) {
                    continue;
                }
                int distance = Motion.getChebyshevDistance(closestOpponentTower, POI.parseLocation(POI.towers[i]));
                if (distance > bestDistance) {
                    foundFurther = true;
                    break;
                }
                if (Clock.getBytecodesLeft() < 2500) {
                    break;
                }
            }
            if (foundFurther) {
                break destroy;
            }
            // better to attack before disintegration
            attack();
            G.rc.disintegrate();
            return;
        }
        while (G.rc.canUpgradeTower(G.me) && G.rc.getMoney() - (level==0?2500:5000) >= 5000) {
            G.rc.upgradeTower(G.me);
        }
        // attack AFTER run (in case we get an upgrade)
        attack();
    }
    public static void attack() throws Exception {
        MapLocation bestEnemyLoc = null;
        int bestEnemyHp = 1000000;
        UnitType bestEnemyType = UnitType.MOPPER;
        // priority: soldier, splasher, mopper
        int numAttackableRobots = 0;
        for (int i = G.opponentRobots.length; --i >= 0;) {
            RobotInfo r = G.opponentRobots[i];
            if (G.rc.canAttack(r.location)) {
                numAttackableRobots++;
                switch (bestEnemyType) {
                    case UnitType.MOPPER:
                        if (r.type == UnitType.SOLDIER || r.type == UnitType.SPLASHER || r.health < bestEnemyHp) {
                            bestEnemyHp = r.health;
                            bestEnemyLoc = r.location;
                            bestEnemyType = r.type;
                        }
                        break;
                    case UnitType.SPLASHER:
                        if (r.type == UnitType.SOLDIER || (r.type == UnitType.SPLASHER && r.health < bestEnemyHp)) {
                            bestEnemyHp = r.health;
                            bestEnemyLoc = r.location;
                            bestEnemyType = r.type;
                        }
                        break;
                    case UnitType.SOLDIER:
                        if (r.type == UnitType.SOLDIER && r.health < bestEnemyHp) {
                            bestEnemyHp = r.health;
                            bestEnemyLoc = r.location;
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        if (numAttackableRobots > 2 && G.rc.canAttack(null)) {
            G.rc.attack(null);
        } else if (G.rc.canAttack(bestEnemyLoc)) {
            G.rc.attack(bestEnemyLoc);
        }
    }
}
