package org.millenaire.building;

/**
 * Simply maps a building ID to a level.
 */
public class BuildingRecord {
    /**
     * Building's internal name
     */
    public String ID;
    /**
     * Pretty self-explanatory
     */
    public int lvl;

    /**
     * This is just here to make the JSON parser work correctly. USE OTHER CONSTRUCTOR!
     */
    public BuildingRecord() {

    }
}
