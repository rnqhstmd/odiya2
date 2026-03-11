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

import java.time.ZonedDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class TravelTimeService {

    private static final double WALKING_SPEED_METERS_PER_MINUTE = 80.0;
    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    private final TravelTimeRepository travelTimeRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final KakaoMobilityApiClient kakaoMobilityApiClient;
    private final OdsayApiClient odsayApiClient;

    /**
     * Calculate and save travel time for a participant.
     * Returns the saved TravelTime entity.
     */
    public TravelTime calculateAndSave(AppointmentParticipant participant) {
        if (participant.getDeparturePlace() == null) {
            return null;
        }

        double originLat = participant.getDeparturePlace().getLatitude();
        double originLng = participant.getDeparturePlace().getLongitude();
        double destLat = participant.getAppointment().getLatitude();
        double destLng = participant.getAppointment().getLongitude();
        TransportType transportType = participant.getTransportType();

        int durationMinutes;
        if (Double.compare(originLat, destLat) == 0 && Double.compare(originLng, destLng) == 0) {
            durationMinutes = 0;
        } else {
            durationMinutes = calculateDuration(transportType, originLng, originLat, destLng, destLat);
        }

        UserSettings settings = userSettingsRepository.findActiveByUserId(participant.getUser().getId())
            .orElse(null);
        int parkingBuffer = (transportType == TransportType.CAR_PARKING && settings != null)
            ? settings.getParkingBufferMinutes() : 0;
        int extraMinutes = settings != null ? settings.getExtraMinutes() : 5;

        int totalMinutes = durationMinutes + parkingBuffer + extraMinutes;

        ZonedDateTime appointmentTime = participant.getAppointment().getDateTime();
        ZonedDateTime departureAlertAt = appointmentTime.minusMinutes(totalMinutes);

        TravelTime existing = travelTimeRepository.findByParticipantId(participant.getId()).orElse(null);
        if (existing != null) {
            existing.update(durationMinutes, transportType, departureAlertAt);
            return travelTimeRepository.save(existing);
        }

        TravelTime travelTime = TravelTime.create(participant, durationMinutes, transportType, departureAlertAt);
        return travelTimeRepository.save(travelTime);
    }

    private int calculateDuration(TransportType transportType, double originLng, double originLat,
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

    /**
     * Calculate walking duration using Haversine formula.
     * Speed: 80 meters per minute.
     */
    private int calculateWalkingDuration(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distanceMeters = EARTH_RADIUS_METERS * c;

        return (int) Math.ceil(distanceMeters / WALKING_SPEED_METERS_PER_MINUTE);
    }
}
