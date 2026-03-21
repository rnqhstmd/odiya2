package com.loopers.domain.traveltime;

public final class CoordinateHash {

    private CoordinateHash() {
    }

    public static String hash(double lat, double lng) {
        double roundedLat = Math.round(lat * 10_000.0) / 10_000.0;
        double roundedLng = Math.round(lng * 10_000.0) / 10_000.0;
        return String.format("%.4f,%.4f", roundedLat, roundedLng);
    }
}
