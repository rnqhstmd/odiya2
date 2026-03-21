package com.loopers.domain.traveltime;

import com.loopers.domain.appointment.AppointmentParticipant;
import com.loopers.domain.usersettings.TransportType;
import com.loopers.domain.usersettings.UserSettings;
import com.loopers.domain.usersettings.UserSettingsRepository;
import com.loopers.infrastructure.kakao.KakaoMobilityApiClient;
import com.loopers.infrastructure.odsay.OdsayApiClient;
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

    private static final double WALKING_SPEED_METERS_PER_MINUTE = 80.0;
    private static final double EARTH_RADIUS_METERS = 6_371_000.0;
    private static final double WITHIN_DISTANCE_METERS = 50.0;
    private static final double CAR_DETOUR_FACTOR = 1.4;
    private static final double CAR_SPEED_KMH = 40.0;
    private static final double TRANSIT_DETOUR_FACTOR = 1.5;
    private static final double TRANSIT_SPEED_KMH = 30.0;

    private final TravelTimeRepository travelTimeRepository;
    private final TravelTimeCacheRepository travelTimeCacheRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final KakaoMobilityApiClient kakaoMobilityApiClient;
    private final OdsayApiClient odsayApiClient;

    /**
     * Calculate and save travel time for a participant.
     * Flow: null check -> 50m check -> cache -> API -> fallback -> DB save
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
        // E-2: within 50m -> 0 minutes
        if (isWithin50Meters(originLat, originLng, destLat, destLng)) {
            return new DurationResult(0, false, false);
        }

        // Cache lookup
        Optional<Integer> cached = travelTimeCacheRepository.find(originLat, originLng, destLat, destLng, transportType);
        if (cached.isPresent()) {
            return new DurationResult(cached.get(), false, true);
        }

        // External API call
        try {
            int duration = calculateDurationFromApi(transportType, originLng, originLat, destLng, destLat);
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

    private int calculateDurationFromApi(TransportType transportType, double originLng, double originLat,
                                         double destLng, double destLat) {
        return switch (transportType) {
            case CAR_PARKING, CAR_PICKUP ->
                kakaoMobilityApiClient.calculateDuration(originLng, originLat, destLng, destLat);
            case TRANSIT ->
                odsayApiClient.calculateDuration(originLng, originLat, destLng, destLat);
            case WALKING ->
                calculateWalkingDuration(originLat, originLng, destLat, destLng);
        };
    }

    private boolean isWithin50Meters(double lat1, double lng1, double lat2, double lng2) {
        return haversineDistance(lat1, lng1, lat2, lng2) <= WITHIN_DISTANCE_METERS;
    }

    double haversineDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }

    private int calculateWalkingDuration(double lat1, double lng1, double lat2, double lng2) {
        double distanceMeters = haversineDistance(lat1, lng1, lat2, lng2);
        return (int) Math.ceil(distanceMeters / WALKING_SPEED_METERS_PER_MINUTE);
    }

    private int calculateHaversineFallback(double lat1, double lng1, double lat2, double lng2,
                                           TransportType transportType) {
        double distanceMeters = haversineDistance(lat1, lng1, lat2, lng2);
        return switch (transportType) {
            case CAR_PARKING, CAR_PICKUP -> {
                double distanceKm = distanceMeters / 1000.0;
                double hours = (distanceKm * CAR_DETOUR_FACTOR) / CAR_SPEED_KMH;
                yield (int) Math.ceil(hours * 60);
            }
            case TRANSIT -> {
                double distanceKm = distanceMeters / 1000.0;
                double hours = (distanceKm * TRANSIT_DETOUR_FACTOR) / TRANSIT_SPEED_KMH;
                yield (int) Math.ceil(hours * 60);
            }
            case WALKING -> (int) Math.ceil(distanceMeters / WALKING_SPEED_METERS_PER_MINUTE);
        };
    }

    private record DurationResult(int durationMinutes, boolean isFallback, boolean fromCache) {
    }
}
