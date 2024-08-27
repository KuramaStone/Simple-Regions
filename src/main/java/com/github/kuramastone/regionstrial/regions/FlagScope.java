package com.github.kuramastone.regionstrial.regions;

import java.util.Objects;

/**
Who a flag can apply to within a region
 */
public enum FlagScope {
    NONE, EVERYONE, WHITELIST;

    public static FlagScope getOrNull(String scopeName) {
        Objects.requireNonNull(scopeName);

        try {
            return valueOf(scopeName.toUpperCase());
        }
        catch (Exception e) {
            return null;
        }
    }
}
