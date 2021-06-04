package org.yatopiamc.c2me.common.util;

public interface IBiomeArray {

    /**
     * Check if the specified Y coordinate is covered in this BiomeArray
     * @param y biome Y coordinate
     * @return whether y is within range
     */
    boolean isWithinRange(int y);

}
