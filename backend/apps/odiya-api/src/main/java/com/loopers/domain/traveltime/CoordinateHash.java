package com.loopers.domain.traveltime;

import java.util.Locale;

public final class CoordinateHash {

    private CoordinateHash() {
    }

    public static String hash(double lat, double lng) {
        return String.format(Locale.US, "%.4f,%.4f", lat, lng);
    }
}
