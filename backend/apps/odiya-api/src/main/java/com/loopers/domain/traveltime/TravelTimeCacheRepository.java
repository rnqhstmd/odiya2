package com.loopers.domain.traveltime;

import com.loopers.domain.usersettings.TransportType;

import java.util.Optional;

public interface TravelTimeCacheRepository {

    Optional<Integer> find(double originLat, double originLng, double destLat, double destLng,
                           TransportType transportType);

    void save(double originLat, double originLng, double destLat, double destLng,
              TransportType transportType, int durationMinutes);

    void evict(double originLat, double originLng, double destLat, double destLng,
               TransportType transportType);
}
