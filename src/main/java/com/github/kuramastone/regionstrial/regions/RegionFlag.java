package com.github.kuramastone.regionstrial.regions;

import java.util.Objects;

/**
Types of region flags. Could have been a simple string, but kept as a class for simple defaults and future expansion
 */
public class RegionFlag {
    //CAN_BREAK_BLOCK, CAN_PLACE_BLOCK, CAN_INTERACT, CAN_RECEIVE_DAMAGE;

    public static final RegionFlag CAN_BREAK_BLOCK = new RegionFlag("CAN_BREAK_BLOCK");
    public static final RegionFlag CAN_PLACE_BLOCK = new RegionFlag("CAN_PLACE_BLOCK");
    public static final RegionFlag CAN_INTERACT = new RegionFlag("CAN_INTERACT");
    public static final RegionFlag CAN_RECEIVE_DAMAGE = new RegionFlag("CAN_RECEIVE_DAMAGE");

    private String name;

    public RegionFlag(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegionFlag that = (RegionFlag) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
