package SPAARK;

import battlecode.common.*;

public class POI {
    static Team opponentTeam = Team.NEUTRAL;

    MapLocation[] allyTowers = new MapLocation[25];
    MapLocation[] oppTowers = new MapLocation[25];
    MapLocation[] ruins = new MapLocation[25];


    // need to keep track of what info each tower has
    // as well as what info is most important
    // then send messages to tower

    // for tower, recieve message = update stuff
    // also send most important stuff to nearby robots
}