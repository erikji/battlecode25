package SPAARK;

import battlecode.common.*;
import java.util.*;

//store global stuff that you dont want to pass around
//and doesnt fit anywhere else
public class G {
    public static RobotController rc;
    public static Random rng;
    public static UnitType[] towerTypes = new UnitType[]{
        UnitType.LEVEL_ONE_DEFENSE_TOWER,
        UnitType.LEVEL_ONE_PAINT_TOWER,
        UnitType.LEVEL_ONE_MONEY_TOWER
    };

    public static StringBuilder indicatorString;
}
