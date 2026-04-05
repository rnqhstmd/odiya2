package com.loopers.domain.traveltime;

import com.loopers.domain.appointment.AppointmentParticipant;
import com.loopers.domain.traveltime.port.ExternalTravelTimeProvider;
import com.loopers.domain.usersettings.TransportType;
import com.loopers.domain.usersettings.UserSettings;
import com.loopers.domain.usersettings.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class TravelTimeService {

    private final TravelTimeRepository travelTimeRepository;
    private final TravelTimeCacheRepository travelTimeCacheRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final ExternalTravelTimeProvider externalTravelTimeProvider;
    private final TravelTimeProperties properties;

    /**
     * Calculate and save travel time for a participant.
     * Flow: null check -> near-range check (withinDistanceMeters) -> cache -> API -> fallback -> DB save
     */
    @Transactional
    public TravelTime calculateAndSave(AppointmentParticipant participant) {
        if (participant.getDeparturePlace() == null) {
            return null;
        }

        double originLat = participant.getDeparturePlace().getLatitude();
        double originLng = participant.getDeparturePlace().getLongitude();
        double destLat = participant.getAppointment().getLatitude();
        double destLng = participant.getAppointment().getLongitude();
        TransportType transportType = participant.getTransportType();

        DurationResult durationResult = calculateDuration(originLat, originLng, destLat, destLng, transportType);

        UserSettings settings = userSettingsRepository.findActiveByUserId(participant.getUser().getId())
            .orElse(null);
        int parkingBuffer = (transportType == TransportType.CAR_PARKING && settings != null)
            ? settings.getParkingBufferMinutes() : 0;
        int extraMinutes = settings != null ? settings.getExtraMinutes() : 5;

        int totalMinutes = durationResult.durationMinutes() + parkingBuffer + extraMinutes;

        ZonedDateTime appointmentTime = participant.getAppointment().getDateTime();
        ZonedDateTime departureAlertAt = appointmentTime.minusMinutes(totalMinutes);

        TravelTime existing = travelTimeRepository.findByParticipantId(participant.getId()).orElse(null);
        if (existing != null) {
            existing.update(durationResult.durationMinutes(), transportType, departureAlertAt, durationResult.isFallback());
            return travelTimeRepository.save(existing);
        }

        TravelTime travelTime = TravelTime.create(participant, durationResult.durationMinutes(),
            transportType, departureAlertAt, durationResult.isFallback());
        return travelTimeRepository.save(travelTime);
    }

    private DurationResult calculateDuration(double originLat, double originLng,
                                             double destLat, double destLng,
                                             TransportType transportType) {
        // E-2: near-range check — 출발지/목적지가 설정된 임계 거리(기본 50m) 이내면 0분으로 처리
        if (isWithinDirectRange(originLat, originLng, destLat, destLng)) {
            return new DurationResult(0, false, false);
        }

        // Cache lookup
        Optional<Integer> cached = travelTimeCacheRepository.find(originLat, originLng, destLat, destLng, transportType);
        if (cached.isPresent()) {
            return new DurationResult(cached.get(), false, true);
        }

        // External API call
        try {
            int duration = calculateDurationFromApi(transportType, originLat, originLng, destLat, destLng);
            // Save to cache (non-fallback only, D1)
            travelTimeCacheRepository.save(originLat, originLng, destLat, destLng, transportType, duration);
            return new DurationResult(duration, false, false);
        } catch (Exception e) {
            log.warn("외부 API 이동시간 계산 실패, fallback 사용: transportType={}, error={}", transportType, e.getMessage(), e);
        }

        // Haversine fallback
        int fallbackDuration = calculateHaversineFallback(originLat, originLng, destLat, destLng, transportType);
        return new DurationResult(fallbackDuration, true, false);
    }

    private int calculateDurationFromApi(TransportType transportType,
                                         double originLat, double originLng,
                                         double destLat, double destLng) {
        if (transportType == TransportType.WALKING) {
            return calculateWalkingDuration(originLat, originLng, destLat, destLng);
        }
        return externalTravelTimeProvider.calculateDuration(
            transportType, originLat, originLng, destLat, destLng);
    }

    /**
     * 출발지와 목적지가 설정된 직접 이동 임계 거리({@code properties.withinDistanceMeters()}) 이내인지 여부.
     * 이내라면 외부 API 호출 없이 0분으로 간주한다.
     */
    private boolean isWithinDirectRange(double lat1, double lng1, double lat2, double lng2) {
        return haversineDistance(lat1, lng1, lat2, lng2) <= properties.withinDistanceMeters();
    }

    double haversineDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return properties.earthRadiusMeters() * c;
    }

    private int calculateWalkingDuration(double lat1, double lng1, double lat2, double lng2) {
        double distanceMeters = haversineDistance(lat1, lng1, lat2, lng2);
        return (int) Math.ceil(distanceMeters / properties.walkingSpeedMetersPerMinute());
    }

    private int calculateHaversineFallback(double lat1, double lng1, double lat2, double lng2,
                                           TransportType transportType) {
        double distanceMeters = haversineDistance(lat1, lng1, lat2, lng2);
        return switch (transportType) {
            case CAR_PARKING, CAR_PICKUP -> {
                double distanceKm = distanceMeters / 1000.0;
                double hours = (distanceKm * properties.carDetourFactor()) / properties.carSpeedKmh();
                yield (int) Math.ceil(hours * 60);
            }
            case TRANSIT -> {
                double distanceKm = distanceMeters / 1000.0;
                double hours = (distanceKm * properties.transitDetourFactor()) / properties.transitSpeedKmh();
                yield (int) Math.ceil(hours * 60);
            }
            case WALKING -> (int) Math.ceil(distanceMeters / properties.walkingSpeedMetersPerMinute());
        };
    }

    private record DurationResult(int durationMinutes, boolean isFallback, boolean fromCache) {
    }
}
