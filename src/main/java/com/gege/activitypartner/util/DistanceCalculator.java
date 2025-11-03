package com.gege.activitypartner.util;

import java.math.BigDecimal;

/**
 * Utility class for calculating distances between geographic coordinates
 * using the Haversine formula.
 */
public class DistanceCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Calculate the distance between two points using the Haversine formula.
     *
     * @param lat1 Latitude of the first point
     * @param lon1 Longitude of the first point
     * @param lat2 Latitude of the second point
     * @param lon2 Longitude of the second point
     * @return Distance in kilometers
     */
    public static double calculateDistance(BigDecimal lat1, BigDecimal lon1,
                                          BigDecimal lat2, BigDecimal lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return Double.MAX_VALUE; // Return max distance if any coordinate is null
        }

        double lat1Rad = Math.toRadians(lat1.doubleValue());
        double lon1Rad = Math.toRadians(lon1.doubleValue());
        double lat2Rad = Math.toRadians(lat2.doubleValue());
        double lon2Rad = Math.toRadians(lon2.doubleValue());

        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Calculate the distance between two points using the Haversine formula.
     * Overloaded method that accepts double values.
     *
     * @param lat1 Latitude of the first point
     * @param lon1 Longitude of the first point
     * @param lat2 Latitude of the second point
     * @param lon2 Longitude of the second point
     * @return Distance in kilometers
     */
    public static double calculateDistance(double lat1, double lon1,
                                          double lat2, double lon2) {
        return calculateDistance(
            BigDecimal.valueOf(lat1),
            BigDecimal.valueOf(lon1),
            BigDecimal.valueOf(lat2),
            BigDecimal.valueOf(lon2)
        );
    }
}
