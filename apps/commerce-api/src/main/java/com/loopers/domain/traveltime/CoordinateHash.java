package com.loopers.domain.traveltime;

public final class CoordinateHash {

    private CoordinateHash() {
    }

    public static String hash(double lat, double lng) {
        return String.format("%.4f,%.4f", lat, lng);
    }
}
