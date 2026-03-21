package com.loopers.domain.traveltime;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.appointment.AppointmentParticipant;
import com.loopers.domain.usersettings.TransportType;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.ZonedDateTime;

@Entity
@Table(name = "travel_times")
public class TravelTime extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false, unique = true)
    private AppointmentParticipant participant;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "transport_type", nullable = false, length = 20)
    private TransportType transportType;

    @Column(name = "calculated_at", nullable = false)
    private ZonedDateTime calculatedAt;

    @Column(name = "departure_alert_at")
    private ZonedDateTime departureAlertAt;

    @Column(name = "is_fallback", nullable = false)
    private boolean isFallback = false;

    protected TravelTime() {}

    private TravelTime(AppointmentParticipant participant, Integer durationMinutes,
                       TransportType transportType, ZonedDateTime calculatedAt,
                       ZonedDateTime departureAlertAt, boolean isFallback) {
        guardDurationMinutes(durationMinutes);
        this.participant = participant;
        this.durationMinutes = durationMinutes;
        this.transportType = transportType;
        this.calculatedAt = calculatedAt;
        this.departureAlertAt = departureAlertAt;
        this.isFallback = isFallback;
    }

    public static TravelTime create(AppointmentParticipant participant, Integer durationMinutes,
                                     TransportType transportType, ZonedDateTime departureAlertAt) {
        return new TravelTime(participant, durationMinutes, transportType,
                              ZonedDateTime.now(), departureAlertAt, false);
    }

    public static TravelTime create(AppointmentParticipant participant, Integer durationMinutes,
                                     TransportType transportType, ZonedDateTime departureAlertAt,
                                     boolean isFallback) {
        return new TravelTime(participant, durationMinutes, transportType,
                              ZonedDateTime.now(), departureAlertAt, isFallback);
    }

    @Override
    protected void guard() {
        guardDurationMinutes(this.durationMinutes);
    }

    public void update(Integer durationMinutes, TransportType transportType,
                       ZonedDateTime departureAlertAt) {
        update(durationMinutes, transportType, departureAlertAt, false);
    }

    public void update(Integer durationMinutes, TransportType transportType,
                       ZonedDateTime departureAlertAt, boolean isFallback) {
        guardDurationMinutes(durationMinutes);
        this.durationMinutes = durationMinutes;
        this.transportType = transportType;
        this.calculatedAt = ZonedDateTime.now();
        this.departureAlertAt = departureAlertAt;
        this.isFallback = isFallback;
    }

    public AppointmentParticipant getParticipant() { return participant; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public TransportType getTransportType() { return transportType; }
    public ZonedDateTime getCalculatedAt() { return calculatedAt; }
    public ZonedDateTime getDepartureAlertAt() { return departureAlertAt; }
    public boolean isFallback() { return isFallback; }

    private void guardDurationMinutes(Integer durationMinutes) {
        if (durationMinutes == null || durationMinutes < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이동시간은 0분 이상이어야 합니다.");
        }
    }
}
